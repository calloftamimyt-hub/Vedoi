package com.example.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.components.formatNumber
import com.example.components.formatRelativeTime
import com.example.model.Comment
import com.example.model.Video
import com.example.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: VideoViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeVideo by viewModel.activeVideo.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()
    val isPipMode by viewModel.isPipMode.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val playbackQuality by viewModel.playbackQuality.collectAsState()
    val autoPlayNext by viewModel.autoPlayNext.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val comments by viewModel.activeVideoComments.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isDescExpanded by remember { mutableStateOf(false) }
    var isSavingToPlaylist by remember { mutableStateOf(false) }

    // Dialog state controllers
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    // Dynamic Comments input buffers
    var activeCommentText by remember { mutableStateOf("") }
    var replyingCommentId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    val video = activeVideo ?: return

    val isLiked = viewModel.isVideoLiked(video.id)
    val isDisliked = viewModel.isVideoDisliked(video.id)
    val isSubscribed = viewModel.isSubscribed(video.channelId)
    val isDownloaded = viewModel.isDownloaded(video.id)

    if (isFullScreen) {
        // Simple Full screen player canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Back from fullscreen button
            IconButton(
                onClick = { viewModel.toggleFullScreen(false) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit full screen",
                    tint = Color.White
                )
            }

            Text(
                text = "Simulating Full Screen Playback (16:9 Aspect)",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
            )
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Now Playing", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Interactive Video Screen Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = "Video playback frame",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Mock Player visual overlays
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(16.dp)
                ) {
                    // Center active Play/Pause simulated triggers
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {} /* previous */) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Skip back", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        IconButton(
                            onClick = { Toast.makeText(context, "Playback Paused", Toast.LENGTH_SHORT).show() }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.size(54.dp))
                        }
                        IconButton(onClick = { viewModel.playNextVideo() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Skip forward", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    // Bottom: Scrubbing Slider bar
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "01:24 / ${video.duration}", color = Color.White, fontSize = 12.sp)

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Pin-to-Pin PiP Trigger
                                Icon(
                                    imageVector = Icons.Default.PictureInPicture,
                                    contentDescription = "PiP Mode",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            viewModel.togglePipMode(true)
                                            Toast.makeText(context, "Picture in Picture Enabled", Toast.LENGTH_SHORT).show()
                                        }
                                )

                                // FullScreen
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Full Screen",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable { viewModel.toggleFullScreen(true) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = 0.15f,
                            color = Color.Red,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            // 2. Video Details (Title, Statistics, Description Expansion)
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats summary expandable description header
                Surface(
                    onClick = { isDescExpanded = !isDescExpanded },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${formatNumber(video.viewsCount)} views • ${formatRelativeTime(video.createdAt)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDescExpanded) "Show Less" else "...more",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = isDescExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = video.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Category: ${video.category}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Horizontal Interactive Toolbar: Likes / Shares / Playlists / Downloads
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like / Dislike Split-button
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.toggleLikeVideo(video.id) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like video",
                                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = formatNumber(video.likesCount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Divider line
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))

                        IconButton(
                            onClick = { viewModel.toggleDislikeVideo(video.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                                contentDescription = "Dislike video",
                                tint = if (isDisliked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Share button
                    Button(
                        onClick = { 
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, video.title)
                                putExtra(Intent.EXTRA_TEXT, "Check out this video: https://viewtube.app/play/${video.id}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Video Link"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), contentColor = MaterialTheme.colorScheme.onSurface),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share video link", modifier = Modifier.size(16.dp))
                            Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Save playlist
                    Button(
                        onClick = { isSavingToPlaylist = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), contentColor = MaterialTheme.colorScheme.onSurface),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = "Add to playlist", modifier = Modifier.size(16.dp))
                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Download Offline
                    IconButton(
                        onClick = {
                            viewModel.toggleDownload(video.id)
                            Toast.makeText(
                                context,
                                if (isDownloaded) "Removed from offline downloads" else "Downloaded to local storage of ViewTube",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                            contentDescription = "Download video",
                            tint = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Configure Playback settings buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSpeedDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Speed: $playbackSpeed", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showQualityDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Quality: $playbackQuality", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video Performance Metrics snippet
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Video Performance Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        
                        Text("Viewer Retention", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val mockRetentionData = listOf(1f, 0.95f, 0.85f, 0.7f, 0.65f, 0.5f, 0.45f, 0.4f, 0.35f, 0.3f)
                            mockRetentionData.forEach { factor ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(factor)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Avg Watch Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("02:15", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Channel Details Header Info Area (Avatar, Name, Sub Count, Sub Action Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = video.channelAvatarUrl.ifEmpty { "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=100&auto=format&fit=crop" },
                        contentDescription = "Channel Icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.channelName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (video.channelId == currentUser?.id)
                                "${formatNumber(currentUser?.subscribersCount ?: 0)} subscribers"
                            else
                                "Subscribers count simulated",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (video.channelId != currentUser?.id) {
                        Button(
                            onClick = { viewModel.toggleSubscribeChannel(video.channelId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else
                                    MaterialTheme.colorScheme.error,
                                contentColor = if (isSubscribed)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("subscribe_button")
                        ) {
                            Text(text = if (isSubscribed) "Subscribed" else "Subscribe", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // 5. Unified Comments Column Section (Add Comment, Display Replies, Likes, and Trash Can Deletes)
                Text(
                    text = "Comments (${comments.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Post New Comment Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = currentUser?.avatarUrl?.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" }
                            ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop",
                        contentDescription = "My avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )

                    OutlinedTextField(
                        value = activeCommentText,
                        onValueChange = { activeCommentText = it },
                        placeholder = { Text("Add a public comment...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_comment_input"),
                        shape = RoundedCornerShape(20.dp),
                        trailingIcon = {
                            if (activeCommentText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewModel.submitComment(video.id, activeCommentText)
                                        activeCommentText = ""
                                    },
                                    modifier = Modifier.testTag("submit_comment_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Publish", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of interactive Active Comments
                if (comments.isEmpty()) {
                    Text(
                        text = "Be the first to comment on this video!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        comments.forEach { comment ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = comment.userAvatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" },
                                        contentDescription = "User comment avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = comment.userName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = formatRelativeTime(comment.createdAt),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = comment.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Comment Action row: Likes count, Reply Button, Delete Trashcan
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable { viewModel.submitLikeComment(video.id, comment.id) }
                                            ) {
                                                Icon(
                                                    imageVector = if (comment.isLikedByMe) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                                    contentDescription = "Thumb up comment",
                                                    tint = if (comment.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = formatNumber(comment.likesCount),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Text(
                                                text = "Reply",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.clickable {
                                                    replyingCommentId = if (replyingCommentId == comment.id) null else comment.id
                                                    replyText = ""
                                                }
                                            )

                                            // Delete Trash indicator if owned by user
                                            if (comment.userId == currentUser?.id) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete comment",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable {
                                                            viewModel.removeComment(video.id, comment.id)
                                                            Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show()
                                                        }
                                                )
                                            }
                                        }

                                        // Insert replies field inline
                                        AnimatedVisibility(visible = replyingCommentId == comment.id) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = replyText,
                                                    onValueChange = { replyText = it },
                                                    placeholder = { Text("Reply to comment...", fontSize = 12.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(16.dp),
                                                    singleLine = true
                                                )
                                                IconButton(
                                                    onClick = {
                                                        viewModel.submitReplyComment(video.id, comment.id, replyText)
                                                        replyingCommentId = null
                                                        replyText = ""
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Send, contentDescription = "Send reply", tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }

                                        // Render Replies
                                        if (comment.replies.isNotEmpty()) {
                                            Column(modifier = Modifier.padding(top = 8.dp, start = 8.dp)) {
                                                comment.replies.forEach { r ->
                                                    Row(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        AsyncImage(
                                                            model = r.userAvatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" },
                                                            contentDescription = null,
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                        )
                                                        Column {
                                                            Text(
                                                                text = "${r.userName} • ${formatRelativeTime(r.createdAt)}",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Text(text = r.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS & SHEET OVERLAYS ---

    // 1. Playback Speed Selector Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Select Playback Speed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("0.5x", "1.0x (Normal)", "1.5x", "2.0x").forEach { spd ->
                        val plainSpeed = spd.substringBefore(" ")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.changePlaybackSpeed(plainSpeed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = spd, fontSize = 15.sp, fontWeight = if (playbackSpeed == plainSpeed) FontWeight.Bold else FontWeight.Normal)
                            if (playbackSpeed == plainSpeed) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text("Cancel") }
            }
        )
    }

    // 2. Playback Quality Selector Dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Select Stream Quality") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Auto", "1080p60", "720p", "480p", "360p", "240p").forEach { qlty ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.changePlaybackQuality(qlty)
                                    showQualityDialog = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = qlty, fontSize = 15.sp, fontWeight = if (playbackQuality == qlty) FontWeight.Bold else FontWeight.Normal)
                            if (playbackQuality == qlty) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQualityDialog = false }) { Text("Cancel") }
            }
        )
    }

    // 4. Save to Playlist Popup list
    if (isSavingToPlaylist) {
        AlertDialog(
            onDismissRequest = { isSavingToPlaylist = false },
            title = { Text("Add to Playlists") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (playlists.isEmpty()) {
                        Text("No playlists found. Create one from the Library Screen first.")
                    } else {
                        playlists.forEach { pl ->
                            val containsVideo = pl.videoIds.contains(video.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (containsVideo) {
                                            viewModel.removeVideoFromPlaylist(pl.id, video.id)
                                            Toast.makeText(context, "Removed from playlist: ${pl.title}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.saveVideoToPlaylist(pl.id, video.id)
                                            Toast.makeText(context, "Saved to playlist: ${pl.title}", Toast.LENGTH_SHORT).show()
                                        }
                                        isSavingToPlaylist = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (pl.isPublic) Icons.Default.Public else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(text = pl.title, fontSize = 15.sp)
                                }
                                Checkbox(
                                    checked = containsVideo,
                                    onCheckedChange = {
                                        if (containsVideo) {
                                            viewModel.removeVideoFromPlaylist(pl.id, video.id)
                                        } else {
                                            viewModel.saveVideoToPlaylist(pl.id, video.id)
                                        }
                                        isSavingToPlaylist = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { isSavingToPlaylist = false }) { Text("Done") }
            }
        )
    }
}
