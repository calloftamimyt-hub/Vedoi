package com.example.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Playlist
import com.example.model.Video
import com.example.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: VideoViewModel,
    onNavigateToPlaylistDetail: (String) -> Unit,
    onNavigateToChannel: (String) -> Unit,
    onNavigateToCreateChannel: () -> Unit,
    onVideoClick: (Video) -> Unit,
    navController: androidx.navigation.NavController? = null,
    modifier: Modifier = Modifier
) {
    val watchHistory by viewModel.watchHistory.collectAsState()
    val likedVideos by viewModel.likedVideos.collectAsState()
    val downloadedVideos by viewModel.downloadedVideos.collectAsState()
    val savedVideos by viewModel.savedVideos.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val subscribedChannels by viewModel.subscribedChannels.collectAsState()
    val uploadTasks by viewModel.uploadTasks.collectAsState()

    val context = LocalContext.current

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDesc by remember { mutableStateOf("") }
    var playlistIsPublic by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "You", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.performLogout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (currentUser?.hasChannel == true) {
                                onNavigateToChannel("user_me")
                            } else {
                                onNavigateToCreateChannel()
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AsyncImage(
                        model = currentUser?.avatarUrl?.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" }
                            ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop",
                        contentDescription = "My avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column {
                        Text(
                            text = currentUser?.displayName ?: "User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (currentUser?.hasChannel == true) {
                            Text(
                                text = "@" + (currentUser?.username ?: ""),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "View channel \u203A",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = currentUser?.email ?: "",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "No channel \u203A",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            if (uploadTasks.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "Your Videos Uploading",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        uploadTasks.forEach { task ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (task.isFailed)
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = task.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (task.isCompleted) {
                                            IconButton(onClick = { viewModel.dismissCompletedUpload(task.id) }) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Dismiss task", tint = Color.Green)
                                            }
                                        } else if (task.isFailed) {
                                            IconButton(onClick = { viewModel.retryUploadTask(task.id) }) {
                                                Icon(Icons.Default.Refresh, contentDescription = "Retry upload task", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (!task.isCompleted && !task.isFailed) {
                                        LinearProgressIndicator(
                                            progress = task.progress,
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Uploading... ${(task.progress * 100).toInt()}% to S3 Cloudflare",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (task.isFailed) {
                                        Text(
                                            text = "Upload Interrupted! Cloudflare S3 bucket connection failed. Click retry.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Text(
                                            text = "Completed! Video posted live to channel.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1. Watch History horizontal scroll
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "History Icon", modifier = Modifier.size(24.dp))
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "View all",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                // In a real app we would navigate to HistoryScreen
                                // Assuming we have added a route for history
                                navController?.navigate("history")
                            }
                        )
                    }

                    if (watchHistory.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Videos you watch will slide in here.",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(watchHistory) { video ->
                                Column(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { onVideoClick(video) }
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = video.thumbnailUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .width(150.dp)
                                                .height(85.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        // duration tag
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.82f),
                                            shape = RoundedCornerShape(2.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = video.duration,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = video.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = video.channelName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Vertical list for options
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Liked Videos Menu Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Navigate to Liked Videos */ }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ThumbUp, contentDescription = "Liked videos", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Liked videos", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${likedVideos.size} videos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Shared Videos Menu Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Navigate to Shared Videos */ }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Shared videos", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Shared videos", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            val sharedCount by viewModel.sharedVideos.collectAsState()
                            Text(text = "${sharedCount.size} shared videos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Downloads Menu Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Navigate to Downloads */ }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Downloads", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Downloads", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${downloadedVideos.size} videos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Modal Create Playlist dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create New Playlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = playlistTitle,
                        onValueChange = { playlistTitle = it },
                        label = { Text("Playlist Title") },
                        placeholder = { Text("Study Beats, Workout tracks...") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("Description (Optional)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Visible Publicly", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = playlistIsPublic,
                            onCheckedChange = { playlistIsPublic = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistTitle.trim().isEmpty()) {
                            Toast.makeText(context, "Title is mandatory", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.submitCreatePlaylist(playlistTitle, playlistDesc, playlistIsPublic)
                        Toast.makeText(context, "Playlist Constructed!", Toast.LENGTH_SHORT).show()
                        playlistTitle = ""
                        playlistDesc = ""
                        playlistIsPublic = true
                        showCreatePlaylistDialog = false
                    }
                ) {
                    Text("Build")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        playlistTitle = ""
                        playlistDesc = ""
                        showCreatePlaylistDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
