package me.asteroidus.swissgrades.ui.app

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val BackupManifestFileName = "manifest.json"
private const val BackupAttachmentsDirectoryName = "attachments"
private const val BackupFormatVersion = 1
private val SafeBackupPathComponentRegex = Regex("[A-Za-z0-9][A-Za-z0-9._-]*")

data class PreparedBackupImport(
    val displayName: String,
    internal val workingDirectoryPath: String,
    internal val importedState: GradeTrackerAppState
)

interface AppBackupCoordinator {
    fun suggestedBackupFileName(now: Date = Date()): String

    fun exportBackup(state: GradeTrackerAppState, destinationUriString: String)

    fun prepareImport(sourceUriString: String): PreparedBackupImport

    fun applyPreparedImport(preparedBackupImport: PreparedBackupImport): GradeTrackerAppState

    fun discardPreparedImport(preparedBackupImport: PreparedBackupImport)
}

class LocalAppBackupCoordinator(
    private val context: Context
) : AppBackupCoordinator {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

    override fun suggestedBackupFileName(now: Date): String {
        return "swissgrades-backup-${dateFormatter.format(now)}.sgb"
    }

    override fun exportBackup(state: GradeTrackerAppState, destinationUriString: String) {
        val destinationUri = destinationUriString.toUri()
        context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            ZipOutputStream(outputStream).use { zipOutputStream ->
                writeBackupArchive(zipOutputStream, state)
            }
        } ?: throw IllegalStateException("Could not open export destination.")
    }

    override fun prepareImport(sourceUriString: String): PreparedBackupImport {
        val sourceUri = sourceUriString.toUri()
        val workingDirectory = File(context.cacheDir, "prepared-backup-import-${UUID.randomUUID()}").apply {
            mkdirs()
        }
        try {
            val displayName = resolveDisplayName(sourceUri) ?: "swissgrades-backup.sgb"
            unzipBackup(sourceUri, workingDirectory)
            val manifestFile = File(workingDirectory, BackupManifestFileName)
            if (!manifestFile.exists()) {
                throw IllegalStateException("Backup manifest is missing.")
            }
            val manifestJson = manifestFile.readText()
            val importedState = decodeBackupManifest(manifestJson)
            validateImportedAttachments(importedState, workingDirectory)
            return PreparedBackupImport(
                displayName = displayName,
                workingDirectoryPath = workingDirectory.absolutePath,
                importedState = importedState
            )
        } catch (error: Exception) {
            workingDirectory.deleteRecursively()
            throw error
        }
    }

    override fun applyPreparedImport(preparedBackupImport: PreparedBackupImport): GradeTrackerAppState {
        val workingDirectory = File(preparedBackupImport.workingDirectoryPath)
        if (!workingDirectory.exists()) {
            throw IllegalStateException("Prepared backup import is no longer available.")
        }

        val preparedAttachmentsRoot = File(workingDirectory, "prepared-attachments")
        val restoredState = restoreAttachmentsIntoPreparedRoot(
            importedState = preparedBackupImport.importedState,
            unpackedDirectory = workingDirectory,
            preparedAttachmentsRoot = preparedAttachmentsRoot
        )

        val attachmentsRoot = GradeAttachmentPaths.attachmentsRoot(context.filesDir)
        val rollbackRoot = File(context.cacheDir, "backup-attachments-rollback-${UUID.randomUUID()}")

        try {
            if (attachmentsRoot.exists()) {
                attachmentsRoot.parentFile?.mkdirs()
                if (!attachmentsRoot.renameTo(rollbackRoot)) {
                    attachmentsRoot.copyRecursively(rollbackRoot, overwrite = true)
                    attachmentsRoot.deleteRecursively()
                }
            }

            if (preparedAttachmentsRoot.exists()) {
                attachmentsRoot.parentFile?.mkdirs()
                if (!preparedAttachmentsRoot.renameTo(attachmentsRoot)) {
                    preparedAttachmentsRoot.copyRecursively(attachmentsRoot, overwrite = true)
                }
            } else {
                attachmentsRoot.deleteRecursively()
            }

            rollbackRoot.deleteRecursively()
            workingDirectory.deleteRecursively()
            return restoredState
        } catch (error: Exception) {
            attachmentsRoot.deleteRecursively()
            if (rollbackRoot.exists()) {
                rollbackRoot.parentFile?.mkdirs()
                if (!rollbackRoot.renameTo(attachmentsRoot)) {
                    rollbackRoot.copyRecursively(attachmentsRoot, overwrite = true)
                }
            }
            throw error
        } finally {
            rollbackRoot.deleteRecursively()
            workingDirectory.deleteRecursively()
        }
    }

    override fun discardPreparedImport(preparedBackupImport: PreparedBackupImport) {
        File(preparedBackupImport.workingDirectoryPath).deleteRecursively()
    }

    internal fun exportBackup(state: GradeTrackerAppState, destinationFile: File) {
        destinationFile.parentFile?.mkdirs()
        FileOutputStream(destinationFile).use { outputStream ->
            ZipOutputStream(outputStream).use { zipOutputStream ->
                writeBackupArchive(zipOutputStream, state)
            }
        }
    }

    internal fun prepareImport(sourceFile: File): PreparedBackupImport {
        val workingDirectory = File(context.cacheDir, "prepared-backup-import-${UUID.randomUUID()}").apply {
            mkdirs()
        }
        try {
            unzipBackup(sourceFile, workingDirectory)
            val manifestFile = File(workingDirectory, BackupManifestFileName)
            if (!manifestFile.exists()) {
                throw IllegalStateException("Backup manifest is missing.")
            }
            val importedState = decodeBackupManifest(manifestFile.readText())
            validateImportedAttachments(importedState, workingDirectory)
            return PreparedBackupImport(
                displayName = sourceFile.name,
                workingDirectoryPath = workingDirectory.absolutePath,
                importedState = importedState
            )
        } catch (error: Exception) {
            workingDirectory.deleteRecursively()
            throw error
        }
    }

    private fun writeBackupArchive(
        zipOutputStream: ZipOutputStream,
        state: GradeTrackerAppState
    ) {
        val attachmentSources = collectAttachmentSources(state)
        val exportedState = state.withBackupAttachmentPaths(attachmentSources.associate { it.originalFilePath to it.archivePath })
        val manifestJson = JSONObject()
            .put("version", BackupFormatVersion)
            .put("appState", JSONObject(exportedState.encodeToJsonString()))
            .toString()

        zipOutputStream.putNextEntry(ZipEntry(BackupManifestFileName))
        zipOutputStream.write(manifestJson.toByteArray())
        zipOutputStream.closeEntry()

        attachmentSources.forEach { attachmentSource ->
            val sourceFile = File(attachmentSource.originalFilePath)
            if (!sourceFile.exists()) {
                throw IllegalStateException("Attachment file is missing: ${sourceFile.name}")
            }
            zipOutputStream.putNextEntry(ZipEntry(attachmentSource.archivePath))
            FileInputStream(sourceFile).use { inputStream ->
                inputStream.copyTo(zipOutputStream)
            }
            zipOutputStream.closeEntry()
        }
    }

    private fun unzipBackup(sourceUri: Uri, destinationDirectory: File) {
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            unzipBackup(inputStream, destinationDirectory)
        } ?: throw IllegalStateException("Could not open backup file.")
    }

    private fun unzipBackup(sourceFile: File, destinationDirectory: File) {
        FileInputStream(sourceFile).use { inputStream ->
            unzipBackup(inputStream, destinationDirectory)
        }
    }

    private fun unzipBackup(
        inputStream: java.io.InputStream,
        destinationDirectory: File
    ) {
        ZipInputStream(inputStream).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                val destinationFile = File(destinationDirectory, entry.name).canonicalFile
                val destinationRoot = destinationDirectory.canonicalFile
                val destinationRootPath = destinationRoot.path + File.separator
                if (destinationFile.path != destinationRoot.path && !destinationFile.path.startsWith(destinationRootPath)) {
                    throw IllegalStateException("Invalid backup archive entry.")
                }
                if (entry.isDirectory) {
                    destinationFile.mkdirs()
                } else {
                    destinationFile.parentFile?.mkdirs()
                    FileOutputStream(destinationFile).use { outputStream ->
                        zipInputStream.copyTo(outputStream)
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        }
    }

    private fun decodeBackupManifest(manifestJson: String): GradeTrackerAppState {
        val root = JSONObject(manifestJson)
        val version = root.optInt("version", -1)
        if (version != BackupFormatVersion) {
            throw IllegalStateException("Unsupported backup version.")
        }
        val appStateJson = root.optJSONObject("appState")
            ?: throw IllegalStateException("Backup manifest is invalid.")
        return decodeGradeTrackerAppState(appStateJson.toString())
    }

    private fun validateImportedAttachments(
        importedState: GradeTrackerAppState,
        unpackedDirectory: File
    ) {
        importedState.allStoredNotes().forEach { note ->
            requireSafeBackupPathComponent(note.id, "note")
            note.attachments.forEach { attachment ->
                requireSafeBackupPathComponent(attachment.id, "attachment")
                val attachmentFile = resolveImportedAttachmentFile(unpackedDirectory, attachment.filePath)
                if (!attachmentFile.exists() || !attachmentFile.isFile) {
                    throw IllegalStateException("Backup attachment is missing.")
                }
            }
        }
    }

    private fun restoreAttachmentsIntoPreparedRoot(
        importedState: GradeTrackerAppState,
        unpackedDirectory: File,
        preparedAttachmentsRoot: File
    ): GradeTrackerAppState {
        val preparedRoot = preparedAttachmentsRoot.canonicalFile
        return importedState.mapStoredNotes { note ->
            requireSafeBackupPathComponent(note.id, "note")
            val restoredAttachments = note.attachments.map { attachment ->
                requireSafeBackupPathComponent(attachment.id, "attachment")
                val importedFile = resolveImportedAttachmentFile(unpackedDirectory, attachment.filePath)
                val extension = importedFile.extension.takeIf { it.isNotBlank() } ?: "jpg"
                val noteDirectory = File(preparedRoot, "notes/${note.id}").canonicalFile
                noteDirectory.requireInside(preparedRoot)
                noteDirectory.mkdirs()
                val restoredFile = File(noteDirectory, "${attachment.id}.$extension").canonicalFile
                restoredFile.requireInside(preparedRoot)
                importedFile.copyTo(restoredFile, overwrite = true)
                StoredAttachment(
                    id = attachment.id,
                    filePath = File(
                        GradeAttachmentPaths.noteDirectory(context.filesDir, note.id),
                        "${attachment.id}.$extension"
                    ).absolutePath
                )
            }
            note.copy(attachments = restoredAttachments)
        }
    }

    private fun resolveDisplayName(sourceUri: Uri): String? {
        context.contentResolver.query(sourceUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameColumn >= 0 && cursor.moveToFirst()) {
                    return cursor.getString(nameColumn)
                }
            }
        return sourceUri.lastPathSegment?.substringAfterLast('/')
    }
}

