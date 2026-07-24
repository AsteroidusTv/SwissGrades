package me.asteroidus.swissgrades.ui.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.net.toUri
import java.io.OutputStream
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface GradeReportExporter {
    fun suggestedFileName(
        schoolYear: SchoolYear,
        semester: SchoolSemester,
        now: Date = Date()
    ): String

    fun export(
        report: GradeReport,
        language: AppLanguage,
        destinationUriString: String
    )
}

class LocalGradeReportPdfExporter(
    private val context: Context
) : GradeReportExporter {
    private val fileDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

    override fun suggestedFileName(
        schoolYear: SchoolYear,
        semester: SchoolSemester,
        now: Date
    ): String {
        return "swissgrades-year-${schoolYear.ordinal + 1}-semester-${semester.ordinal + 1}-" +
            "${fileDateFormatter.format(now)}.pdf"
    }

    override fun export(
        report: GradeReport,
        language: AppLanguage,
        destinationUriString: String
    ) {
        val destinationUri = destinationUriString.toUri()
        context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            writeGradeReportPdf(report, language, outputStream)
        } ?: throw IllegalStateException("Could not open PDF export destination.")
    }
}

object NoOpGradeReportExporter : GradeReportExporter {
    override fun suggestedFileName(
        schoolYear: SchoolYear,
        semester: SchoolSemester,
        now: Date
    ): String = "swissgrades-report.pdf"

    override fun export(
        report: GradeReport,
        language: AppLanguage,
        destinationUriString: String
    ) = Unit
}

internal fun writeGradeReportPdf(
    report: GradeReport,
    language: AppLanguage,
    outputStream: OutputStream
): Int {
    val document = PdfDocument()
    return try {
        val writer = GradeReportPdfWriter(
            document = document,
            language = language,
            report = report
        )
        writer.write()
        document.writeTo(outputStream)
        writer.pageCount
    } finally {
        document.close()
    }
}

