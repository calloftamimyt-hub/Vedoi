package com.example.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = { Text(text = "Subscriptions", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (subscriptionVideos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("You haven't subscribed to any channels yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                items(subscriptionVideos) { video ->
                    VideoItemCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onChannelClick = onNavigateToChannel
                    )
                }
            }
        }
    }
}