object NoOpAppBackupCoordinator : AppBackupCoordinator {
    override fun suggestedBackupFileName(now: Date): String = "swissgrades-backup.sgb"

    override fun exportBackup(state: GradeTrackerAppState, destinationUriString: String) = Unit

    override fun prepareImport(sourceUriString: String): PreparedBackupImport {
        throw IllegalStateException("Backup import is unavailable.")
    }

    override fun applyPreparedImport(preparedBackupImport: PreparedBackupImport): GradeTrackerAppState {
        throw IllegalStateException("Backup import is unavailable.")
    }

    override fun discardPreparedImport(preparedBackupImport: PreparedBackupImport) = Unit
}

private data class AttachmentBackupSource(
    val originalFilePath: String,
    val archivePath: String
)

private fun GradeTrackerAppState.withBackupAttachmentPaths(
    archivePathsByOriginalFilePath: Map<String, String>
): GradeTrackerAppState {
    return mapStoredNotes { note ->
        note.copy(
            attachments = note.attachments.map { attachment ->
                attachment.copy(
                    filePath = requireNotNull(archivePathsByOriginalFilePath[attachment.filePath])
                )
            }
        )
    }
}

private fun collectAttachmentSources(state: GradeTrackerAppState): List<AttachmentBackupSource> {
    return state.allStoredNotes().flatMap { note ->
        requireSafeBackupPathComponent(note.id, "note")
        note.attachments.map { attachment ->
            requireSafeBackupPathComponent(attachment.id, "attachment")
            val extension = File(attachment.filePath).extension.takeIf { it.isNotBlank() } ?: "jpg"
            AttachmentBackupSource(
                originalFilePath = attachment.filePath,
                archivePath = "$BackupAttachmentsDirectoryName/${note.id}/${attachment.id}.$extension"
            )
        }
    }
}

