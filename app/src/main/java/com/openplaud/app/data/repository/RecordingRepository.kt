package com.openplaud.app.data.repository

import com.openplaud.app.data.api.OpenPlaudApi
import com.openplaud.app.data.model.V1Recording
import com.openplaud.app.data.model.V1RecordingDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val api: OpenPlaudApi
) {
    suspend fun getRecordings(limit: Int = 50, cursor: String? = null): Result<V1RecordingListPage> {
        return try {
            val response = api.getRecordings(limit = limit, cursor = cursor)
            if (response.isSuccessful) {
                val body = response.body()!!
                Result.success(
                    V1RecordingListPage(
                        recordings = body.data,
                        nextCursor = body.nextCursor,
                        hasMore = body.hasMore
                    )
                )
            } else {
                Result.failure(Exception("API error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecordingDetail(id: String): Result<V1RecordingDetail> {
        return try {
            val response = api.getRecordingDetail(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncRecordings(): Result<Int> {
        return try {
            val response = api.syncRecordings()
            if (response.isSuccessful) {
                val body = response.body()!!
                Result.success(body.newRecordings + body.updatedRecordings)
            } else {
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class V1RecordingListPage(
    val recordings: List<V1Recording>,
    val nextCursor: String?,
    val hasMore: Boolean
)
