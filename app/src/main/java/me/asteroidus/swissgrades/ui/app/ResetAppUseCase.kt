package me.asteroidus.swissgrades.ui.app

interface ResetAppUseCase {
    fun reset(): GradeTrackerAppState
}

class DefaultResetAppUseCase(
    private val attachmentStorage: GradeAttachmentStorage
) : ResetAppUseCase {

    override fun reset(): GradeTrackerAppState {
        attachmentStorage.deleteAllAttachments()
        return GradeTrackerAppState()
    }
}
