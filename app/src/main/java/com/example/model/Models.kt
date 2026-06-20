package com.example.model

import java.util.UUID

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val bannerUrl: String = "",
    val subscribersCount: Int = 0,
    val bio: String = "No bio yet.",
    val createdAt: Long = System.currentTimeMillis(),
    val hasChannel: Boolean = false,
    val channelCategory: String = "",
    val channelKeywords: String = ""
)

data class Video(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val duration: String,
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val dislikesCount: Int = 0,
    val commentsCount: Int = 0,
    val category: String = "All",
    val channelId: String,
    val channelName: String,
    val channelAvatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isShort: Boolean = false
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val videoId: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String = "",
    val content: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val replies: List<CommentReply> = emptyList()
)

data class CommentReply(
    val id: String = UUID.randomUUID().toString(),
    val commentId: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String = "",
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Playlist(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isPublic: Boolean = true,
    val userId: String,
    val videoIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val relatedVideoId: String? = null
)

enum class NotificationType {
    SUBSCRIBER, COMMENT, LIKE, SYSTEM, NEW_VIDEO
}

data class SearchHistoryItem(
    val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)

data class UploadProgress(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val progress: Float = 0f,
    val isFailed: Boolean = false,
    val isCompleted: Boolean = false,
    val videoLocalPath: String = "",
    val thumbnailLocalPath: String = ""
)
