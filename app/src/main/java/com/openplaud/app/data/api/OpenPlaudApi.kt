package com.openplaud.app.data.api

import com.openplaud.app.data.model.PlaudConnection
import com.openplaud.app.data.model.SyncResult
import com.openplaud.app.data.model.V1RecordingDetail
import com.openplaud.app.data.model.V1RecordingList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenPlaudApi {

    // --- V1 API (API Key auth) ---

    @GET("api/v1/recordings")
    suspend fun getRecordings(
        @Query("limit") limit: Int = 50,
        @Query("cursor") cursor: String? = null,
        @Query("has_transcription") hasTranscription: Boolean? = null
    ): Response<V1RecordingList>

    @GET("api/v1/recordings/{id}")
    suspend fun getRecordingDetail(
        @Path("id") id: String
    ): Response<V1RecordingDetail>

    // --- Plaud Sync (session auth required) ---
    // These endpoints need session cookies; use via WebView sign-in

    @GET("api/plaud/connection")
    suspend fun getPlaudConnection(): Response<PlaudConnection>

    @POST("api/plaud/sync")
    suspend fun syncRecordings(): Response<SyncResult>
}
