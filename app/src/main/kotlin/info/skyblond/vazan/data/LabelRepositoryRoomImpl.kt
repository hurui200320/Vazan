package info.skyblond.vazan.data

import info.skyblond.vazan.data.room.Label
import info.skyblond.vazan.data.room.LabelDao
import info.skyblond.vazan.domain.repository.LabelRepository
import javax.inject.Inject

class LabelRepositoryRoomImpl @Inject constructor(
    private val dao: LabelDao
) : LabelRepository {
    override suspend fun getLabelById(labelId: String): Label? = dao.getLabelById(labelId)

    override suspend fun insertOrUpdateLabel(label: Label) = dao.insertOrUpdateLabel(label)

    override suspend fun deleteLabel(label: Label) = dao.deleteLabel(label)

    override suspend fun deleteLabelByStatus(status: Label.Status) = dao.deleteLabelByStatus(status)

    override suspend fun deleteOldLabelsByStatus(status: Label.Status, latestVersion: Long) =
        dao.deleteOldLabelsByStatus(status, latestVersion)
}