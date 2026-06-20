package com.example.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.VideoItemCard
import com.example.viewmodel.VideoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: VideoViewModel,
    onNavigateBack: () -> Unit,
    onVideoClick: (com.example.model.Video) -> Unit,
    onNavigateToChannel: (String) -> Unit
) {
    val history by viewModel.watchHistory.collectAsState()

    // Grouping by Date
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // In a real app we would have the exact watch timestamp per video.
    // For now we group by a static day or treat them all as "Today" since the model doesn't store watch timestamps separately yet.
    // However, to mimic YouTube, we'll just group them by the video's creation date 
    // or simulate "Today", "Yesterday". We can just show them all under "Today" for the mockup.
    val groupedHistory = history.groupBy { video ->
        // Simulate a date grouping
        "Recently watched"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Your watch history is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                groupedHistory.forEach { (date, videos) ->
                    item {
                        Text(
                            text = date,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    items(videos) { video ->
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
}
