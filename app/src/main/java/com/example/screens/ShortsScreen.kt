package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Video
import com.example.viewmodel.VideoViewModel

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ShortsScreen(
    viewModel: VideoViewModel,
    onNavigateToChannel: (String) -> Unit
) {
    val shortsVideos by viewModel.shortsVideos.collectAsState()
    
    if (shortsVideos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No shorts available at the moment.", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { shortsVideos.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) { page ->
        val video = shortsVideos[page]
        ShortVideoItem(
            video = video,
            viewModel = viewModel,
            onNavigateToChannel = onNavigateToChannel
        )
    }
}

@Composable
fun ShortVideoItem(
    video: Video,
    viewModel: VideoViewModel,
    onNavigateToChannel: (String) -> Unit
) {
    val isLiked = viewModel.isVideoLiked(video.id)
    val isDisliked = viewModel.isVideoDisliked(video.id)
    
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
                .padding(16.dp)
                // App Scaffold handles bottom padding
                .fillMaxWidth(0.8f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateToChannel(video.channelId) }
            ) {
                AsyncImage(
                    model = video.channelAvatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" },
                    contentDescription = "Channel Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = video.channelName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { viewModel.toggleSubscribeChannel(video.channelId) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Subscribe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 14.sp
            )
        }
        
        // Actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.toggleLikeVideo(video.id) }) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
                Text(text = formatCount(video.likesCount), color = Color.White, fontSize = 12.sp)
            }
            
            // Dislike
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.toggleDislikeVideo(video.id) }) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "Dislike",
                        tint = if (isDisliked) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
                Text(text = "Dislike", color = Color.White, fontSize = 12.sp)
            }
            
            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /* Open comments */ }) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comment",
                        tint = Color.White
                    )
                }
                Text(text = formatCount(video.commentsCount), color = Color.White, fontSize = 12.sp)
            }
            
            // Share
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /* Share */ }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
                Text(text = "Share", color = Color.White, fontSize = 12.sp)
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
