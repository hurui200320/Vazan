package info.skyblond.vazan.domain.repository

import info.skyblond.vazan.data.room.Label

interface LabelRepository {
    suspend fun getLabelById(labelId: String): Label?

    suspend fun insertOrUpdateLabel(label: Label)

    suspend fun deleteLabel(label: Label)

    suspend fun deleteLabelByStatus(status: Label.Status)

    suspend fun deleteOldLabelsByStatus(status: Label.Status, latestVersion: Long)
}