package com.example.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Real Retrofit integration interface for Supabase DB API operations.
 */
interface SupabaseApiService {

    @POST("auth/v1/signup")
    suspend fun signUpWithEmail(
        @Header("apikey") apiKey: String,
        @Body request: Map<String, String>
    ): Response<ResponseBody>

    @POST("auth/v1/token?grant_type=password")
    suspend fun loginWithEmail(
        @Header("apikey") apiKey: String,
        @Body request: Map<String, String>
    ): Response<ResponseBody>

    @POST("auth/v1/recover")
    suspend fun recoverPassword(
        @Header("apikey") apiKey: String,
        @Body request: Map<String, String>
    ): Response<ResponseBody>

    @GET("rest/v1/profiles")
    suspend fun getUserProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("id") userId: String
    ): Response<ResponseBody>

    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("id") userId: String,
        @Body updates: Map<String, String>
    ): Response<ResponseBody>

    @GET("rest/v1/videos")
    suspend fun fetchVideos(
        @Header("apikey") apiKey: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<ResponseBody>

    @POST("rest/v1/videos")
    suspend fun createVideoMetadata(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body videoMetadata: Map<String, String>
    ): Response<ResponseBody>

    @GET("rest/v1/comments")
    suspend fun getComments(
        @Header("apikey") apiKey: String,
        @Query("video_id") videoId: String,
        @Query("select") select: String = "*,profiles(*)"
    ): Response<ResponseBody>

    @POST("rest/v1/comments")
    suspend fun postComment(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Body commentBody: Map<String, String>
    ): Response<ResponseBody>

    @DELETE("rest/v1/comments")
    suspend fun deleteComment(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("id") commentId: String
    ): Response<ResponseBody>
}

/**
 * Real Retrofit integration interface for Cloudflare R2 Uploading operations.
 */
interface CloudflareR2Service {

    @PUT("{path}")
    @Headers("Content-Type: application/octet-stream")
    suspend fun uploadMediaToR2(
        @Header("Authorization") authHeader: String,
        @Path("path") path: String,
        @Body fileBody: RequestBody
    ): Response<ResponseBody>
}