private fun resolveImportedAttachmentFile(unpackedDirectory: File, archivePath: String): File {
    val normalizedPath = archivePath.replace('\\', '/')
    val segments = normalizedPath.split('/')
    if (
        normalizedPath.isBlank() ||
        File(normalizedPath).isAbsolute ||
        segments.firstOrNull() != BackupAttachmentsDirectoryName ||
        segments.size < 3 ||
        segments.any { it.isBlank() || it == "." || it == ".." }
    ) {
        throw IllegalStateException("Backup attachment path is invalid.")
    }

    val unpackedRoot = unpackedDirectory.canonicalFile
    val attachmentsRoot = File(unpackedRoot, BackupAttachmentsDirectoryName).canonicalFile
    val attachmentFile = File(unpackedRoot, normalizedPath).canonicalFile
    attachmentFile.requireInside(attachmentsRoot)
    return attachmentFile
}

private fun requireSafeBackupPathComponent(value: String, label: String) {
    if (!SafeBackupPathComponentRegex.matches(value) || value == "." || value == "..") {
        throw IllegalStateException("Backup $label id is invalid.")
    }
}

private fun File.requireInside(root: File) {
    val rootPath = root.path + File.separator
    if (path != root.path && !path.startsWith(rootPath)) {
        throw IllegalStateException("Backup path is invalid.")
    }
}

private fun GradeTrackerAppState.allStoredNotes(): List<StoredNote> {
    return subjects.flatMap { subject ->
        subject.notes + subject.subSubjects.flatMap { subSubject -> subSubject.notes }
    }
}

private inline fun GradeTrackerAppState.mapStoredNotes(
    transform: (StoredNote) -> StoredNote
): GradeTrackerAppState {
    return copy(
        subjects = subjects.map { subject ->
            subject.copy(
                notes = subject.notes.map(transform),
                subSubjects = subject.subSubjects.map { subSubject ->
                    subSubject.copy(notes = subSubject.notes.map(transform))
                }
            )
        }
    )
}
