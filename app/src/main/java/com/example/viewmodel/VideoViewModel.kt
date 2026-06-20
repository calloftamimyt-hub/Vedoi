package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.repository.VideoRepository
import com.example.network.*
import com.example.BuildConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VideoViewModel(private val repository: VideoRepository = VideoRepository()) : ViewModel() {

    // Auth States
    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating = _isAuthenticating.asStateFlow()

    private val _hasSeenOnboarding = MutableStateFlow(false)
    val hasSeenOnboarding = _hasSeenOnboarding.asStateFlow()

    private var sharedPreferences: android.content.SharedPreferences? = null

    fun initPersistence(context: android.content.Context) {
        val prefs = context.applicationContext.getSharedPreferences("viewtube_prefs", android.content.Context.MODE_PRIVATE)
        sharedPreferences = prefs
        _hasSeenOnboarding.value = prefs.getBoolean("has_seen_onboarding", false)

        val userId = prefs.getString("user_id", null)
        val email = prefs.getString("user_email", null)
        if (userId != null && email != null) {
            val user = UserProfile(
                id = userId,
                email = email,
                username = prefs.getString("user_username", "") ?: "",
                displayName = prefs.getString("user_display_name", "") ?: "",
                avatarUrl = prefs.getString("user_avatar_url", "") ?: "",
                bannerUrl = prefs.getString("user_banner_url", "") ?: "",
                subscribersCount = prefs.getInt("user_subscribers_count", 0),
                bio = prefs.getString("user_bio", "Welcome to ViewTube!") ?: "Welcome to ViewTube!",
                createdAt = prefs.getLong("user_created_at", System.currentTimeMillis()),
                hasChannel = prefs.getBoolean("user_has_channel", false),
                channelCategory = prefs.getString("user_channel_category", "") ?: "",
                channelKeywords = prefs.getString("user_channel_keywords", "") ?: "",
                password = prefs.getString("user_password", "") ?: ""
            )
            repository.setCurrentUserDirectly(user)
            _hasSeenOnboarding.value = true
            prefs.edit().putBoolean("has_seen_onboarding", true).apply()
        }
    }

    fun completeOnboarding() {
        _hasSeenOnboarding.value = true
        sharedPreferences?.edit()?.putBoolean("has_seen_onboarding", true)?.apply()
    }

    private fun saveUserSessionToPrefs(user: UserProfile) {
        sharedPreferences?.edit()?.apply {
            putString("user_id", user.id)
            putString("user_email", user.email)
            putString("user_username", user.username)
            putString("user_display_name", user.displayName)
            putString("user_avatar_url", user.avatarUrl)
            putString("user_banner_url", user.bannerUrl)
            putInt("user_subscribers_count", user.subscribersCount)
            putString("user_bio", user.bio)
            putLong("user_created_at", user.createdAt)
            putBoolean("user_has_channel", user.hasChannel)
            putString("user_channel_category", user.channelCategory)
            putString("user_channel_keywords", user.channelKeywords)
            putString("user_password", user.password)
            apply()
        }
    }

    private fun clearUserSessionFromPrefs() {
        sharedPreferences?.edit()?.apply {
            remove("user_id")
            remove("user_email")
            remove("user_username")
            remove("user_display_name")
            remove("user_avatar_url")
            remove("user_banner_url")
            remove("user_subscribers_count")
            remove("user_bio")
            remove("user_created_at")
            remove("user_has_channel")
            remove("user_channel_category")
            remove("user_channel_keywords")
            remove("user_password")
            apply()
        }
    }

    // Screen States
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _suggestedQueries = MutableStateFlow<List<String>>(emptyList())
    val suggestedQueries = _suggestedQueries.asStateFlow()

    // Video Selection and Video Player Specifics
    private val _activeVideo = MutableStateFlow<Video?>(null)
    val activeVideo = _activeVideo.asStateFlow()

    private val _playbackSpeed = MutableStateFlow("1.0x")
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _playbackQuality = MutableStateFlow("Auto")
    val playbackQuality = _playbackQuality.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen = _isFullScreen.asStateFlow()

    private val _isPipMode = MutableStateFlow(false)
    val isPipMode = _isPipMode.asStateFlow()

    private val _autoPlayNext = MutableStateFlow(true)
    val autoPlayNext = _autoPlayNext.asStateFlow()

    // Lists
    val allVideos: StateFlow<List<Video>> = repository.videos
    val uploadTasks: StateFlow<List<UploadProgress>> = repository.uploadTasks
    val playlists: StateFlow<List<Playlist>> = repository.getPlaylists()
    val searchHistory: StateFlow<List<SearchHistoryItem>> = repository.getSearchHistory()
    
    val subscribedChannels: StateFlow<List<Triple<String, String, String>>> = repository.subscribedChannels
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _trendingVideos = MutableStateFlow<List<Video>>(emptyList())
    val trendingVideos = _trendingVideos.asStateFlow()

    private val _categoryVideos = MutableStateFlow<List<Video>>(emptyList())
    val categoryVideos = _categoryVideos.asStateFlow()

    private val _shortsVideos = MutableStateFlow<List<Video>>(emptyList())
    val shortsVideos = _shortsVideos.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Video>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _watchHistory = MutableStateFlow<List<Video>>(emptyList())
    val watchHistory = _watchHistory.asStateFlow()

    private val _likedVideos = MutableStateFlow<List<Video>>(emptyList())
    val likedVideos = _likedVideos.asStateFlow()

    private val _downloadedVideos = MutableStateFlow<List<Video>>(emptyList())
    val downloadedVideos = _downloadedVideos.asStateFlow()

    private val _savedVideos = MutableStateFlow<List<Video>>(emptyList())
    val savedVideos = _savedVideos.asStateFlow()

    private val _sharedVideos = MutableStateFlow<List<Video>>(emptyList())
    val sharedVideos = _sharedVideos.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    // Active playing video comments (loaded dynamically)
    private val _activeVideoComments = MutableStateFlow<List<Comment>>(emptyList())
    val activeVideoComments = _activeVideoComments.asStateFlow()

    // Dark Mode settings / Language / Privacy
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode = _themeMode.asStateFlow()

    private val _language = MutableStateFlow("English")
    val language = _language.asStateFlow()

    private val _isSearchHistoryPaused = MutableStateFlow(false)
    val isSearchHistoryPaused = _isSearchHistoryPaused.asStateFlow()

    private val _isWatchHistoryPaused = MutableStateFlow(false)
    val isWatchHistoryPaused = _isWatchHistoryPaused.asStateFlow()

    // Init blocks
    init {
        // Collect & transform
        viewModelScope.launch {
            repository.videos.collect {
                updateFeeds()
            }
        }
        viewModelScope.launch {
            repository.getWatchHistory().collect {
                _watchHistory.value = it
            }
        }
        viewModelScope.launch {
            repository.getNotifications().collect {
                _notifications.value = it
            }
        }
        viewModelScope.launch {
            repository.getLikedVideos().collect {
                _likedVideos.value = it
            }
        }
        viewModelScope.launch {
            repository.getDownloadedVideos().collect {
                _downloadedVideos.value = it
            }
        }
        viewModelScope.launch {
            repository.getSavedVideos().collect {
                _savedVideos.value = it
            }
        }
        viewModelScope.launch {
            repository.getSharedVideos().collect {
                _sharedVideos.value = it
            }
        }

        // Setup live suggestions on search query transitions
        viewModelScope.launch {
            _searchQuery.collect { query ->
                if (query.isEmpty()) {
                    _suggestedQueries.value = emptyList()
                } else {
                    _suggestedQueries.value = listOf(
                        "$query coding",
                        "$query sound effects",
                        "mastering $query in 10 minutes",
                        "advanced $query tutorial"
                    ).filter { it.contains(query, ignoreCase = true) }
                }
            }
        }
    }

    private fun updateFeeds() {
        val all = repository.videos.value
        _trendingVideos.value = all.filter { !it.isShort }.sortedByDescending { it.viewsCount }
        _shortsVideos.value = all.filter { it.isShort || it.category == "Shorts" }
        filterCategory(_selectedCategory.value)
    }

    // --- Authentication ---
    fun performSignUp(email: String, username: String, pass: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null
            val result = repository.signUp(email, username, pass)
            _isAuthenticating.value = false
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    saveUserSessionToPrefs(user)
                    _hasSeenOnboarding.value = true
                    sharedPreferences?.edit()?.putBoolean("has_seen_onboarding", true)?.apply()
                }
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Sign up failed."
            }
        }
    }

    fun performLogin(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null
            val result = repository.login(email, pass)
            _isAuthenticating.value = false
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    saveUserSessionToPrefs(user)
                    _hasSeenOnboarding.value = true
                    sharedPreferences?.edit()?.putBoolean("has_seen_onboarding", true)?.apply()
                }
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Login failed."
            }
        }
    }

    fun performGoogleSignIn() {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null
            val result = repository.googleLogin()
            _isAuthenticating.value = false
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    saveUserSessionToPrefs(user)
                    _hasSeenOnboarding.value = true
                    sharedPreferences?.edit()?.putBoolean("has_seen_onboarding", true)?.apply()
                }
            } else {
                _authError.value = "Google OAuth login cancelled or failed."
            }
        }
    }

    fun performForgotPassword(email: String) {
        viewModelScope.launch {
            repository.forgotPassword(email)
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            repository.logout()
            _activeVideo.value = null
            clearUserSessionFromPrefs()
        }
    }

    fun createChannel(
        displayName: String,
        username: String,
        avatarUrl: String,
        bio: String,
        channelKeywords: String,
        channelCategory: String
    ) {
        repository.updateUserChannelProfile(displayName, username, avatarUrl, bio, channelKeywords, channelCategory)
    }

    // --- Media Controls & UI States ---
    fun selectCategory(category: String) {
        _selectedCategory.value = category
        filterCategory(category)
    }

    private fun filterCategory(category: String) {
        val list = repository.videos.value
        _categoryVideos.value = when {
            category == "All" -> list.filter { !it.isShort }
            category == "Trending" -> list.filter { !it.isShort }.sortedByDescending { it.viewsCount }
            category == "Shorts" -> list.filter { it.isShort }
            else -> list.filter { !it.isShort && it.category.equals(category, ignoreCase = true) }
        }
    }

    fun playVideo(video: Video) {
        _activeVideo.value = video
        if (!_isWatchHistoryPaused.value) {
            repository.recordWatch(video)
        }
        loadCommentsForVideo(video.id)
    }

    fun playNextVideo() {
        val list = allVideos.value
        val currentIndex = list.indexOfFirst { it.id == _activeVideo.value?.id }
        if (currentIndex != -1 && currentIndex < list.size - 1) {
            playVideo(list[currentIndex + 1])
        } else if (list.isNotEmpty()) {
            playVideo(list.first())
        }
    }

    fun clearActiveVideo() {
        _activeVideo.value = null
    }

    fun changePlaybackSpeed(speed: String) {
        _playbackSpeed.value = speed
    }

    fun changePlaybackQuality(quality: String) {
        _playbackQuality.value = quality
    }

    fun toggleFullScreen(enable: Boolean) {
        _isFullScreen.value = enable
    }

    fun togglePipMode(enable: Boolean) {
        _isPipMode.value = enable
    }

    fun toggleAutoPlayNext(enable: Boolean) {
        _autoPlayNext.value = enable
    }

    // --- Likes/Dislikes & Subscribe Core ---
    fun toggleLikeVideo(videoId: String) {
        repository.toggleLike(videoId)
        // Refresh active video state only if it matches
        if (_activeVideo.value?.id == videoId) {
            _activeVideo.value = repository.getVideoById(videoId)
        }
    }

    fun toggleDislikeVideo(videoId: String) {
        repository.toggleDislike(videoId)
        if (_activeVideo.value?.id == videoId) {
            _activeVideo.value = repository.getVideoById(videoId)
        }
    }

    fun isVideoLiked(videoId: String): Boolean = repository.isLiked(videoId)
    fun isVideoDisliked(videoId: String): Boolean = repository.isDisliked(videoId)

    fun isSubscribed(channelId: String): Boolean = repository.isSubscribed(channelId)

    fun toggleSubscribeChannel(channelId: String) {
        repository.toggleSubscribe(channelId)
    }

    // --- Comments Logic ---
    private fun loadCommentsForVideo(videoId: String) {
        viewModelScope.launch {
            repository.getCommentsForVideo(videoId).collect {
                _activeVideoComments.value = it
            }
        }
    }

    fun getComments(videoId: String) = repository.getCommentsForVideo(videoId)

    fun submitComment(videoId: String, content: String) {
        if (content.trim().isEmpty()) return
        repository.addComment(videoId, content)
    }

    fun submitReplyComment(videoId: String, commentId: String, content: String) {
        if (content.trim().isEmpty()) return
        repository.replyComment(videoId, commentId, content)
    }

    fun submitLikeComment(videoId: String, commentId: String) {
        repository.likeComment(videoId, commentId)
    }

    fun removeComment(videoId: String, commentId: String) {
        repository.deleteComment(videoId, commentId)
    }

    // --- Searching ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _searchResults.value = repository.searchVideos(query)
    }

    fun performSearch(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty() && !_isSearchHistoryPaused.value) {
            repository.addSearchQuery(query)
        }
        _searchResults.value = repository.searchVideos(query)
    }

    fun deleteRecentSearch(query: String) {
        repository.removeSearchHistoryItem(query)
    }

    fun clearHistory() {
        repository.clearSearchHistory()
    }

    // --- Uploading ---
    fun generateMetadataWithGemini(title: String, description: String, onResult: (category: String, tags: String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val prompt = "Based on the following video title and description, suggest 1 main category (from: Tech, Coding, Music, Food, Lifestyle) and 3-5 relevant comma-separated tags. Return immediately perfectly formatted like this: Category|tags\n\nTitle: $title\nDescription: $description"
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null && text.contains("|")) {
                    val parts = text.split("|", limit = 2)
                    if (parts.size == 2) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            onResult(parts[0].trim(), parts[1].trim())
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun requestVideoUpload(title: String, description: String, category: String, duration: String, context: android.content.Context) {
        repository.uploadVideo(title, description, category, duration, context)
    }

    fun retryUploadTask(taskId: String) {
        repository.retryUpload(taskId)
    }

    fun dismissCompletedUpload(taskId: String) {
        repository.clearCompletedUpload(taskId)
    }

    // --- Channel Customization ---
    fun configureChannel(name: String, bio: String, avatarUrl: String, bannerUrl: String) {
        repository.updateChannel(name, bio, avatarUrl, bannerUrl)
    }

    // --- Playlist Controls ---
    fun submitCreatePlaylist(title: String, description: String, isPublic: Boolean) {
        if (title.trim().isEmpty()) return
        repository.createPlaylist(title, description, isPublic)
    }

    fun saveVideoToPlaylist(playlistId: String, videoId: String) {
        repository.addVideoToPlaylist(playlistId, videoId)
    }

    fun removeVideoFromPlaylist(playlistId: String, videoId: String) {
        repository.removeVideoFromPlaylist(playlistId, videoId)
    }

    // --- Saved Videos ---
    fun saveVideo(videoId: String) {
        repository.toggleSaveVideo(videoId)
    }

    fun isSaved(videoId: String): Boolean = repository.isSaved(videoId)

    fun shareVideoToProfile(videoId: String) {
        repository.shareVideoToProfile(videoId)
    }

    // --- Save to Downloads ---
    fun toggleDownload(videoId: String) {
        repository.toggleDownloadVideo(videoId)
    }

    fun isDownloaded(videoId: String): Boolean = repository.isDownloaded(videoId)

    // --- Notifications Hub ---
    fun resetNotificationsUnread() {
        repository.markNotificationsAsRead()
    }

    // --- Local Configuration Settings ---
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun changeLanguage(lang: String) {
        _language.value = lang
    }

    fun pauseSearchHistory(pause: Boolean) {
        _isSearchHistoryPaused.value = pause
    }

    fun pauseWatchHistory(pause: Boolean) {
        _isWatchHistoryPaused.value = pause
    }
}