private class GradeReportPdfWriter(
    private val document: PdfDocument,
    private val language: AppLanguage,
    private val report: GradeReport
) {
    private val text = GradeReportText.forLanguage(language)
    private val locale = when (language) {
        AppLanguage.ENGLISH -> Locale.ENGLISH
        AppLanguage.FRENCH -> Locale.FRENCH
    }
    private val numberFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 2
    }
    private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var page: PdfDocument.Page? = null
    private var canvas: Canvas? = null
    private var cursorY = PageTop

    var pageCount: Int = 0
        private set

    fun write() {
        startPage()
        writeHeader()
        writeSummary()
        report.subjects.forEach(::writeSubject)
        finishPage()
    }

    private fun writeHeader() {
        drawText(
            value = "SWISSGRADES",
            size = 11f,
            color = AccentBlue,
            bold = true,
            letterSpacing = 1.4f
        )
        cursorY += 4f
        drawWrappedText(text.title, size = 24f, bold = true, lineHeight = 28f)
        cursorY += 5f
        drawWrappedText(
            value = text.period(
                year = report.schoolYear,
                semester = report.semester
            ),
            size = 12f,
            color = SecondaryText
        )
        drawWrappedText(
            value = text.generatedOn(dateFormat.format(Date(report.generatedAtEpochMillis))),
            size = 9f,
            color = SecondaryText
        )
        cursorY += 12f
        drawWrappedText(
            value = text.unofficialNotice,
            size = 9f,
            color = SecondaryText,
            lineHeight = 12f
        )
        cursorY += 18f
    }

    private fun writeSummary() {
        ensureSpace(146f)
        val summaryTop = cursorY
        currentCanvas().drawRoundRect(
            PageMargin,
            summaryTop,
            PageWidth - PageMargin,
            summaryTop + 130f,
            16f,
            16f,
            paint.apply {
                style = Paint.Style.FILL
                color = SummaryBackground
            }
        )
        cursorY += 24f
        drawSummaryRow(
            leftLabel = text.roundedAverage,
            leftValue = formatValue(report.overallAverage),
            rightLabel = text.status,
            rightValue = text.status(report.promotionStatus)
        )
        cursorY += 18f
        drawSummaryRow(
            leftLabel = text.promotionPoints,
            leftValue = formatSignedValue(report.promotionPoints),
            rightLabel = text.basket,
            rightValue = report.basketTotal?.let { "${formatValue(it)} / 16" } ?: text.notAvailable
        )
        cursorY += 18f
        drawText(
            value = text.insufficiencies,
            size = 8f,
            color = SecondaryText,
            bold = true,
            x = PageMargin + 18f
        )
        drawText(
            value = "${report.insufficiencyCount} / 4",
            size = 14f,
            bold = true,
            x = PageMargin + 18f
        )
        cursorY = summaryTop + 150f
    }

    private fun drawSummaryRow(
        leftLabel: String,
        leftValue: String,
        rightLabel: String,
        rightValue: String
    ) {
        val leftX = PageMargin + 18f
        val rightX = PageWidth / 2f + 8f
        val labelY = cursorY
        drawText(leftLabel, size = 8f, color = SecondaryText, bold = true, x = leftX)
        drawText(rightLabel, size = 8f, color = SecondaryText, bold = true, x = rightX)
        cursorY = labelY + 20f
        drawText(leftValue, size = 17f, bold = true, x = leftX)
        drawText(rightValue, size = 13f, bold = true, x = rightX)
    }

    private fun writeSubject(subject: GradeReportSubject) {
        ensureSpace(110f)
        drawDivider()
        cursorY += 15f
        drawWrappedText(
            value = subject.displayName(language),
            size = 18f,
            bold = true,
            lineHeight = 21f
        )
        val badges = buildList {
            if (subject.isOptionSubject) add(text.optionSubject)
            if (subject.isInBasket) add(text.inBasket)
            if (!subject.isCounted && !subject.isOptionSubject) add(text.notCounted)
        }
        if (badges.isNotEmpty()) {
            drawWrappedText(
                value = badges.joinToString(" · "),
                size = 9f,
                color = AccentBlue,
                bold = true,
                lineHeight = 12f
            )
        }
        cursorY += 5f
        drawWrappedText(
            value = "${text.roundedAverage}: ${formatValue(subject.officialAverage)}  ·  " +
                "${text.detailedAverage}: ${formatValue(subject.detailedAverage)}  ·  " +
                "${text.points}: ${formatSignedValue(subject.promotionPoints)}",
            size = 10f,
            color = SecondaryText,
            lineHeight = 14f
        )
        cursorY += 8f

        if (subject.subSubjects.isEmpty()) {
            writeNotes(subject.notes, indent = 0f)
        } else {
            subject.subSubjects.forEach { subSubject ->
                ensureSpace(60f)
                drawWrappedText(
                    value = "${subSubject.displayName(language)}  ·  " +
                        "${text.average}: ${formatValue(subSubject.average)}",
                    size = 12f,
                    bold = true,
                    x = PageMargin + SubjectIndent,
                    maxWidth = ContentWidth - SubjectIndent,
                    lineHeight = 15f
                )
                cursorY += 5f
                writeNotes(subSubject.notes, indent = SubjectIndent)
                cursorY += 6f
            }
        }
    }

    private fun writeNotes(notes: List<GradeReportNote>, indent: Float) {
        if (notes.isEmpty()) {
            drawWrappedText(
                value = text.noGrades,
                size = 10f,
                color = SecondaryText,
                x = PageMargin + indent,
                maxWidth = ContentWidth - indent
            )
            return
        }

        notes.forEach { note ->
            ensureSpace(46f)
            val description = note.description.ifBlank { text.defaultAssessment }
            drawWrappedText(
                value = description,
                size = 11f,
                bold = true,
                x = PageMargin + indent,
                maxWidth = ContentWidth - indent,
                lineHeight = 14f
            )
            drawWrappedText(
                value = "${formatValue(note.value)}  ·  ${text.weight(note.weight)}  ·  " +
                    "${text.semesterShort(note.semester)}  ·  ${formatNoteDate(note.createdAtEpochMillis)}",
                size = 9f,
                color = SecondaryText,
                x = PageMargin + indent,
                maxWidth = ContentWidth - indent,
                lineHeight = 12f
            )
            cursorY += 7f
        }
    }

    private fun drawDivider() {
        currentCanvas().drawLine(
            PageMargin,
            cursorY,
            PageWidth - PageMargin,
            cursorY,
            paint.apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = DividerColor
            }
        )
    }

    private fun drawText(
        value: String,
        size: Float,
        color: Int = PrimaryText,
        bold: Boolean = false,
        x: Float = PageMargin,
        letterSpacing: Float = 0f
    ) {
        configurePaint(size, color, bold, letterSpacing)
        currentCanvas().drawText(value, x, cursorY, paint)
    }

    private fun drawWrappedText(
        value: String,
        size: Float,
        color: Int = PrimaryText,
        bold: Boolean = false,
        x: Float = PageMargin,
        maxWidth: Float = ContentWidth,
        lineHeight: Float = size * 1.35f
    ) {
        configurePaint(size, color, bold)
        wrapText(value, maxWidth).forEach { line ->
            ensureSpace(lineHeight)
            configurePaint(size, color, bold)
            currentCanvas().drawText(line, x, cursorY, paint)
            cursorY += lineHeight
        }
    }

    private fun wrapText(value: String, maxWidth: Float): List<String> {
        if (value.isBlank()) return listOf("")
        val lines = mutableListOf<String>()
        value.lines().forEach { paragraph ->
            var current = ""
            paragraph.split(Regex("\\s+")).forEach { word ->
                val wordParts = splitLongWord(word, maxWidth)
                wordParts.forEachIndexed { index, wordPart ->
                    val candidate = if (current.isEmpty()) wordPart else "$current $wordPart"
                    if (paint.measureText(candidate) <= maxWidth || current.isEmpty()) {
                        current = candidate
                    } else {
                        lines += current
                        current = wordPart
                    }
                    if (index < wordParts.lastIndex) {
                        lines += current
                        current = ""
                    }
                }
            }
            if (current.isNotEmpty()) lines += current
        }
        return lines
    }

    private fun splitLongWord(word: String, maxWidth: Float): List<String> {
        if (paint.measureText(word) <= maxWidth) return listOf(word)
        val parts = mutableListOf<String>()
        var current = ""
        word.forEach { character ->
            val candidate = current + character
            if (current.isNotEmpty() && paint.measureText(candidate) > maxWidth) {
                parts += current
                current = character.toString()
            } else {
                current = candidate
            }
        }
        if (current.isNotEmpty()) parts += current
        return parts
    }

    private fun configurePaint(
        size: Float,
        color: Int,
        bold: Boolean,
        letterSpacing: Float = 0f
    ) {
        paint.apply {
            style = Paint.Style.FILL
            textSize = size
            this.color = color
            typeface = Typeface.create(
                Typeface.SANS_SERIF,
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
            this.letterSpacing = letterSpacing / 100f
        }
    }

    private fun ensureSpace(requiredHeight: Float) {
        if (cursorY + requiredHeight > ContentBottom) {
            finishPage()
            startPage()
        }
    }

    private fun startPage() {
        pageCount += 1
        page = document.startPage(
            PdfDocument.PageInfo.Builder(PageWidth.toInt(), PageHeight.toInt(), pageCount).create()
        )
        canvas = requireNotNull(page).canvas
        currentCanvas().drawColor(Color.WHITE)
        cursorY = PageTop
    }

    private fun finishPage() {
        val currentPage = page ?: return
        val footerY = PageHeight - 38f
        currentCanvas().drawLine(
            PageMargin,
            footerY - 12f,
            PageWidth - PageMargin,
            footerY - 12f,
            paint.apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = DividerColor
            }
        )
        configurePaint(size = 7.5f, color = SecondaryText, bold = false)
        val footer = text.footer
        currentCanvas().drawText(footer, PageMargin, footerY, paint)
        val pageLabel = text.page(pageCount)
        currentCanvas().drawText(
            pageLabel,
            PageWidth - PageMargin - paint.measureText(pageLabel),
            footerY,
            paint
        )
        document.finishPage(currentPage)
        page = null
        canvas = null
    }

    private fun currentCanvas(): Canvas = requireNotNull(canvas)

    private fun formatValue(value: Double?): String {
        return value?.let(numberFormat::format) ?: text.notAvailable
    }

    private fun formatSignedValue(value: Double?): String {
        value ?: return text.notAvailable
        val prefix = if (value > 0.0) "+" else ""
        return prefix + numberFormat.format(value)
    }

    private fun formatNoteDate(epochMillis: Long): String {
        return if (epochMillis > 0L) dateFormat.format(Date(epochMillis)) else text.noDate
    }

    private companion object {
        const val PageWidth = 595f
        const val PageHeight = 842f
        const val PageMargin = 42f
        const val PageTop = 48f
        const val ContentBottom = 780f
        const val ContentWidth = PageWidth - (PageMargin * 2f)
        const val SubjectIndent = 18f
        const val PrimaryText = 0xFF121722.toInt()
        const val SecondaryText = 0xFF566173.toInt()
        const val AccentBlue = 0xFF377FD5.toInt()
        const val SummaryBackground = 0xFFEAF3FF.toInt()
        const val DividerColor = 0xFFD8E0EA.toInt()
    }
}
