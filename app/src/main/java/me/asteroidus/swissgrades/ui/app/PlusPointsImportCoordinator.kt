package me.asteroidus.swissgrades.ui.app

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.Normalizer
import javax.xml.parsers.DocumentBuilderFactory

private const val MaxPlusPointsImportBytes = 2 * 1024 * 1024

data class PreparedPlusPointsImport(
    val displayName: String,
    val importedState: GradeTrackerAppState,
    val sourceSemester: SchoolSemester? = null
)

interface PlusPointsImportCoordinator {
    fun prepareImport(sourceUriString: String): PreparedPlusPointsImport
    fun discardPreparedImport(preparedImport: PreparedPlusPointsImport)
}

class LocalPlusPointsImportCoordinator(
    private val context: Context
) : PlusPointsImportCoordinator {
    override fun prepareImport(sourceUriString: String): PreparedPlusPointsImport {
        val sourceUri = sourceUriString.toUri()
        val displayName = resolveDisplayName(sourceUri) ?: "pluspoints-export.PlusPointsExport"
        val xml = context.contentResolver.openInputStream(sourceUri)?.use { it.readPlusPointsImportText() }
            ?: throw IllegalStateException("Could not open PlusPoints export.")
        return PreparedPlusPointsImport(
            displayName = displayName,
            importedState = parsePlusPointsExport(xml),
            sourceSemester = detectPlusPointsSourceSemesterFromXml(xml)
        )
    }

    override fun discardPreparedImport(preparedImport: PreparedPlusPointsImport) = Unit

    private fun resolveDisplayName(sourceUri: Uri): String? {
        return context.contentResolver.query(sourceUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
    }
}

object NoOpPlusPointsImportCoordinator : PlusPointsImportCoordinator {
    override fun prepareImport(sourceUriString: String): PreparedPlusPointsImport {
        throw IllegalStateException("PlusPoints import is unavailable.")
    }

    override fun discardPreparedImport(preparedImport: PreparedPlusPointsImport) = Unit
}

internal fun parsePlusPointsExport(xml: String): GradeTrackerAppState {
    val root = parsePlistRoot(xml)
    val data = root["data"] as? Map<*, *> ?: throw IllegalStateException("Missing PlusPoints data.")
    val sourceSemester = detectPlusPointsSourceSemester(data["name"] as? String)
    val subjectMaps = data["subjects"] as? List<*> ?: emptyList<Any>()

    val detectedOption = detectOptionChoice(subjectMaps)
    var nextSubjectSequence = 1
    var nextNoteSequence = 1

    val optionSubject = createImportedOptionSubject(detectedOption, nextSubjectSequence++)
    val importedSubjects = mutableListOf<StoredSubject>()

    subjectMaps.forEach { rawSubject ->
        val subject = rawSubject as? Map<*, *> ?: return@forEach
        val subjectName = subject["name"] as? String ?: return@forEach
        val counted = ((subject["counted"] as? Number)?.toInt() ?: 1) != 0
        if (isOptionSubjectName(subjectName)) {
            val importedOption = importOptionSubject(
                source = subject,
                base = optionSubject,
                nextNoteId = { "note-${nextNoteSequence++}" },
                sourceSemester = sourceSemester
            )
            importedSubjects += importedOption
        } else {
            importedSubjects += importRegularSubject(
                source = subject,
                subjectId = "subject-${nextSubjectSequence++}",
                counted = counted,
                nextNoteId = { "note-${nextNoteSequence++}" },
                sourceSemester = sourceSemester
            )
        }
    }

    val subjects = buildList {
        if (importedSubjects.none { it.isOptionSubject }) add(optionSubject)
        addAll(importedSubjects)
    }

    return GradeTrackerAppState(
        selectedOption = detectedOption,
        subjects = subjects,
        nextSubjectSequence = nextSubjectSequence,
        nextNoteSequence = nextNoteSequence,
        selectedSemester = sourceSemester ?: SchoolSemester.SEMESTER_1
    )
}

private fun importRegularSubject(
    source: Map<*, *>,
    subjectId: String,
    counted: Boolean,
    nextNoteId: () -> String,
    sourceSemester: SchoolSemester? = null
): StoredSubject {
    val subjectName = source["name"] as? String ?: "Subject"
    val exams = source["exams"] as? List<*> ?: emptyList<Any>()
    val notes = exams.flatMap {
        importExamAsNotes(
            exam = it as? Map<*, *> ?: emptyMap<Any, Any>(),
            nextNoteId = nextNoteId,
            sourceSemester = sourceSemester
        )
    }
    return StoredSubject(
        id = subjectId,
        name = normalizePlusPointsSubjectName(subjectName),
        isCounted = counted,
        isInBasket = counted && isOfficialBasketSubjectName(subjectName),
        notes = notes,
        subjectColor = inferredColorChoice(subjectName),
        subjectIcon = inferredIconChoice(subjectName)
    )
}

private fun importOptionSubject(
    source: Map<*, *>,
    base: StoredSubject,
    nextNoteId: () -> String,
    sourceSemester: SchoolSemester? = null
): StoredSubject {
    if (base.subSubjects.isEmpty()) {
        val exams = source["exams"] as? List<*> ?: emptyList<Any>()
        return base.copy(
            notes = exams.flatMap {
                importExamAsNotes(
                    exam = it as? Map<*, *> ?: emptyMap<Any, Any>(),
                    nextNoteId = nextNoteId,
                    sourceSemester = sourceSemester
                )
            }
        )
    }

    val exams = source["exams"] as? List<*> ?: emptyList<Any>()
    val subSubjectNotesById = base.subSubjects.associate { it.id to mutableListOf<StoredNote>() }

    exams.forEach { rawExam ->
        val exam = rawExam as? Map<*, *> ?: return@forEach
        val targetSubSubjectId = when (normalizeKey(exam["name"] as? String)) {
            "am", "applications of mathematics", "applications des maths", "applications des mathematiques" ->
                base.subSubjects.getOrNull(1)?.id
            "physique", "physics" -> base.subSubjects.getOrNull(0)?.id
            "biologie", "biology" -> base.subSubjects.getOrNull(0)?.id
            "chimie", "chemistry" -> base.subSubjects.getOrNull(1)?.id
            "economie", "economics" -> base.subSubjects.getOrNull(0)?.id
            "droit", "law" -> base.subSubjects.getOrNull(1)?.id
            else -> base.subSubjects.firstOrNull()?.id
        } ?: return@forEach

        importExamAsNotes(exam, nextNoteId, sourceSemester).forEach { note ->
            subSubjectNotesById.getValue(targetSubSubjectId) += note
        }
    }

    return base.copy(
        subSubjects = base.subSubjects.map { sub ->
            sub.copy(notes = subSubjectNotesById.getValue(sub.id))
        }
    )
}

private fun importExamAsNotes(
    exam: Map<*, *>,
    nextNoteId: () -> String,
    sourceSemester: SchoolSemester? = null
): List<StoredNote> {
    val nested = exam["exams"] as? List<*> ?: emptyList<Any>()
    return if (nested.isNotEmpty()) {
        nested.mapNotNull { nestedExam ->
            createStoredNote(
                exam = nestedExam as? Map<*, *> ?: return@mapNotNull null,
                noteId = nextNoteId(),
                semester = sourceSemester ?: SchoolSemester.SEMESTER_1,
                fallbackName = exam["name"] as? String
            )
        }
    } else {
        listOfNotNull(
            createStoredNote(
                exam = exam,
                noteId = nextNoteId(),
                semester = sourceSemester ?: SchoolSemester.SEMESTER_1,
                fallbackName = null
            )
        )
    }
}

private fun createStoredNote(
    exam: Map<*, *>,
    noteId: String,
    semester: SchoolSemester,
    fallbackName: String?
): StoredNote? {
    val rawMark = exam["mark"] as? Number ?: return null
    val weight = parseWeight(exam["weight"]?.toString()) ?: return null
    val name = exam["name"] as? String
    val description = buildImportedDescription(name, fallbackName)
    return StoredNote(
        id = noteId,
        value = rawMark.toDouble(),
        weight = weight,
        description = description,
        createdAtEpochMillis = plusPointsDateToUnixMillis(exam["dAtEaTtr:date"] as? Number),
        semester = semester
    )
}

private fun buildImportedDescription(name: String?, fallbackName: String?): String {
    val cleanedName = name?.trim().orEmpty()
    val genericSubgrade = Regex("""subgrade\s+\d+""", RegexOption.IGNORE_CASE)
    return when {
        cleanedName.isBlank() -> fallbackName?.trim().orEmpty()
        genericSubgrade.matches(cleanedName) && !fallbackName.isNullOrBlank() -> fallbackName.trim()
        else -> cleanedName
    }
}

private fun parseWeight(rawWeight: String?): me.asteroidus.swissgrades.domain.model.AssessmentWeight? {
    return when (rawWeight?.trim()) {
        "1", "1.0" -> me.asteroidus.swissgrades.domain.model.AssessmentWeight.FULL
        "0.5" -> me.asteroidus.swissgrades.domain.model.AssessmentWeight.HALF
        "0.25" -> me.asteroidus.swissgrades.domain.model.AssessmentWeight.QUARTER
        else -> null
    }
}

private fun plusPointsDateToUnixMillis(raw: Number?): Long {
    if (raw == null) return 0L
    val appleEpochSeconds = raw.toDouble()
    val unixSeconds = appleEpochSeconds + 978_307_200
    return (unixSeconds * 1000).toLong()
}

private fun detectOptionChoice(subjectMaps: List<*>): InitialOptionChoice {
    subjectMaps.forEach { rawSubject ->
        val name = ((rawSubject as? Map<*, *>)?.get("name") as? String).orEmpty()
        detectOptionChoiceFromName(name)?.let { return it }
    }
    return InitialOptionChoice.OTHER
}

private fun detectPlusPointsSourceSemesterFromXml(xml: String): SchoolSemester? {
    val root = parsePlistRoot(xml)
    val data = root["data"] as? Map<*, *> ?: return null
    return detectPlusPointsSourceSemester(data["name"] as? String)
}

private fun detectPlusPointsSourceSemester(rawName: String?): SchoolSemester? {
    return when (normalizeKey(rawName)) {
        "semestre 1", "semester 1", "s1" -> SchoolSemester.SEMESTER_1
        "semestre 2", "semester 2", "s2" -> SchoolSemester.SEMESTER_2
        else -> null
    }
}

private fun isOptionSubjectName(name: String): Boolean {
    val normalized = normalizeKey(name)
    return normalized.startsWith("os ") ||
        normalized == "pyam" ||
        normalized == "bich" ||
        normalized.contains("economie-droit") ||
        normalized.contains("economics-law") ||
        normalized.contains("economie droit")
}

private fun detectOptionChoiceFromName(name: String): InitialOptionChoice? {
    val normalized = normalizeKey(name).removePrefix("os ").trim()
    return when {
        normalized.contains("pyam") -> InitialOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATH
        normalized.contains("bich") -> InitialOptionChoice.BIOLOGY_CHEMISTRY
        normalized.contains("economie-droit") || normalized.contains("economics-law") || normalized.contains("economie droit") ->
            InitialOptionChoice.ECONOMICS_LAW
        normalized.contains("espagnol") || normalized.contains("spanish") -> InitialOptionChoice.SPANISH
        normalized.contains("italien") || normalized.contains("italian") -> InitialOptionChoice.ITALIAN
        normalized.contains("latin") -> InitialOptionChoice.LATIN
        normalized.contains("musique") || normalized.contains("music") -> InitialOptionChoice.MUSIC
        normalized.contains("philo") || normalized.contains("philosophy") || normalized.contains("philosophie") ->
            InitialOptionChoice.PHILOSOPHY
        normalized.contains("arts visuels") || normalized.contains("visual arts") ->
            InitialOptionChoice.VISUAL_ARTS
        else -> null
    }
}

private fun createImportedOptionSubject(choice: InitialOptionChoice, sequence: Int): StoredSubject {
    return StoredSubject(
        id = "subject-$sequence",
        name = choice.label,
        isCounted = true,
        isInBasket = true,
        isOptionSubject = true,
        optionChoice = choice,
        notes = emptyList(),
        subSubjects = choice.compositeSubSubjectNames.mapIndexed { index, name ->
            StoredSubSubject(
                id = "option-subject-${index + 1}",
                name = name,
                notes = emptyList()
            )
        }
    )
}

private fun isOfficialBasketSubjectName(name: String): Boolean {
    return when (normalizeKey(name)) {
        "allemand", "german", "francais", "français", "french", "maths", "math", "mathematiques", "mathematics" -> true
        else -> false
    }
}

private fun normalizePlusPointsSubjectName(name: String): String {
    return when (normalizeKey(name)) {
        "maths" -> "Math"
        "allemand" -> "Allemand"
        "francais", "français" -> "Français"
        else -> name.trim()
    }
}

private fun inferredColorChoice(name: String): SubjectColorChoice {
    return when {
        normalizeKey(name).contains("math") -> SubjectColorChoice.BLUE
        normalizeKey(name).contains("phys") || normalizeKey(name).contains("chim") || normalizeKey(name).contains("bio") ->
            SubjectColorChoice.TEAL
        normalizeKey(name).contains("franc") || normalizeKey(name).contains("anglais") || normalizeKey(name).contains("allemand") ->
            SubjectColorChoice.PURPLE
        normalizeKey(name).contains("histoire") || normalizeKey(name).contains("geo") -> SubjectColorChoice.AMBER
        else -> SubjectColorChoice.BLUE
    }
}

private fun inferredIconChoice(name: String): SubjectIconChoice {
    return when {
        normalizeKey(name).contains("math") -> SubjectIconChoice.MATH
        normalizeKey(name).contains("phys") || normalizeKey(name).contains("chim") || normalizeKey(name).contains("bio") ->
            SubjectIconChoice.SCIENCE
        normalizeKey(name).contains("franc") || normalizeKey(name).contains("anglais") || normalizeKey(name).contains("allemand") ->
            SubjectIconChoice.LANGUAGE
        normalizeKey(name).contains("histoire") -> SubjectIconChoice.HISTORY
        normalizeKey(name).contains("geo") -> SubjectIconChoice.WORLD
        else -> SubjectIconChoice.BOOK
    }
}

private fun normalizeKey(value: String?): String {
    val text = value?.trim().orEmpty().lowercase()
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
}

private fun parsePlistRoot(xml: String): Map<String, Any?> {
    val factory = hardenedDocumentBuilderFactory()
    val builder = factory.newDocumentBuilder()
    val document = builder.parse(ByteArrayInputStream(xml.encodeToByteArray()))
    val plist = document.documentElement
    val firstElement = plist.childNodes.asElementSequence().firstOrNull()
        ?: throw IllegalStateException("Invalid plist file.")
    return parsePlistNode(firstElement).asStringKeyedMap()
        ?: throw IllegalStateException("Invalid plist root.")
}

private fun parsePlistNode(node: Element): Any? {
    return when (node.tagName) {
        "dict" -> parseDict(node)
        "array" -> node.childNodes.asElementSequence().map(::parsePlistNode).toList()
        "string" -> node.textContent
        "real" -> node.textContent.toDouble()
        "integer" -> node.textContent.toInt()
        "true" -> true
        "false" -> false
        else -> null
    }
}

private fun parseDict(node: Element): Map<String, Any?> {
    val children = node.childNodes.asElementSequence().toList()
    val result = linkedMapOf<String, Any?>()
    var index = 0
    while (index < children.size) {
        val keyNode = children[index]
        if (keyNode.tagName != "key") {
            index++
            continue
        }
        val valueNode = children.getOrNull(index + 1) ?: break
        result[keyNode.textContent] = parsePlistNode(valueNode)
        index += 2
    }
    return result
}

private fun Any?.asStringKeyedMap(): Map<String, Any?>? {
    val rawMap = this as? Map<*, *> ?: return null
    if (rawMap.keys.any { it !is String }) return null
    @Suppress("UNCHECKED_CAST")
    return rawMap as Map<String, Any?>
}

private fun org.w3c.dom.NodeList.asElementSequence(): Sequence<Element> = sequence {
    for (index in 0 until length) {
        val node = item(index)
        if (node.nodeType == Node.ELEMENT_NODE) yield(node as Element)
    }
}

internal fun InputStream.readPlusPointsImportText(maxBytes: Int = MaxPlusPointsImportBytes): String {
    require(maxBytes > 0) { "Maximum import size must be positive." }
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var totalBytes = 0
    while (true) {
        val read = read(buffer)
        if (read == -1) break
        totalBytes += read
        if (totalBytes > maxBytes) {
            throw IllegalStateException("PlusPoints export is too large.")
        }
        output.write(buffer, 0, read)
    }
    return output.toByteArray().decodeToString()
}

private fun hardenedDocumentBuilderFactory(): DocumentBuilderFactory {
    return DocumentBuilderFactory.newInstance().apply {
        requireXmlFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        trySetXmlFeature("http://xml.org/sax/features/external-general-entities", false)
        trySetXmlFeature("http://xml.org/sax/features/external-parameter-entities", false)
        trySetXmlFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        runCatching { isXIncludeAware = false }
        runCatching { isExpandEntityReferences = false }
    }
}

private fun DocumentBuilderFactory.requireXmlFeature(feature: String, enabled: Boolean) {
    runCatching { setFeature(feature, enabled) }
        .getOrElse { throw IllegalStateException("Secure XML parsing is unavailable.", it) }
}

private fun DocumentBuilderFactory.trySetXmlFeature(feature: String, enabled: Boolean) {
    runCatching { setFeature(feature, enabled) }
}
