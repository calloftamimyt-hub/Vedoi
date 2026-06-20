package com.example.repository

import com.example.model.Video
import com.example.model.UserProfile
import com.example.model.Comment
import com.example.model.Playlist
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.DELETE

interface SupabaseApi {
    @GET("videos?select=*")
    suspend fun getVideos(): List<Video>

    @POST("videos")
    suspend fun createVideo(@Body video: Video)

    @GET("user_profiles?select=*")
    suspend fun getUserProfiles(): List<UserProfile>

    @POST("user_profiles")
    suspend fun createUserProfile(@Body profile: UserProfile)

    @POST("user_profiles")
    suspend fun upsertUserProfile(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body profile: UserProfile
    )

    @GET("comments?select=*")
    suspend fun getComments(): List<Comment>

    @POST("comments")
    suspend fun createComment(@Body comment: Comment)

    @DELETE("comments")
    suspend fun deleteComment(@Query("id") id: String, @Query("userId") userId: String)
    
    @GET("playlists?select=*")
    suspend fun getPlaylists(): List<Playlist>
    
    @POST("playlists")
    suspend fun createPlaylist(@Body playlist: Playlist)
}
