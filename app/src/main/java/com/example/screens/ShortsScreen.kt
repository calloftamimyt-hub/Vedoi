package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.components.CommentSection
import com.example.model.Video
import com.example.viewmodel.VideoViewModel

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ShortsScreen(
    viewModel: VideoViewModel,
    onNavigateToChannel: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val shortsVideos by viewModel.shortsVideos.collectAsState()
    val subscriptions by viewModel.subscribedChannels.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) } // 0: For You, 1: Following
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredShorts = if (selectedTab == 0) {
        shortsVideos
    } else {
        shortsVideos.filter { video ->
            subscriptions.any { sub -> sub.first == video.channelId }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (filteredShorts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedTab == 0) "No shorts available at the moment." else "You haven't followed any channels with shorts.",
                    color = Color.White
                )
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { filteredShorts.size })
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val video = filteredShorts[page]
                ShortVideoItem(
                    video = video,
                    viewModel = viewModel,
                    onNavigateToChannel = onNavigateToChannel
                )
            }
        }

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "For You",
                    fontSize = 18.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { selectedTab = 0 }
                )
                Text(
                    text = "Following",
                    fontSize = 18.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { selectedTab = 1 }
                )
            }

            IconButton(onClick = { showReportDialog = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
            }
        }

        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("Report Video") },
                text = { Text("Are you sure you want to report this video?") },
                confirmButton = {
                    TextButton(onClick = {
                        showReportDialog = false
                        Toast.makeText(context, "Video reported", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Report")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortVideoItem(
    video: Video,
    viewModel: VideoViewModel,
    onNavigateToChannel: (String) -> Unit
) {
    val isLiked = viewModel.isVideoLiked(video.id)
    val isSubscribed = viewModel.isSubscribed(video.channelId)
    val savedVideos by viewModel.savedVideos.collectAsState()
    val isSaved = savedVideos.any { it.id == video.id }
    val comments by viewModel.getComments(video.id).collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    
    var titleExpanded by remember { mutableStateOf(false) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full screen video mock (using image for now)
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = "Short video",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Gradient for text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 500f
                    )
                )
        )
        
        // Details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp, start = 12.dp, end = 70.dp) // Avoid overlap with right actions
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = video.channelAvatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" },
                    contentDescription = "Channel Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToChannel(video.channelId) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = video.channelName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { onNavigateToChannel(video.channelId) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                // Follow Button
                Surface(
                    onClick = { viewModel.toggleSubscribeChannel(video.channelId) },
                    color = if (isSubscribed) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isSubscribed) "Following" else "Follow",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Column {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = if (titleExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!titleExpanded && video.title.length > 60) { // Rough heuristic for 2 lines
                    Text(
                        text = "See more",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { titleExpanded = true }
                            .padding(top = 4.dp)
                    )
                } else if (titleExpanded) {
                    Text(
                        text = "See less",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { titleExpanded = false }
                            .padding(top = 4.dp)
                    )
                }
            }
        }
        
        // Actions (Right side)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Like (Heart)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.toggleLikeVideo(video.id) }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(text = formatCount(video.likesCount), color = Color.White, fontSize = 12.sp)
            }
            
            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    showCommentSheet = true
                }) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(text = formatCount(video.commentsCount), color = Color.White, fontSize = 12.sp)
            }
            
            // Save (Bookmark)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    viewModel.saveVideo(video.id)
                    val msg = if (isSaved) "Removed from saved" else "Saved to your list"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) Color.Yellow else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(text = "Save", color = Color.White, fontSize = 12.sp)
            }
            
            // Share
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { showShareSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(text = "Share", color = Color.White, fontSize = 12.sp)
            }
        }
    }

    if (showCommentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                CommentSection(
                    comments = comments,
                    currentUser = currentUser,
                    onPostComment = { text -> viewModel.submitComment(video.id, text) },
                    onReplyComment = { commentId, text -> viewModel.submitReplyComment(video.id, commentId, text) },
                    onLikeComment = { commentId -> viewModel.submitLikeComment(video.id, commentId) },
                    onDeleteComment = { commentId -> 
                        viewModel.removeComment(video.id, commentId)
                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Share to",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Horizontal list of share targets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                        Toast.makeText(context, "Link Copied", Toast.LENGTH_SHORT).show()
                        showShareSheet = false
                    }) {
                        Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Copy Link", fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                        viewModel.shareVideoToProfile(video.id)
                        Toast.makeText(context, "Shared to Profile", Toast.LENGTH_SHORT).show()
                        showShareSheet = false
                    }) {
                        Box(modifier = Modifier.size(56.dp).background(Color(0xFF1877F2), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = "My Profile", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("My Profile", fontSize = 12.sp)
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                        Toast.makeText(context, "Video reported", Toast.LENGTH_SHORT).show()
                        showShareSheet = false
                    }) {
                        Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.errorContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Flag, contentDescription = "Report", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Report", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

