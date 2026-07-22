package me.asteroidus.swissgrades.ui.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class AppBackupCoordinatorInstrumentedTest {

    private lateinit var appContext: Context
    private lateinit var backupCoordinator: LocalAppBackupCoordinator

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        backupCoordinator = LocalAppBackupCoordinator(appContext)
        GradeAttachmentPaths.attachmentsRoot(appContext.filesDir).deleteRecursively()
    }

    @Test
    fun exportAndImportBackup_roundTripsStateAndAttachments() {
        val sourceAttachment = File(appContext.cacheDir, "source-attachment.jpg").apply {
            writeText("attachment-content")
        }
        val exportFile = File(appContext.cacheDir, "round-trip-backup.sgb")
        val state = GradeTrackerAppState(
            selectedOption = InitialOptionChoice.BIOLOGY_CHEMISTRY,
            subjects = listOf(
                StoredSubject(
                    id = "subject-1",
                    name = "BICH",
                    isInBasket = true,
                    isOptionSubject = true,
                    optionChoice = InitialOptionChoice.BIOLOGY_CHEMISTRY,
                    subSubjects = listOf(
                        StoredSubSubject(
                            id = "option-subject-1",
                            name = "Biology",
                            notes = listOf(
                                StoredNote(
                                    id = "note-1",
                                    value = 5.0,
                                    weight = AssessmentWeight.FULL,
                                    description = "Exam",
                                    createdAtEpochMillis = 1L,
                                    attachments = listOf(
                                        StoredAttachment(
                                            id = "attachment-1",
                                            filePath = sourceAttachment.absolutePath
                                        )
                                    )
                                )
                            )
                        ),
                        StoredSubSubject(
                            id = "option-subject-2",
                            name = "Chemistry",
                            notes = listOf(
                                StoredNote(
                                    id = "note-2",
                                    value = 4.0,
                                    weight = AssessmentWeight.HALF,
                                    description = "Quiz",
                                    createdAtEpochMillis = 2L
                                )
                            )
                        )
                    )
                ),
                StoredSubject(
                    id = "subject-2",
                    name = "History",
                    isInBasket = false,
                    notes = listOf(
                        StoredNote(
                            id = "note-3",
                            value = 5.5,
                            weight = AssessmentWeight.QUARTER,
                            description = "Essay",
                            createdAtEpochMillis = 3L
                        )
                    )
                )
            ),
            nextSubjectSequence = 3,
            nextNoteSequence = 4,
            language = AppLanguage.ENGLISH,
            themeMode = AppThemeMode.DARK
        )

        backupCoordinator.exportBackup(state, exportFile)
        val preparedImport = backupCoordinator.prepareImport(exportFile)
        val importedState = backupCoordinator.applyPreparedImport(preparedImport)

        assertEquals(InitialOptionChoice.BIOLOGY_CHEMISTRY, importedState.selectedOption)
        assertEquals(AppLanguage.ENGLISH, importedState.language)
        assertEquals(AppThemeMode.DARK, importedState.themeMode)

        val restoredAttachment = importedState.subjects.first()
            .subSubjects.first()
            .notes.first()
            .attachments.single()
        val restoredAttachmentFile = File(restoredAttachment.filePath)
        assertTrue(restoredAttachmentFile.exists())
        assertEquals("attachment-content", restoredAttachmentFile.readText())
    }

    @Test(expected = IllegalStateException::class)
    fun prepareImport_rejectsBackupWhenAttachmentFileIsMissing() {
        val corruptedBackup = File(appContext.cacheDir, "corrupted-backup.sgb")
        val exportedState = GradeTrackerAppState(
            selectedOption = InitialOptionChoice.SPANISH,
            subjects = listOf(
                StoredSubject(
                    id = "subject-1",
                    name = "Spanish",
                    isInBasket = true,
                    isOptionSubject = true,
                    optionChoice = InitialOptionChoice.SPANISH,
                    notes = listOf(
                        StoredNote(
                            id = "note-1",
                            value = 5.0,
                            weight = AssessmentWeight.FULL,
                            description = "",
                            createdAtEpochMillis = 0L,
                            attachments = listOf(
                                StoredAttachment(
                                    id = "attachment-1",
                                    filePath = "attachments/note-1/attachment-1.jpg"
                                )
                            )
                        )
                    )
                )
            ),
            nextSubjectSequence = 2,
            nextNoteSequence = 2
        )

        ZipOutputStream(FileOutputStream(corruptedBackup)).use { zipOutputStream ->
            zipOutputStream.putNextEntry(ZipEntry("manifest.json"))
            zipOutputStream.write(
                """{"version":1,"appState":${exportedState.encodeToJsonString()}}""".toByteArray()
            )
            zipOutputStream.closeEntry()
        }

        backupCoordinator.prepareImport(corruptedBackup)
    }

    @Test(expected = IllegalStateException::class)
    fun prepareImport_rejectsBackupAttachmentPathTraversal() {
        val corruptedBackup = File(appContext.cacheDir, "path-traversal-backup.sgb")
        val exportedState = backupStateWithAttachment(
            attachmentPath = "attachments/note-1/../note-1/attachment-1.jpg"
        )

        writeBackupArchive(
            file = corruptedBackup,
            state = exportedState,
            attachmentEntries = mapOf("attachments/note-1/attachment-1.jpg" to "attachment-content")
        )

        backupCoordinator.prepareImport(corruptedBackup)
    }

    @Test(expected = IllegalStateException::class)
    fun prepareImport_rejectsBackupWithUnsafeNoteId() {
        val corruptedBackup = File(appContext.cacheDir, "unsafe-note-id-backup.sgb")
        val exportedState = backupStateWithAttachment(noteId = "../outside")

        writeBackupArchive(
            file = corruptedBackup,
            state = exportedState,
            attachmentEntries = mapOf("attachments/note-1/attachment-1.jpg" to "attachment-content")
        )

        backupCoordinator.prepareImport(corruptedBackup)
    }

    @Test(expected = IllegalStateException::class)
    fun prepareImport_rejectsBackupWithUnsafeAttachmentId() {
        val corruptedBackup = File(appContext.cacheDir, "unsafe-attachment-id-backup.sgb")
        val exportedState = backupStateWithAttachment(attachmentId = "../outside")

        writeBackupArchive(
            file = corruptedBackup,
            state = exportedState,
            attachmentEntries = mapOf("attachments/note-1/attachment-1.jpg" to "attachment-content")
        )

        backupCoordinator.prepareImport(corruptedBackup)
    }

    private fun backupStateWithAttachment(
        noteId: String = "note-1",
        attachmentId: String = "attachment-1",
        attachmentPath: String = "attachments/note-1/attachment-1.jpg"
    ): GradeTrackerAppState {
        return GradeTrackerAppState(
            selectedOption = InitialOptionChoice.SPANISH,
            subjects = listOf(
                StoredSubject(
                    id = "subject-1",
                    name = "Spanish",
                    isInBasket = true,
                    isOptionSubject = true,
                    optionChoice = InitialOptionChoice.SPANISH,
                    notes = listOf(
                        StoredNote(
                            id = noteId,
                            value = 5.0,
                            weight = AssessmentWeight.FULL,
                            description = "",
                            createdAtEpochMillis = 0L,
                            attachments = listOf(
                                StoredAttachment(
                                    id = attachmentId,
                                    filePath = attachmentPath
                                )
                            )
                        )
                    )
                )
            ),
            nextSubjectSequence = 2,
            nextNoteSequence = 2
        )
    }

    private fun writeBackupArchive(
        file: File,
        state: GradeTrackerAppState,
        attachmentEntries: Map<String, String> = emptyMap()
    ) {
        ZipOutputStream(FileOutputStream(file)).use { zipOutputStream ->
            zipOutputStream.putNextEntry(ZipEntry("manifest.json"))
            zipOutputStream.write("""{"version":1,"appState":${state.encodeToJsonString()}}""".toByteArray())
            zipOutputStream.closeEntry()

            attachmentEntries.forEach { (path, content) ->
                zipOutputStream.putNextEntry(ZipEntry(path))
                zipOutputStream.write(content.toByteArray())
                zipOutputStream.closeEntry()
            }
        }
    }
}
