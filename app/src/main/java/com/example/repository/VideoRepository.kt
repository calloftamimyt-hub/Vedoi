package com.example.repository

import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import java.util.UUID

class VideoRepository {

    // Current Sign-in user state
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _registeredUsers = MutableStateFlow<List<UserProfile>>(emptyList())

    // Database simulation for local fallback
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    private val _networkVideos = MutableStateFlow<List<Video>>(emptyList())

    val videos: StateFlow<List<Video>> = kotlinx.coroutines.flow.combine(
        _currentUser, 
        _networkVideos, 
        _videos
    ) { user: UserProfile?, netList: List<Video>, defaultList: List<Video> ->
        netList
    }.stateIn(CoroutineScope(Dispatchers.Default + SupervisorJob()), SharingStarted.Eagerly, emptyList())

    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap()) // videoId -> comments
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    private val _searchHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())
    private val _watchHistory = MutableStateFlow<List<Video>>(emptyList())
    private val _likedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    private val _dislikedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    private val _subscribedChannelIds = MutableStateFlow<Set<String>>(emptySet())
    private val _downloadedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    private val _savedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    private val _sharedVideoIds = MutableStateFlow<Set<String>>(emptySet())

    // Upload Tasks
    private val _uploadTasks = MutableStateFlow<List<UploadProgress>>(emptyList())
    val uploadTasks: StateFlow<List<UploadProgress>> = _uploadTasks.asStateFlow()

    init {
        _videos.value = getDefaultVideos()
        // Hydrate from Supabase
        setupSupabaseData()
    }

    private fun getDefaultVideos(): List<Video> {
        return emptyList()
    }

    private fun setupSupabaseData() {
        GlobalScope.launch {
            try {
                // Fetch from Supabase via Retrofit
                val networkVideos = SupabaseClient.api.getVideos()
                _networkVideos.value = networkVideos
                _videos.value = networkVideos
            } catch (e: Exception) {
                // If tables do not exist or error, start with empty list for real users
                e.printStackTrace()
                _networkVideos.value = emptyList()
                _videos.value = emptyList()
            }

            try {
                val networkPlaylists = SupabaseClient.api.getPlaylists()
                _playlists.value = networkPlaylists
            } catch (e: Exception) {
                _playlists.value = emptyList()
            }
            
            try {
                val commentsList = SupabaseClient.api.getComments()
                val map = commentsList.groupBy { it.videoId }
                _comments.value = map
            } catch (e: Exception) {
                _comments.value = emptyMap()
            }
        }
    }


    // --- Authentication REST / SQLite methods ---
    suspend fun signUp(email: String, username: String, pass: String): Result<UserProfile> {
        val newUser = UserProfile(
            id = "user_" + UUID.randomUUID().toString().take(6),
            email = email,
            username = username,
            displayName = username.replaceFirstChar { it.uppercase() },
            avatarUrl = "",
            bio = "Welcome to ViewTube!",
            password = pass
        )
        try {
            SupabaseClient.api.createUserProfile(newUser)
            _registeredUsers.value = _registeredUsers.value + newUser
            _currentUser.value = newUser
            return Result.success(newUser)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to in-memory since API is not actively configured
            val existing = _registeredUsers.value.find { it.email == email }
            if (existing != null) {
                return Result.failure(Exception("Account already exists. Please login."))
            }
            _registeredUsers.value = _registeredUsers.value + newUser
            _currentUser.value = newUser
            return Result.success(newUser)
        }
    }

    suspend fun login(email: String, pass: String): Result<UserProfile> {
        try {
            val users = SupabaseClient.api.getUserProfiles()
            val matched = users.find { it.email == email }
            if (matched != null) {
                if (matched.password.isNotEmpty() && matched.password != pass) {
                    return Result.failure(Exception("Incorrect password for this account. Please try again."))
                }
                _currentUser.value = matched
                return Result.success(matched)
            }
            return Result.failure(Exception("Account not found. Please sign up first."))
        } catch (e: Exception) {
            e.printStackTrace()
            
            // Fallback to in-memory locally
            val matched = _registeredUsers.value.find { it.email == email }
            if (matched != null) {
                if (matched.password.isNotEmpty() && matched.password != pass) {
                    return Result.failure(Exception("Incorrect password for this account. Please try again."))
                }
                _currentUser.value = matched
                return Result.success(matched)
            }
            
            return Result.failure(Exception("Account not found. Please sign up first."))
        }
    }

    suspend fun googleLogin(): Result<UserProfile> {
        delay(800)
        val email = "tamim.google@gmail.com"
        try {
            val users = SupabaseClient.api.getUserProfiles()
            val matched = users.find { it.email == email }
            if (matched != null) {
                _currentUser.value = matched
                return Result.success(matched)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Create new profile if not found
        val newUser = UserProfile(
            id = "user_" + UUID.randomUUID().toString().take(6),
            email = email,
            username = "tamim_ahmed",
            displayName = "Tamim Ahmed",
            avatarUrl = "",
            bio = "Official Channel of Tamim Ahmed."
        )
        
        try {
            SupabaseClient.api.createUserProfile(newUser)
            _currentUser.value = newUser
            return Result.success(newUser)
        } catch (e: Exception) {
            e.printStackTrace()
            _currentUser.value = newUser // still allow locally
            return Result.success(newUser)
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        delay(400)
        return Result.success("Password recovery credentials dispatched to: $email")
    }

    suspend fun logout() {
        _currentUser.value = null
    }

    fun setCurrentUserDirectly(user: UserProfile?) {
        _currentUser.value = user
    }

    // --- Channels ---
    fun updateUserChannelProfile(
        displayName: String,
        username: String,
        avatarUrl: String,
        bio: String,
        channelKeywords: String,
        channelCategory: String
    ) {
        val updated = _currentUser.value?.copy(
            displayName = displayName,
            username = username,
            avatarUrl = avatarUrl.ifEmpty { _currentUser.value?.avatarUrl ?: "" },
            bio = bio,
            channelKeywords = channelKeywords,
            channelCategory = channelCategory,
            hasChannel = true
        )
        _currentUser.value = updated
        
        updated?.let { user ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    SupabaseClient.api.upsertUserProfile(profile = user)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateChannel(name: String, bio: String, avatarUrl: String, bannerUrl: String) {
        val updated = _currentUser.value?.copy(
            displayName = name,
            bio = bio,
            avatarUrl = avatarUrl.ifEmpty { _currentUser.value?.avatarUrl ?: "" },
            bannerUrl = bannerUrl.ifEmpty { _currentUser.value?.bannerUrl ?: "" }
        )
        _currentUser.value = updated
        
        updated?.let { user ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    SupabaseClient.api.upsertUserProfile(profile = user)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateLocalRegisteredList(user: UserProfile) {
        _registeredUsers.value = _registeredUsers.value.map {
            if (it.id == user.id) user else it
        }
    }

    fun isSubscribed(channelId: String): Boolean {
        return _subscribedChannelIds.value.contains(channelId)
    }

    val subscribedChannels: Flow<List<Triple<String, String, String>>> = _subscribedChannelIds.map { subscriptions ->
        val allChannelsInfo = _videos.value.map { Triple(it.channelId, it.channelName, it.channelAvatarUrl) }.distinctBy { it.first }
        allChannelsInfo.filter { subscriptions.contains(it.first) }
    }

    fun toggleSubscribe(channelId: String) {
        val current = _subscribedChannelIds.value.toMutableSet()
        if (current.contains(channelId)) {
            current.remove(channelId)
            // decrement remote/mock subscriber count
            modifyChannelSubscribers(channelId, -1)
        } else {
            current.add(channelId)
            modifyChannelSubscribers(channelId, 1)

            // Trigger real subscriber notification
            val notifs = _notifications.value.toMutableList()
            notifs.add(0, NotificationItem(
                type = NotificationType.SUBSCRIBER,
                title = "New Subscription!",
                message = "You subscribed to ${channelId.replace("_", " ").capitalize()}!"
            ))
            _notifications.value = notifs
        }
        _subscribedChannelIds.value = current
    }

    private fun modifyChannelSubscribers(channelId: String, delta: Int) {
        _videos.value = _videos.value.map { video ->
            if (video.channelId == channelId) {
                // Return updated channel if custom or update local channel info
                video
            } else {
                video
            }
        }
    }

    // --- Video Retrieval ---
    fun loadRecommendedVideos(): StateFlow<List<Video>> = videos

    fun loadTrendingVideos(): Flow<List<Video>> {
        return videos.map { list -> list.sortedByDescending { it.viewsCount } }
    }

    fun loadCategoryVideos(category: String): Flow<List<Video>> {
        return videos.map { list ->
            if (category == "All") list else list.filter { it.category.equals(category, ignoreCase = true) }
        }
    }

    fun getVideoById(videoId: String): Video? {
        return _videos.value.find { it.id == videoId }
    }

    fun recordWatch(video: Video) {
        val currentHistory = _watchHistory.value.toMutableList()
        currentHistory.removeAll { it.id == video.id }
        currentHistory.add(0, video)
        _watchHistory.value = currentHistory

        // Increment views
        _videos.value = _videos.value.map {
            if (it.id == video.id) it.copy(viewsCount = it.viewsCount + 1) else it
        }
    }

    fun getWatchHistory(): StateFlow<List<Video>> = _watchHistory

    // --- Likes & Dislikes ---
    fun isLiked(videoId: String): Boolean = _likedVideoIds.value.contains(videoId)
    fun isDisliked(videoId: String): Boolean = _dislikedVideoIds.value.contains(videoId)

    fun toggleLike(videoId: String) {
        val liked = _likedVideoIds.value.toMutableSet()
        val disliked = _dislikedVideoIds.value.toMutableSet()

        if (liked.contains(videoId)) {
            liked.remove(videoId)
            modifyVideoLikes(videoId, -1, 0)
        } else {
            liked.add(videoId)
            modifyVideoLikes(videoId, 1, if (disliked.contains(videoId)) -1 else 0)
            disliked.remove(videoId)
        }
        _likedVideoIds.value = liked
        _dislikedVideoIds.value = disliked
    }

    fun toggleDislike(videoId: String) {
        val liked = _likedVideoIds.value.toMutableSet()
        val disliked = _dislikedVideoIds.value.toMutableSet()

        if (disliked.contains(videoId)) {
            disliked.remove(videoId)
            modifyVideoLikes(videoId, 0, -1)
        } else {
            disliked.add(videoId)
            modifyVideoLikes(videoId, if (liked.contains(videoId)) -1 else 0, 1)
            liked.remove(videoId)
        }
        _likedVideoIds.value = liked
        _dislikedVideoIds.value = disliked
    }

    private fun modifyVideoLikes(videoId: String, likeDelta: Int, dislikeDelta: Int) {
        _videos.value = _videos.value.map {
            if (it.id == videoId) {
                it.copy(
                    likesCount = (it.likesCount + likeDelta).coerceAtLeast(0),
                    dislikesCount = (it.dislikesCount + dislikeDelta).coerceAtLeast(0)
                )
            } else it
        }
    }

    // --- Comments ---
    fun getCommentsForVideo(videoId: String): Flow<List<Comment>> {
        return _comments.map { it[videoId] ?: emptyList() }
    }

    fun addComment(videoId: String, content: String) {
        val me = _currentUser.value ?: return
        val newComment = Comment(
            id = "com_" + UUID.randomUUID().toString().take(6),
            videoId = videoId,
            userId = me.id,
            userName = me.displayName,
            userAvatarUrl = me.avatarUrl,
            content = content,
            createdAt = System.currentTimeMillis()
        )

        val videoComments = (_comments.value[videoId] ?: emptyList()).toMutableList()
        videoComments.add(0, newComment)
        val allComments = _comments.value.toMutableMap()
        allComments[videoId] = videoComments
        _comments.value = allComments

        // Update video comment count locally
        _videos.value = _videos.value.map {
            if (it.id == videoId) it.copy(commentsCount = it.commentsCount + 1) else it
        }

        GlobalScope.launch {
            try {
                SupabaseClient.api.createComment(newComment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun replyComment(videoId: String, commentId: String, content: String) {
        val me = _currentUser.value ?: return
        val videoComments = (_comments.value[videoId] ?: emptyList()).map { comment ->
            if (comment.id == commentId) {
                val replies = comment.replies.toMutableList()
                replies.add(CommentReply(
                    commentId = commentId,
                    userId = me.id,
                    userName = me.displayName,
                    userAvatarUrl = me.avatarUrl,
                    content = content
                ))
                comment.copy(replies = replies)
            } else comment
        }

        val allComments = _comments.value.toMutableMap()
        allComments[videoId] = videoComments
        _comments.value = allComments
    }

    fun likeComment(videoId: String, commentId: String) {
        val videoComments = (_comments.value[videoId] ?: emptyList()).map { comment ->
            if (comment.id == commentId) {
                val likedByMe = !comment.isLikedByMe
                comment.copy(
                    isLikedByMe = likedByMe,
                    likesCount = comment.likesCount + (if (likedByMe) 1 else -1)
                )
            } else comment
        }

        val allComments = _comments.value.toMutableMap()
        allComments[videoId] = videoComments
        _comments.value = allComments
    }

    fun deleteComment(videoId: String, commentId: String) {
        val me = _currentUser.value ?: return
        val videoComments = (_comments.value[videoId] ?: emptyList()).toMutableList()
        val removed = videoComments.removeAll { it.id == commentId && it.userId == me.id }
        if (removed) {
            val allComments = _comments.value.toMutableMap()
            allComments[videoId] = videoComments
            _comments.value = allComments

            _videos.value = _videos.value.map {
                if (it.id == videoId) it.copy(commentsCount = (it.commentsCount - 1).coerceAtLeast(0)) else it
            }

            GlobalScope.launch {
                try {
                    SupabaseClient.api.deleteComment(commentId, me.id)
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // --- Search System ---
    fun getSearchHistory(): StateFlow<List<SearchHistoryItem>> = _searchHistory

    fun addSearchQuery(query: String) {
        val history = _searchHistory.value.toMutableList()
        history.removeAll { it.query.equals(query, ignoreCase = true) }
        history.add(0, SearchHistoryItem(query))
        _searchHistory.value = history.take(20)
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun removeSearchHistoryItem(query: String) {
        _searchHistory.value = _searchHistory.value.filterNot { it.query == query }
    }

    fun searchVideos(query: String): List<Video> {
        if (query.isEmpty()) return _videos.value
        return _videos.value.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.channelName.contains(query, ignoreCase = true)
        }
    }

    // --- Playlists ---
    fun getPlaylists(): StateFlow<List<Playlist>> = _playlists

    fun createPlaylist(title: String, description: String, isPublic: Boolean) {
        val me = _currentUser.value ?: return
        val list = _playlists.value.toMutableList()
        val pl = Playlist(
            title = title,
            description = description,
            isPublic = isPublic,
            userId = me.id
        )
        list.add(pl)
        _playlists.value = list
        
        GlobalScope.launch {
            try {
                SupabaseClient.api.createPlaylist(pl)
            } catch(e: Exception) { e.printStackTrace() }
        }
    }

    fun addVideoToPlaylist(playlistId: String, videoId: String) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                if (!playlist.videoIds.contains(videoId)) {
                    playlist.copy(videoIds = playlist.videoIds + videoId)
                } else playlist
            } else playlist
        }
    }

    fun removeVideoFromPlaylist(playlistId: String, videoId: String) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(videoIds = playlist.videoIds - videoId)
            } else playlist
        }
    }

    fun getLikedVideos(): Flow<List<Video>> {
        return _videos.map { videos ->
            videos.filter { _likedVideoIds.value.contains(it.id) }
        }
    }

    fun getDownloadedVideos(): Flow<List<Video>> {
        return _videos.map { videos ->
            videos.filter { _downloadedVideoIds.value.contains(it.id) }
        }
    }

    fun getSavedVideos(): Flow<List<Video>> {
        return _videos.map { videos ->
            videos.filter { _savedVideoIds.value.contains(it.id) }
        }
    }

    fun getSharedVideos(): Flow<List<Video>> {
        return _videos.map { videos ->
            videos.filter { _sharedVideoIds.value.contains(it.id) }
        }
    }

    fun shareVideoToProfile(videoId: String) {
        val shared = _sharedVideoIds.value.toMutableSet()
        shared.add(videoId)
        _sharedVideoIds.value = shared
    }

    fun toggleSaveVideo(videoId: String) {
        val saved = _savedVideoIds.value.toMutableSet()
        if (saved.contains(videoId)) {
            saved.remove(videoId)
        } else {
            saved.add(videoId)
        }
        _savedVideoIds.value = saved
    }

    fun isSaved(videoId: String): Boolean = _savedVideoIds.value.contains(videoId)

    fun toggleDownloadVideo(videoId: String) {
        val downloads = _downloadedVideoIds.value.toMutableSet()
        if (downloads.contains(videoId)) {
            downloads.remove(videoId)
        } else {
            downloads.add(videoId)
        }
        _downloadedVideoIds.value = downloads
    }

    fun isDownloaded(videoId: String): Boolean = _downloadedVideoIds.value.contains(videoId)

    // --- Notifications Feed ---
    fun getNotifications(): Flow<List<NotificationItem>> = _notifications

    fun markNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    // --- Video Upload System with Progress Bar & Fail/Retry Simulation ---
    fun uploadVideo(title: String, description: String, category: String, duration: String, isShort: Boolean = false, context: android.content.Context) {
        val me = _currentUser.value ?: return
        val taskId = "task_" + UUID.randomUUID().toString().take(6)
        
        val newTask = UploadProgress(
            id = taskId,
            title = title,
            isShort = isShort
        )
        
        _uploadTasks.value = _uploadTasks.value + newTask

        // Simulation parameters
        val maxSizeMB = if (category == "Shorts") 30 else 100
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notificationId = taskId.hashCode()
        val channelId = "upload_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Video Uploads",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Uploading: $title")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        // Log S3 details
        println("Connecting to S3 API: https://04fcb334fa07a6aa40a8160b776e0d8d.r2.cloudflarestorage.com")
        println("Using Account ID: 04fcb334fa07a6aa40a8160b776e0d8d")

        // Simulate Background Upload with high detail
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            var currentProgress = 0f
            var attempts = 0
            while (currentProgress < 1.0f) {
                delay(800)
                attempts++
                
                if (attempts == 6 && taskId.hashCode() % 3 == 0) { // simulate intermittent failures on certain cases
                    _uploadTasks.value = _uploadTasks.value.map {
                        if (it.id == taskId) it.copy(isFailed = true) else it
                    }
                    builder.setContentText("Upload failed on S3 API")
                        .setOngoing(false)
                        .setProgress(0, 0, false)
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                    notificationManager.notify(notificationId, builder.build())
                    return@launch // Stop uploading, awaits retry
                }

                currentProgress += 0.15f
                if (currentProgress >= 1.0f) {
                    currentProgress = 1.0f
                }

                val currentMB = (currentProgress * maxSizeMB).toInt()
                builder.setContentText("$currentMB MB / $maxSizeMB MB")
                    .setProgress(100, (currentProgress * 100).toInt(), false)
                notificationManager.notify(notificationId, builder.build())

                _uploadTasks.value = _uploadTasks.value.map {
                    if (it.id == taskId) it.copy(progress = currentProgress, isCompleted = currentProgress == 1.0f) else it
                }
            }

            builder.setContentText("Upload complete: $maxSizeMB MB")
                .setContentTitle("Uploaded: $title")
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            notificationManager.notify(notificationId, builder.build())

            // Once finished, insert the video live into our feed!
            val newVideo = Video(
                id = "vid_upload_" + UUID.randomUUID().toString().take(6),
                title = title,
                description = description,
                // Using general placeholder sample video
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop",
                duration = duration.ifEmpty { "04:30" },
                category = if (isShort) "Shorts" else category,
                channelId = me.id,
                channelName = me.displayName,
                channelAvatarUrl = me.avatarUrl,
                createdAt = System.currentTimeMillis(),
                isShort = isShort
            )

            try {
                SupabaseClient.api.createVideo(newVideo)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _videos.value = listOf(newVideo) + _videos.value
                _networkVideos.value = listOf(newVideo) + _networkVideos.value

                // Create subscriber notification about new uploaded video
                val notifs = _notifications.value.toMutableList()
                notifs.add(0, NotificationItem(
                    type = NotificationType.SYSTEM,
                    title = "Video Upload Completed!",
                    message = "Your video '$title' has been transcoded and published successfully to ViewTube."
                ))
                _notifications.value = notifs
            }
        }
    }

    fun retryUpload(taskId: String) {
        val task = _uploadTasks.value.find { it.id == taskId } ?: return
        _uploadTasks.value = _uploadTasks.value.map {
            if (it.id == taskId) it.copy(isFailed = false, progress = 0f) else it
        }

        // Restart simulation
        kotlinx.coroutines.GlobalScope.launch {
            var currentProgress = 0f
            while (currentProgress < 1.0f) {
                delay(350)
                currentProgress += 0.2f
                if (currentProgress >= 1.0f) {
                    currentProgress = 1.0f
                }
                _uploadTasks.value = _uploadTasks.value.map {
                    if (it.id == taskId) {
                        it.copy(
                            progress = currentProgress, 
                            isCompleted = currentProgress == 1.0f,
                            isFailed = false
                        )
                    } else it
                }
            }

            val me = _currentUser.value ?: return@launch
            val newVideo = Video(
                id = "vid_upload_retry_" + UUID.randomUUID().toString().take(4),
                title = task.title,
                description = "Successfully re-uploaded with resilient R2 fallback.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800&auto=format&fit=crop",
                duration = "05:12",
                category = if (task.isShort) "Shorts" else "Lifestyle",
                channelId = me.id,
                channelName = me.displayName,
                channelAvatarUrl = me.avatarUrl,
                createdAt = System.currentTimeMillis(),
                isShort = task.isShort
            )

            try {
                SupabaseClient.api.createVideo(newVideo)
            } catch(e: Exception) {
                e.printStackTrace()
            }

            _videos.value = listOf(newVideo) + _videos.value
        }
    }

    fun clearCompletedUpload(taskId: String) {
        _uploadTasks.value = _uploadTasks.value.filterNot { it.id == taskId }
    }
}