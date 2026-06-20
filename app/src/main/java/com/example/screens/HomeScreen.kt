package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.components.CategoryFilterRow
import com.example.components.VideoItemCard
import com.example.model.Video
import com.example.viewmodel.VideoViewModel

@Composable
fun SkeletonVideoCard() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VideoViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChannel: (String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToShorts: () -> Unit,
    onVideoClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryVideos by viewModel.categoryVideos.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val shortsVideos by viewModel.shortsVideos.collectAsState()

    val unreadNotifs = notifications.any { !it.isRead }

    val categories = listOf("All", "Trending", "Coding", "Tech", "Music", "Food", "Lifestyle")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "ViewTube",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    // Search
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Channel Avatar Profile Link (Moved to Top App Bar as requested)
                    IconButton(
                        onClick = onNavigateToLibrary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        AsyncImage(
                            model = currentUser?.avatarUrl?.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" }
                                ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop",
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("home_top_bar")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal scrolling Category Filters
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

            var isRefreshing by remember { mutableStateOf(false) }

            // Main infinite list of videos
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    // Simulate refresh since mock data is loaded instantly
                    isRefreshing = false 
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (categoryVideos.isEmpty()) {
                    if (currentUser != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                            shape = CircleShape
                                        )
                                        .border(2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                                Text(
                                    text = "No videos here yet",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Be the first to publish a new video, or explore other categories!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = false
                        ) {
                            items(5) {
                                SkeletonVideoCard()
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("recommended_videos_list")
                    ) {
                        if (selectedCategory == "All" && shortsVideos.isNotEmpty()) {
                            item {
                                ShortsShelf(
                                    shorts = shortsVideos,
                                    onNavigateToShorts = onNavigateToShorts
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), thickness = 4.dp)
                            }
                        }
                        
                        items(categoryVideos) { video ->
                            VideoItemCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onChannelClick = onNavigateToChannel
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShortsShelf(
    shorts: List<Video>,
    onNavigateToShorts: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Bolt, contentDescription = "Shorts", tint = MaterialTheme.colorScheme.error)
                Text("Shorts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            TextButton(onClick = onNavigateToShorts) {
                Text("View All")
            }
        }
        
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(shorts) { short ->
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToShorts() }
                ) {
                    AsyncImage(
                        model = short.thumbnailUrl,
                        contentDescription = short.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 100f
                            ))
                    )
                    Text(
                        text = short.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
