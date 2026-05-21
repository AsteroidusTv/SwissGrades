package me.asteroidus.swissgrades.ui.app

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class StoredAttachment(
    val id: String,
    val filePath: String
)

data class DraftAttachment(
    val id: String,
    val filePath: String,
    val isPersisted: Boolean
)

data class PendingCameraCaptureRequest(
    val attachmentId: String,
    val outputUriString: String,
    val filePath: String
)

interface GradeAttachmentStorage {
    fun stageImportedAttachment(sourceUriString: String): DraftAttachment?

    fun createCameraCaptureRequest(): PendingCameraCaptureRequest

    fun finalizeCameraCapture(request: PendingCameraCaptureRequest, success: Boolean): DraftAttachment?

    fun commitAttachments(noteId: String, attachments: List<DraftAttachment>): List<StoredAttachment>

    fun discardNewAttachments(attachments: List<DraftAttachment>)

    fun deleteStoredAttachments(attachments: List<StoredAttachment>)
}

class LocalGradeAttachmentStorage(
    private val context: Context
) : GradeAttachmentStorage {
    private val stagedDirectory = File(context.filesDir, "attachments/staged")
    private val notesDirectory = File(context.filesDir, "attachments/notes")

    override fun stageImportedAttachment(sourceUriString: String): DraftAttachment? {
        val sourceUri = Uri.parse(sourceUriString)
        val attachmentId = nextAttachmentId()
        val extension = resolveFileExtension(sourceUri) ?: "jpg"
        val targetFile = createStagedFile(attachmentId, extension)
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null
            DraftAttachment(
                id = attachmentId,
                filePath = targetFile.absolutePath,
                isPersisted = false
            )
        } catch (_: Exception) {
            targetFile.delete()
            null
        }
    }

    override fun createCameraCaptureRequest(): PendingCameraCaptureRequest {
        val attachmentId = nextAttachmentId()
        val targetFile = createStagedFile(attachmentId, "jpg")
        val authority = "${context.packageName}.fileprovider"
        val outputUri = FileProvider.getUriForFile(context, authority, targetFile)
        return PendingCameraCaptureRequest(
            attachmentId = attachmentId,
            outputUriString = outputUri.toString(),
            filePath = targetFile.absolutePath
        )
    }

    override fun finalizeCameraCapture(
        request: PendingCameraCaptureRequest,
        success: Boolean
    ): DraftAttachment? {
        val stagedFile = File(request.filePath)
        if (!success || !stagedFile.exists() || stagedFile.length() <= 0L) {
            stagedFile.delete()
            return null
        }
        return DraftAttachment(
            id = request.attachmentId,
            filePath = stagedFile.absolutePath,
            isPersisted = false
        )
    }

    override fun commitAttachments(
        noteId: String,
        attachments: List<DraftAttachment>
    ): List<StoredAttachment> {
        val noteDirectory = File(notesDirectory, noteId).apply { mkdirs() }
        return attachments.mapNotNull { attachment ->
            if (attachment.isPersisted) {
                StoredAttachment(id = attachment.id, filePath = attachment.filePath)
            } else {
                val sourceFile = File(attachment.filePath)
                if (!sourceFile.exists()) {
                    null
                } else {
                    val extension = sourceFile.extension.takeIf { it.isNotBlank() } ?: "jpg"
                    val targetFile = File(noteDirectory, "${attachment.id}.$extension")
                    sourceFile.copyTo(targetFile, overwrite = true)
                    sourceFile.delete()
                    StoredAttachment(
                        id = attachment.id,
                        filePath = targetFile.absolutePath
                    )
                }
            }
        }
    }

    override fun discardNewAttachments(attachments: List<DraftAttachment>) {
        attachments
            .filterNot { it.isPersisted }
            .forEach { File(it.filePath).delete() }
    }

    override fun deleteStoredAttachments(attachments: List<StoredAttachment>) {
        attachments.forEach { attachment ->
            File(attachment.filePath).delete()
        }
    }

    private fun resolveFileExtension(uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.let { return it }
        }
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString()).takeIf { it.isNotBlank() }
    }

    private fun createStagedFile(attachmentId: String, extension: String): File {
        stagedDirectory.mkdirs()
        return File(stagedDirectory, "$attachmentId.$extension")
    }

    private fun nextAttachmentId(): String = "attachment-${UUID.randomUUID()}"
}

object NoOpGradeAttachmentStorage : GradeAttachmentStorage {
    override fun stageImportedAttachment(sourceUriString: String): DraftAttachment? = null

    override fun createCameraCaptureRequest(): PendingCameraCaptureRequest {
        return PendingCameraCaptureRequest(
            attachmentId = "attachment-noop",
            outputUriString = "",
            filePath = ""
        )
    }

    override fun finalizeCameraCapture(
        request: PendingCameraCaptureRequest,
        success: Boolean
    ): DraftAttachment? = null

    override fun commitAttachments(noteId: String, attachments: List<DraftAttachment>): List<StoredAttachment> {
        return attachments.map { StoredAttachment(id = it.id, filePath = it.filePath) }
    }

    override fun discardNewAttachments(attachments: List<DraftAttachment>) = Unit

    override fun deleteStoredAttachments(attachments: List<StoredAttachment>) = Unit
}
