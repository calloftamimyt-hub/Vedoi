package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.formatRelativeTime
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: VideoViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()

    // Mark all read when screen is visited
    LaunchedEffect(Unit) {
        viewModel.resetNotificationsUnread()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text("No updates yet.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(notifications) { item ->
                    val bgColor = if (!item.isRead) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon associated with notification type
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (item.type) {
                                        NotificationType.SUBSCRIBER -> MaterialTheme.colorScheme.secondaryContainer
                                        NotificationType.COMMENT -> MaterialTheme.colorScheme.surfaceVariant
                                        NotificationType.LIKE -> MaterialTheme.colorScheme.errorContainer
                                        NotificationType.SYSTEM -> MaterialTheme.colorScheme.primaryContainer
                                        NotificationType.NEW_VIDEO -> MaterialTheme.colorScheme.tertiaryContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (item.type) {
                                    NotificationType.SUBSCRIBER -> Icons.Default.PersonAdd
                                    NotificationType.COMMENT -> Icons.Default.Comment
                                    NotificationType.LIKE -> Icons.Default.Favorite
                                    NotificationType.SYSTEM -> Icons.Default.Notifications
                                    NotificationType.NEW_VIDEO -> Icons.Default.VideoLibrary
                                },
                                contentDescription = null,
                                tint = when (item.type) {
                                    NotificationType.SUBSCRIBER -> MaterialTheme.colorScheme.onSecondaryContainer
                                    NotificationType.COMMENT -> MaterialTheme.colorScheme.onSurfaceVariant
                                    NotificationType.LIKE -> MaterialTheme.colorScheme.onErrorContainer
                                    NotificationType.SYSTEM -> MaterialTheme.colorScheme.onPrimaryContainer
                                    NotificationType.NEW_VIDEO -> MaterialTheme.colorScheme.onTertiaryContainer
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Message details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = formatRelativeTime(item.createdAt),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Highlight unread tags
                        if (!item.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}
