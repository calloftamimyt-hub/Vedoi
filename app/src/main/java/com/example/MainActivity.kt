package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: VideoViewModel = viewModel()
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
                    AppNavigationCoordinator(viewModel = mainViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigationCoordinator(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentUser by viewModel.currentUser.collectAsState()
    val activeVideo by viewModel.activeVideo.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()

    // Redirect to Authentication if not logged in
    val initialRoute = if (currentUser == null) "auth" else "home"

    // Hide Bottom controls on specific screens
    val isAuthScreen = currentRoute == "auth"
    val isPlayerScreen = currentRoute == "player"
    val shouldHideBottomControls = isAuthScreen || isPlayerScreen || isFullScreen

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
                                viewModel.performLogout() // or just stop video: let's clear playing state
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
                            icon = { Icon(if (currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home", modifier = Modifier.size(24.dp)) },
                            label = { Text("Home", maxLines = 1, softWrap = false, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                            icon = { Icon(if (currentRoute == "shorts") Icons.Default.Bolt else Icons.Outlined.Bolt, contentDescription = "Shorts", modifier = Modifier.size(24.dp)) },
                            label = { Text("Shorts", maxLines = 1, softWrap = false, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("nav_shorts")
                        )

                        // Option 3: a plus icon
                        NavigationBarItem(
                            selected = currentRoute == "upload",
                            onClick = {
                                if (currentRoute != "upload") {
                                    navController.navigate("upload") {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(28.dp)) },
                            label = { Text("Create", maxLines = 1, softWrap = false, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                            icon = { Icon(if (currentRoute == "subscriptions") Icons.Default.AccountCircle else Icons.Outlined.AccountCircle, contentDescription = "Subscriptions", modifier = Modifier.size(24.dp)) },
                            label = { Text("Following", maxLines = 1, softWrap = false, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("nav_subscriptions")
                        )

                        // Option 4: a circle. Next to the circle is the profile option.
                        val isProfileSelected = currentRoute == "library"
                        NavigationBarItem(
                            selected = isProfileSelected,
                            onClick = {
                                if (!isProfileSelected) {
                                    navController.navigate("library") {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // The Circle (the fourth option is a circle)
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isProfileSelected) MaterialTheme.colorScheme.error 
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                    )

                                    // Next to the circle is the profile option (avatar indicator)
                                    AsyncImage(
                                        model = currentUser?.avatarUrl?.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" }
                                            ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop",
                                        contentDescription = "Profile option",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            },
                            label = { Text("Profile", maxLines = 1, softWrap = false, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("nav_profile")
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
