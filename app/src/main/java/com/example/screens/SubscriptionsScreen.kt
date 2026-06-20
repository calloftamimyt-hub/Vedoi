package com.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.components.VideoItemCard
import com.example.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    viewModel: VideoViewModel,
    onNavigateToChannel: (String) -> Unit,
    onVideoClick: (com.example.model.Video) -> Unit,
    modifier: Modifier = Modifier
) {
    val subscribedChannels by viewModel.subscribedChannels.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    
    val subscribedChannelIds = subscribedChannels.map { it.first }
    val subscriptionVideos = allVideos.filter { subscribedChannelIds.contains(it.channelId) }.sortedByDescending { it.createdAt }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Following", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (subscriptionVideos.isEmpty() && subscribedChannels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("You haven't followed any channels yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                if (subscribedChannels.isNotEmpty()) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(subscribedChannels) { channel ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onNavigateToChannel(channel.first) }
                                        .width(72.dp)
                                ) {
                                    AsyncImage(
                                        model = channel.third.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" },
                                        contentDescription = channel.second,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = channel.second,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                    }
                }

                items(subscriptionVideos) { video ->
                    val currentUser by viewModel.currentUser.collectAsState()
                    val isDeletable = video.channelId == currentUser?.id || 
                                      video.id.contains("demo", ignoreCase = true) ||
                                      video.videoUrl.contains("commondatastorage", ignoreCase = true)

                    VideoItemCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onChannelClick = onNavigateToChannel,
                        onDeleteClick = if (isDeletable) { { viewModel.deleteVideo(video.id) } } else null
                    )
                }
            }
        }
    }
}
