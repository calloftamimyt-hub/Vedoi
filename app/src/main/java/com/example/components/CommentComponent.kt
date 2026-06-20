package com.example.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ThumbUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Comment
import com.example.model.UserProfile
import java.util.concurrent.TimeUnit

@Composable
fun CommentSection(
    comments: List<Comment>,
    currentUser: UserProfile?,
    onPostComment: (String) -> Unit,
    onReplyComment: (String, String) -> Unit,
    onLikeComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit
) {
    val context = LocalContext.current
    var activeCommentText by remember { mutableStateOf("") }
    var replyingCommentId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Text(
            text = "Comments (${comments.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // List of interactive Active Comments
        if (comments.isEmpty()) {
            Text(
                text = "Be the first to comment on this video!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp).weight(1f)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
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
                                        text = formatCommentRelativeTime(comment.createdAt),
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
                                        modifier = Modifier.clickable { 
                                            if (currentUser != null) {
                                                onLikeComment(comment.id) 
                                            } else {
                                                Toast.makeText(context, "Please sign in to like comments", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (comment.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Like comment",
                                            tint = if (comment.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = formatCommentNumber(comment.likesCount),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (currentUser != null) {
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
                                    }

                                    // Delete Trash indicator if owned by user
                                    if (currentUser != null && comment.userId == currentUser.id) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete comment",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    onDeleteComment(comment.id)
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
                                                onReplyComment(comment.id, replyText)
                                                replyingCommentId = null
                                                replyText = ""
                                            }
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send reply", tint = MaterialTheme.colorScheme.primary)
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
                                                        text = "${r.userName} • ${formatCommentRelativeTime(r.createdAt)}",
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

        Spacer(modifier = Modifier.height(16.dp))

        // Post New Comment Input Row (Moved to bottom)
        if (currentUser != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop" },
                    contentDescription = "My avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )

                OutlinedTextField(
                    value = activeCommentText,
                    onValueChange = { activeCommentText = it },
                    placeholder = { Text("Add a comment...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_comment_input"),
                    shape = RoundedCornerShape(20.dp),
                    trailingIcon = {
                        if (activeCommentText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onPostComment(activeCommentText)
                                    activeCommentText = ""
                                },
                                modifier = Modifier.testTag("submit_comment_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Publish", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                )
            }
        } else {
            Text(
                text = "Please sign in to add a comment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

fun formatCommentNumber(number: Int): String {
    if (number < 1000) return number.toString()
    val k = number / 1000.0
    return String.format("%.1fK", k)
}

fun formatCommentRelativeTime(timeInMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timeInMillis

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hours ago"
        days < 30 -> "$days days ago"
        days < 365 -> "${days / 30} months ago"
        else -> "${days / 365} years ago"
    }
}
