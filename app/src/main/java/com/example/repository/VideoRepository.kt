package com.example.repository

import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class VideoRepository {

    // Current Sign-in user state
    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "user_me",
            email = "its.me.calloftanjil@gmail.com",
            username = "calloftanjil",
            displayName = "Tanjil Ahmed",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop",
            bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop",
            subscribersCount = 1420,
            bio = "Official Channel of Tanjil. Building professional apps and high-quality software."
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Database simulation for local fallback
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap()) // videoId -> comments
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    private val _searchHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())
    private val _watchHistory = MutableStateFlow<List<Video>>(emptyList())
    private val _likedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    private val _dislikedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    private val _subscribedChannelIds = MutableStateFlow<Set<String>>(emptySet())
    private val _downloadedVideoIds = MutableStateFlow<Set<String>>(emptySet())

    // Upload Tasks
    private val _uploadTasks = MutableStateFlow<List<UploadProgress>>(emptyList())
    val uploadTasks: StateFlow<List<UploadProgress>> = _uploadTasks.asStateFlow()

    init {
        // Hydrate demo content
        setupDemoData()
    }

    private fun setupDemoData() {
        val channels = listOf(
            Triple("dev_android", "Google Android", "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=150&auto=format&fit=crop"),
            Triple("cosmic_voyager", "Cosmic Voyager", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=150&auto=format&fit=crop"),
            Triple("synth_beats", "Lofi Synth Records", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop"),
            Triple("chef_alicia", "Bite Size Cooking", "https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=150&auto=format&fit=crop"),
            Triple("fitness_flow", "FitLife Studio", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=150&auto=format&fit=crop")
        )

        val demoVideos = listOf(
            Video(
                id = "vid_1",
                title = "Jetpack Compose M3 Layouts: Complete Modern UI Building Guide",
                description = "Master Compose with Material Design 3 dynamic themes, surface depth tokens, overlapping canvas layouts, and stunning typography pairing. This complete guide provides professional-grade implementation steps.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?w=800&auto=format&fit=crop",
                duration = "14:25",
                viewsCount = 45290,
                likesCount = 3820,
                commentsCount = 4,
                category = "Coding",
                channelId = channels[0].first,
                channelName = channels[0].second,
                channelAvatarUrl = channels[0].third,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 12 // 12 hours ago
            ),
            Video(
                id = "vid_2",
                title = "Journey Through deep Space: Cosmic Dust and Interstellar Nebula HD",
                description = "Embark on an epic real-time animated exploration of distant galaxies and colorful gas clouds in outer space. Perfect background for deep thinking or atmospheric relaxation.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=800&auto=format&fit=crop",
                duration = "22:10",
                viewsCount = 120500,
                likesCount = 18450,
                commentsCount = 2,
                category = "Tech",
                channelId = channels[1].first,
                channelName = channels[1].second,
                channelAvatarUrl = channels[1].third,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3 // 3 days ago
            ),
            Video(
                id = "vid_3",
                title = "Lofi Synthwave Oasis: Ultimate Beats for Coding & Night Driving",
                description = "Unwind to curated synthetic textures, nostalgic retro wave elements, and warm analog basslines. The perfect visual companion for your focused late-night workflow.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1515462277126-270d878326e5?w=800&auto=format&fit=crop",
                duration = "52:45",
                viewsCount = 891000,
                likesCount = 65200,
                commentsCount = 3,
                category = "Music",
                channelId = channels[2].first,
                channelName = channels[2].second,
                channelAvatarUrl = channels[2].third,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7 // 7 days ago
            ),
            Video(
                id = "vid_4",
                title = "Perfect Golden Neapolitan Crust: Homemade Pizza Recipe",
                description = "Learn how to achieve restaurant-level leopard spotting on your pizza dough using a conventional home oven and pre-conditioned pizza stones. Tips on hydration levels and long proofing times.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop",
                duration = "08:15",
                viewsCount = 12400,
                likesCount = 980,
                commentsCount = 2,
                category = "Food",
                channelId = channels[3].first,
                channelName = channels[3].second,
                channelAvatarUrl = channels[3].third,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 4 // 4 hours ago
            ),
            Video(
                id = "vid_5",
                title = "15-Minute Full Body Calisthenics Routine (No Equipment Needed)",
                description = "No gym memberships required. A balanced interval framework engaging core, chest, arms, and legs. Includes progression triggers for beginners up to advanced practitioners.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800&auto=format&fit=crop",
                duration = "15:00",
                viewsCount = 239000,
                likesCount = 14300,
                commentsCount = 2,
                category = "Lifestyle",
                channelId = channels[4].first,
                channelName = channels[4].second,
                channelAvatarUrl = channels[4].third,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 10 // 10 days ago
            ),
            Video(
                id = "vid_short_1",
                title = "Wait for the drop... AMAZING Sunset reflection 🌅 #shorts",
                description = "A beautiful sunset reflection in the mountains.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1558442074-3c19857bc1dc?w=400&h=700&fit=crop",
                duration = "00:58",
                viewsCount = 890000,
                likesCount = 45000,
                commentsCount = 312,
                category = "Shorts",
                channelId = channels[1].first,
                channelName = channels[1].second,
                channelAvatarUrl = channels[1].third,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                isShort = true
            ),
            Video(
                id = "vid_short_2",
                title = "Quick tip for React devs! ⚛️ #coding #shorts",
                description = "Speed up your reactivity with useMemo.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400&h=700&fit=crop",
                duration = "00:45",
                viewsCount = 120000,
                likesCount = 12000,
                commentsCount = 74,
                category = "Shorts",
                channelId = channels[0].first,
                channelName = channels[0].second,
                channelAvatarUrl = channels[0].third,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 12,
                isShort = true
            )
        )

        _videos.value = demoVideos

        // Comments Hydration
        _comments.value = mapOf(
            "vid_1" to listOf(
                Comment(
                    id = "com_1_1",
                    videoId = "vid_1",
                    userId = "user_alicia",
                    userName = "Alicia Keys",
                    userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&auto=format&fit=crop",
                    content = "This was the single best breakdown of Jetpack Compose state control I've ever seen! Dynamic themeing feels so straightforward now.",
                    likesCount = 142,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 3
                ),
                Comment(
                    id = "com_1_2",
                    videoId = "vid_1",
                    userId = "user_jack",
                    userName = "Jack Reacher",
                    userAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&auto=format&fit=crop",
                    content = "Do you have a Github repo with these code snippets? I got stuck at the Window Insets safe drawing setup.",
                    likesCount = 38,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 1,
                    replies = listOf(
                        CommentReply(
                            id = "reply_1",
                            commentId = "com_1_2",
                            userId = "dev_android",
                            userName = "Google Android",
                            userAvatarUrl = channels[0].third,
                            content = "Yes! Check the video description, we added a link to the complete repository containing edge-to-edge custom solutions.",
                            createdAt = System.currentTimeMillis() - 1000 * 60 * 30
                        )
                    )
                ),
                Comment(
                    id = "com_1_3",
                    videoId = "vid_1",
                    userId = "user_me",
                    userName = "calloftanjil",
                    userAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop",
                    content = "Awesome content! Can we talk about building video players next?",
                    likesCount = 5,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 15
                )
            ),
            "vid_2" to listOf(
                Comment(
                    id = "com_2_1",
                    videoId = "vid_2",
                    userId = "synth_beats",
                    userName = "Lofi Synth Records",
                    userAvatarUrl = channels[2].third,
                    content = "The visuals match perfectly with our Synthwave tracks! Space visuals and ambient synths are made for each other.",
                    likesCount = 4220,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 20
                )
            )
        )

        // Demo Playlists
        _playlists.value = listOf(
            Playlist(
                id = "play_curated",
                title = "Study and Productivity Sync",
                description = "Chill retro tracks and tech exploration videos for late night study sessions.",
                isPublic = true,
                userId = "user_me",
                videoIds = listOf("vid_3", "vid_1")
            ),
            Playlist(
                id = "play_fitness",
                title = "Healthy Living & Workouts",
                description = "Unlisted fitness tracker notes and body calibration guides.",
                isPublic = false,
                userId = "user_me",
                videoIds = listOf("vid_5")
            )
        )

        // Notifications
        _notifications.value = listOf(
            NotificationItem(
                id = "notif_1",
                type = NotificationType.SUBSCRIBER,
                title = "New Subscriber!",
                message = "Dev_Android channel just subscribed to your channel.",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 30,
                relatedVideoId = null
            ),
            NotificationItem(
                id = "notif_2",
                type = NotificationType.COMMENT,
                title = "New Comment on your video",
                message = "Alicia Keys commented: 'Wow, excellent tutorial, keep it up!'",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                relatedVideoId = "vid_1"
            ),
            NotificationItem(
                id = "notif_3",
                type = NotificationType.LIKE,
                title = "Video Liked",
                message = "Your video 'Jetpack Compose M3 Layouts' reached 1,400 likes!",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                relatedVideoId = "vid_1"
            )
        )

        // Search History
        _searchHistory.value = listOf(
            SearchHistoryItem("Jetpack Compose tutorial"),
            SearchHistoryItem("Ambient music for coding"),
            SearchHistoryItem("How to bake Neapolitan pizza")
        )
    }

    // --- Authentication REST / SQLite methods ---
    suspend fun signUp(email: String, username: String, pass: String): Result<UserProfile> {
        delay(800) // Call Simulation
        val newUser = UserProfile(
            id = "user_" + UUID.randomUUID().toString().take(6),
            email = email,
            username = username,
            displayName = username.replaceFirstChar { it.uppercase() },
            avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&auto=format&fit=crop",
            bio = "Welcome to my new channel!"
        )
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    suspend fun login(email: String, pass: String): Result<UserProfile> {
        delay(600)
        val user = UserProfile(
            id = "user_me",
            email = email,
            username = email.substringBefore("@"),
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop",
            bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop",
            subscribersCount = 1420,
            bio = "Official Channel of Tanjil Ahmed."
        )
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun googleLogin(): Result<UserProfile> {
        delay(800)
        return login("its.me.calloftanjil@gmail.com", "google-oauthed")
    }

    suspend fun forgotPassword(email: String): Result<String> {
        delay(400)
        return Result.success("Password recovery credentials dispatched to: $email")
    }

    suspend fun logout() {
        _currentUser.value = null
    }

    // --- Channels ---
    fun updateChannel(name: String, bio: String, avatarUrl: String, bannerUrl: String) {
        _currentUser.value = _currentUser.value?.copy(
            displayName = name,
            bio = bio,
            avatarUrl = avatarUrl.ifEmpty { _currentUser.value?.avatarUrl ?: "" },
            bannerUrl = bannerUrl.ifEmpty { _currentUser.value?.bannerUrl ?: "" }
        )
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
        val videoComments = (_comments.value[videoId] ?: emptyList()).toMutableList()

        val newComment = Comment(
            id = "com_" + UUID.randomUUID().toString().take(6),
            videoId = videoId,
            userId = me.id,
            userName = me.displayName,
            userAvatarUrl = me.avatarUrl,
            content = content,
            createdAt = System.currentTimeMillis()
        )

        videoComments.add(0, newComment)
        val allComments = _comments.value.toMutableMap()
        allComments[videoId] = videoComments
        _comments.value = allComments

        // Update video comment count
        _videos.value = _videos.value.map {
            if (it.id == videoId) it.copy(commentsCount = it.commentsCount + 1) else it
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
        val videoComments = (_comments.value[videoId] ?: emptyList()).toMutableList()
        val removed = videoComments.removeAll { it.id == commentId && it.userId == (_currentUser.value?.id ?: "") }
        if (removed) {
            val allComments = _comments.value.toMutableMap()
            allComments[videoId] = videoComments
            _comments.value = allComments

            _videos.value = _videos.value.map {
                if (it.id == videoId) it.copy(commentsCount = (it.commentsCount - 1).coerceAtLeast(0)) else it
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
        list.add(Playlist(
            title = title,
            description = description,
            isPublic = isPublic,
            userId = me.id
        ))
        _playlists.value = list
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
    fun uploadVideo(title: String, description: String, category: String, duration: String) {
        val me = _currentUser.value ?: return
        val taskId = "task_" + UUID.randomUUID().toString().take(6)
        
        val newTask = UploadProgress(
            id = taskId,
            title = title
        )
        
        _uploadTasks.value = _uploadTasks.value + newTask

        // Simulate Background Upload with high detail
        kotlinx.coroutines.GlobalScope.launch {
            var currentProgress = 0f
            var attempts = 0
            while (currentProgress < 1.0f) {
                delay(400)
                attempts++
                // Randomly trigger a network failure on 1st attempt at 60% as user specified retry handling
                if (attempts == 6 && taskId.hashCode() % 3 == 0) { // simulate intermittent failures on certain cases
                    _uploadTasks.value = _uploadTasks.value.map {
                        if (it.id == taskId) it.copy(isFailed = true) else it
                    }
                    return@launch // Stop uploading, awaits retry
                }

                currentProgress += 0.15f
                if (currentProgress >= 1.0f) {
                    currentProgress = 1.0f
                }

                _uploadTasks.value = _uploadTasks.value.map {
                    if (it.id == taskId) it.copy(progress = currentProgress, isCompleted = currentProgress == 1.0f) else it
                }
            }

            // Once finished, insert the video live into our feed!
            val newVideo = Video(
                id = "vid_upload_" + UUID.randomUUID().toString().take(6),
                title = title,
                description = description,
                // Using general placeholder sample video
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop",
                duration = duration.ifEmpty { "04:30" },
                category = category,
                channelId = me.id,
                channelName = me.displayName,
                channelAvatarUrl = me.avatarUrl,
                createdAt = System.currentTimeMillis()
            )

            _videos.value = listOf(newVideo) + _videos.value

            // Create subscriber notification about new uploaded video
            val notifs = _notifications.value.toMutableList()
            notifs.add(0, NotificationItem(
                type = NotificationType.SYSTEM,
                title = "Video Upload Completed!",
                message = "Your video '$title' has been transcoded and published successfully."
            ))
            _notifications.value = notifs
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
                category = "Lifestyle",
                channelId = me.id,
                channelName = me.displayName,
                channelAvatarUrl = me.avatarUrl,
                createdAt = System.currentTimeMillis()
            )

            _videos.value = listOf(newVideo) + _videos.value
        }
    }

    fun clearCompletedUpload(taskId: String) {
        _uploadTasks.value = _uploadTasks.value.filterNot { it.id == taskId }
    }
}
