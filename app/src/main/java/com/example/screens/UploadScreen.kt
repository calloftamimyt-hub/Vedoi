package com.example.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val uploadTasks by viewModel.uploadTasks.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Coding") }
    var duration by remember { mutableStateOf("05:30") }

    var expandedDropdown by remember { mutableStateOf(false) }
    val categoriesList = listOf("Coding", "Tech", "Music", "Food", "Lifestyle")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Studio Upload", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Background Upload Active Queue
                if (uploadTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active Upload Queue",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(uploadTasks) { task ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isFailed)
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_task_${task.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (task.isCompleted) Icons.Default.CloudDone else if (task.isFailed) Icons.Default.CloudOff else Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else if (task.isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = task.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Action buttons for clear completed or retry failed
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

                                // Progress Indicator row
                                if (!task.isCompleted && !task.isFailed) {
                                    LinearProgressIndicator(
                                        progress = task.progress,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Uploading... ${(task.progress * 100).toInt()}% with Cloudflare R2",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (task.isFailed) {
                                    Text(
                                        text = "Upload Interrupted! Cloudflare R2 bucket connection failed. Click retry.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text(
                                        text = "Completed! Video posted live to feed.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }

                // New Upload Form
                item {
                    Text(
                        text = "Upload Video Metadata",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Video Title") },
                            placeholder = { Text("Enter descriptive title...") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_title_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Video Description") },
                            placeholder = { Text("What is this video talking about?") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        // Duration Picker
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Video Duration (Format: MM:SS)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Category Dropdown spinner
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                label = { Text("Content Category") },
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedDropdown = !expandedDropdown },
                                trailingIcon = {
                                    IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Expand category dropdown")
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                categoriesList.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(text = c) },
                                        onClick = {
                                            category = c
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Custom Tags Input
                        var customTags by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = customTags,
                            onValueChange = { customTags = it },
                            label = { Text("Custom Tags (comma separated)") },
                            placeholder = { Text("e.g. android, compose, tutorial") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        var isGenerating by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                if (title.trim().isNotEmpty() && description.trim().isNotEmpty()) {
                                    isGenerating = true
                                    viewModel.generateMetadataWithGemini(title, description) { genCategory, genTags ->
                                        category = genCategory
                                        customTags = genTags
                                        isGenerating = false
                                    }
                                } else {
                                    Toast.makeText(context, "Fill title and description first to generate", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text("Generate AI Metadata")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (title.trim().isEmpty() || description.trim().isEmpty()) {
                                    Toast.makeText(context, "Please fulfill all required metadata fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.requestVideoUpload(title, description, category, duration)
                                Toast.makeText(context, "Dispatched Cloudflare R2 Upload worker!", Toast.LENGTH_SHORT).show()
                                title = ""
                                description = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("upload_submit_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Publish, contentDescription = null)
                                Text("Publish to ViewTube", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
