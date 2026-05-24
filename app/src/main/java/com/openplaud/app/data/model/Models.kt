package com.openplaud.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class V1RecordingList(
    @Json(name = "data") val data: List<V1Recording>,
    @Json(name = "next_cursor") val nextCursor: String?,
    @Json(name = "has_more") val hasMore: Boolean
)

@JsonClass(generateAdapter = true)
data class V1Recording(
    val id: String,
    val title: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "recorded_at") val recordedAt: String,
    @Json(name = "duration_ms") val durationMs: Long,
    @Json(name = "filesize_bytes") val filesizeBytes: Long,
    val device: V1Device?,
    @Json(name = "has_transcription") val hasTranscription: Boolean,
    @Json(name = "has_summary") val hasSummary: Boolean,
    val links: V1Links
)

@JsonClass(generateAdapter = true)
data class V1RecordingDetail(
    val id: String,
    val title: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "recorded_at") val recordedAt: String,
    @Json(name = "duration_ms") val durationMs: Long,
    @Json(name = "filesize_bytes") val filesizeBytes: Long,
    val device: V1Device?,
    @Json(name = "has_transcription") val hasTranscription: Boolean,
    @Json(name = "has_summary") val hasSummary: Boolean,
    val links: V1Links,
    val transcript: V1Transcript?,
    val summary: V1Summary?
)

@JsonClass(generateAdapter = true)
data class V1Device(
    @Json(name = "serial_number") val serialNumber: String,
    val name: String?,
    val model: String?
)

@JsonClass(generateAdapter = true)
data class V1Links(
    val self: String,
    val transcript: String,
    val audio: String
)

@JsonClass(generateAdapter = true)
data class V1Transcript(
    val language: String?,
    val text: String,
    val provider: String,
    val model: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class V1Summary(
    val text: String?,
    @Json(name = "action_items") val actionItems: List<String>?,
    @Json(name = "key_points") val keyPoints: List<String>?,
    val provider: String,
    val model: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class PlaudConnection(
    val connected: Boolean,
    val server: String? = null,
    @Json(name = "plaudEmail") val plaudEmail: String? = null
)

@JsonClass(generateAdapter = true)
data class SyncResult(
    val success: Boolean,
    @Json(name = "newRecordings") val newRecordings: Int,
    @Json(name = "updatedRecordings") val updatedRecordings: Int,
    val errors: List<String>?,
    @Json(name = "inProgress") val inProgress: Boolean
)
