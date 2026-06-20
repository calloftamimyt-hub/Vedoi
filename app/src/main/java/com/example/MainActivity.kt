package com.example

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.components.VideoMiniPlayer
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VideoViewModel
import com.example.screens.*

import androidx.compose.foundation.isSystemInDarkTheme
import com.example.model.ThemeMode
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    private val mainViewModel: VideoViewModel by viewModels()
    private var isPlayerActive = false

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isPlayerActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().build())
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        mainViewModel.togglePipMode(isInPictureInPictureMode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val themeMode by mainViewModel.themeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isAppDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemDark
            }

            MyApplicationTheme(darkTheme = isAppDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationCoordinator(
                        viewModel = mainViewModel,
                        onRouteChanged = { route ->
                            isPlayerActive = route == "player"
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigationCoordinator(
    viewModel: VideoViewModel,
    onRouteChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        onRouteChanged(currentRoute)
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val activeVideo by viewModel.activeVideo.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()
    val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsState()
    
    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isAppDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }

    // Redirect to Authentication or Onboarding if not logged in
    val initialRoute = if (currentUser == null) {
        if (!hasSeenOnboarding) "onboarding" else "auth"
    } else "home"

    // Hide Bottom controls on specific screens
    val isAuthScreen = currentRoute == "auth"
    val isOnboardingScreen = currentRoute == "onboarding"
    val isPlayerScreen = currentRoute == "player"
    val shouldHideBottomControls = isAuthScreen || isOnboardingScreen || isPlayerScreen || isFullScreen

    Scaffold(
        bottomBar = {
            if (!shouldHideBottomControls) {
                Column {
                    // Hover floating Mini Player above navigation bar if video is active and we are not in Player screen
                    if (activeVideo != null) {
                        VideoMiniPlayer(
                            activeVideo = activeVideo,
                            onExpand = {
                                navController.navigate("player") {
                                    launchSingleTop = true
                                }
                            },
                            onClose = {
                                viewModel.clearActiveVideo()
                                viewModel.changePlaybackSpeed("1.0x")
                            }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        modifier = Modifier
                            .testTag("app_bottom_bar")
                            .height(72.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        // Options 1: Home
                        NavigationBarItem(
                            selected = currentRoute == "home",
                            onClick = {
                                if (currentRoute != "home") {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            icon = { Icon(if (currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home", modifier = Modifier.size(26.dp)) },
                            label = { Text("Home", maxLines = 1, softWrap = false, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_home")
                        )

                        // Option 1.5: Shorts
                        NavigationBarItem(
                            selected = currentRoute == "shorts",
                            onClick = {
                                if (currentRoute != "shorts") {
                                    navController.navigate("shorts") {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { Icon(if (currentRoute == "shorts") Icons.Default.PlayCircle else Icons.Outlined.PlayCircle, contentDescription = "Shorts", modifier = Modifier.size(26.dp)) },
                            label = { Text("Shorts", maxLines = 1, softWrap = false, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_shorts")
                        )

                        // Option 3: a plus icon
                        NavigationBarItem(
                            selected = false,
                            onClick = {
                                if (currentRoute != "upload") {
                                    navController.navigate("upload") {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { 
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isAppDarkTheme) androidx.compose.ui.graphics.Color(0xFF272727) else androidx.compose.ui.graphics.Color(0xFFF1F1F1))
                                        .border(1.dp, if (isAppDarkTheme) androidx.compose.ui.graphics.Color.DarkGray else androidx.compose.ui.graphics.Color.LightGray, CircleShape)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(28.dp))
                                }
                            },
                            modifier = Modifier.testTag("nav_upload")
                        )

                        // Option 3.5: a circle icon for Subscriptions
                        NavigationBarItem(
                            selected = currentRoute == "subscriptions",
                            onClick = {
                                if (currentRoute != "subscriptions") {
                                    navController.navigate("subscriptions") {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { Icon(if (currentRoute == "subscriptions") Icons.Default.Subscriptions else Icons.Outlined.Subscriptions, contentDescription = "Following", modifier = Modifier.size(26.dp)) },
                            label = { Text("Following", maxLines = 1, softWrap = false, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_subscriptions")
                        )

                        // Option 4: Notifications
                        val isNotificationsSelected = currentRoute == "notifications"
                        NavigationBarItem(
                            selected = isNotificationsSelected,
                            onClick = {
                                if (!isNotificationsSelected) {
                                    navController.navigate("notifications") {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isNotificationsSelected) Icons.Default.Notifications else Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = { Text("Notifications", maxLines = 1, softWrap = false, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_notifications")
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (shouldHideBottomControls) PaddingValues(0.dp) else innerPadding)
        ) {
            // Onboarding 
            composable("onboarding") {
                OnboardingScreen(
                    onFinish = {
                        viewModel.completeOnboarding()
                        navController.navigate("auth") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            // Authentication
            composable("auth") {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // Recommended Feed
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { navController.navigate("search") },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToChannel = { channelId -> navController.navigate("channel/$channelId") },
                    onNavigateToLibrary = { navController.navigate("library") },
                    onNavigateToShorts = { navController.navigate("shorts") },
                    onVideoClick = { video ->
                        viewModel.playVideo(video)
                        navController.navigate("player")
                    }
                )
            }

            // Main Player Area
            composable("player") {
                PlayerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Studio Upload Area
            composable("upload") {
                UploadScreen(
                    viewModel = viewModel
                )
            }

            // Shorts Area
            composable("shorts") {
                ShortsScreen(
                    viewModel = viewModel,
                    onNavigateToChannel = { channelId -> navController.navigate("channel/$channelId") }
                )
            }

            // Search Hub
            composable("search") {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChannel = { channelId -> navController.navigate("channel/$channelId") },
                    onVideoClick = { video ->
                        viewModel.playVideo(video)
                        navController.navigate("player")
                    }
                )
            }

            // Subscriptions Dashboard
            composable("subscriptions") {
                SubscriptionsScreen(
                    viewModel = viewModel,
                    onNavigateToChannel = { channelId -> navController.navigate("channel/$channelId") },
                    onVideoClick = { video ->
                        viewModel.playVideo(video)
                        navController.navigate("player")
                    }
                )
            }

            // Library playlists / Watch history Dashboard
            composable("library") {
                LibraryScreen(
                    viewModel = viewModel,
                    onNavigateToPlaylistDetail = { playlistId -> navController.navigate("playlist/$playlistId") },
                    onNavigateToChannel = { channelId -> navController.navigate("channel/$channelId") },
                    onNavigateToCreateChannel = { navController.navigate("create_channel") },
                    onVideoClick = { video ->
                        viewModel.playVideo(video)
                        navController.navigate("player")
                    },
                    navController = navController
                )
            }

            // Watch History Detail Screen
            composable("history") {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChannel = { channelId -> navController.navigate("channel/$channelId") },
                    onVideoClick = { video ->
                        viewModel.playVideo(video)
                        navController.navigate("player")
                    }
                )
            }

            // Settings Configurations panel
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAuth = {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Notifications Alert feeds list
            composable("notifications") {
                NotificationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Channel detail customization or inspection
            composable(
                route = "channel/{channelId}",
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val channelId = backStackEntry.arguments?.getString("channelId") ?: "user_me"
                ChannelScreen(
                    viewModel = viewModel,
                    channelId = channelId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onVideoClick = { video ->
                        viewModel.playVideo(video)
                        navController.navigate("player")
                    }
                )
            }

            // Create Channel
            composable("create_channel") {
                com.example.screens.CreateChannelScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onChannelCreated = {
                        navController.navigate("channel/user_me") {
                            popUpTo("library") { inclusive = false }
                        }
                    }
                )
            }

            // Playlist inspection Detail list
            composable(
                route = "playlist/{playlistId}",
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                PlaylistDetailScreen(
                    viewModel = viewModel,
                    playlistId = playlistId,
                    onNavigateBack = { navController.popBackStack() },
                    onVideoClick = { video ->
                        viewModel.playVideo(video)
                        navController.navigate("player")
                    }
                )
            }
        }
    }
}
