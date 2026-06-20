package com.example.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.utils.ImageUtils
import com.example.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChannelScreen(
    viewModel: VideoViewModel,
    onNavigateBack: () -> Unit,
    onChannelCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    
    var channelName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var username by remember { mutableStateOf(currentUser?.displayName?.lowercase()?.replace(" ", "") ?: "") }
    var avatarUrl by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var channelKeywords by remember { mutableStateOf("") }
    var channelCategory by remember { mutableStateOf("Education") }
    
    var suggestedUsername by remember { mutableStateOf("") }
    var usernameExists by remember { mutableStateOf(false) }

    // Mock existing usernames
    val existingUsernames = listOf("tamimahmed", "john", "admin", "test")

    LaunchedEffect(username) {
        if (existingUsernames.contains(username.trim().lowercase())) {
            usernameExists = true
            suggestedUsername = "${username.trim().lowercase()}${(10..99).random()}"
        } else {
            usernameExists = false
            suggestedUsername = ""
        }
    }
    
    val categories = listOf("Education", "Entertainment", "Gaming", "Music", "News", "Sports", "Tech", "Lifestyle")
    var categoryExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var selectedUriForCrop by remember { mutableStateOf<android.net.Uri?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedUriForCrop = uri
            showCropDialog = true
        }
    }

    if (showCropDialog && selectedUriForCrop != null) {
        ImageCropDialog(
            imageUri = selectedUriForCrop!!,
            onDismiss = { showCropDialog = false },
            onCropped = { croppedPath ->
                avatarUrl = croppedPath
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Channel") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = channelName,
                onValueChange = { channelName = it },
                label = { Text("Channel Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Channel Handle (Username)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = usernameExists
                )
                if (usernameExists) {
                    Text(
                        text = "Handle already exists. Suggestion: $suggestedUsername",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                    )
                }
            }

            // Circular profile selection with Camera Badge
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Channel Logo Picture",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { avatarLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Initials or placeholder character when no image is uploaded
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (channelName.isNotEmpty()) channelName.take(1).uppercase() else "C",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    // Camera badge overlaid
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Pick Image",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to choose a photo from the gallery and crop",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Channel Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = channelKeywords,
                onValueChange = { channelKeywords = it },
                label = { Text("Channel Keywords (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = channelCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Channel Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                channelCategory = selectionOption
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.createChannel(channelName, username, avatarUrl, bio, channelKeywords, channelCategory)
                    onChannelCreated()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = channelName.isNotBlank() && username.isNotBlank() && !usernameExists
            ) {
                Text("Create Channel")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private @Composable
fun ImageCropDialog(
    imageUri: android.net.Uri,
    onDismiss: () -> Unit,
    onCropped: (String) -> Unit
) {
    val context = LocalContext.current
    var zoomScale by remember { mutableStateOf(1f) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Crop Profile photo",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            ) 
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Pinch/Zoom and center your brand's face logo inside the crop mark.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Crop preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(maxOf(0.dp, 15.dp * (zoomScale - 1f))) // inverse scale bounds simulation
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut, 
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = zoomScale,
                        onValueChange = { zoomScale = it },
                        valueRange = 1f..3f,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ZoomIn, 
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val croppedPath = ImageUtils.cropAndSaveImage(context, imageUri, zoomScale)
                    onCropped(croppedPath)
                    onDismiss()
                }
            ) {
                Text("Crop & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

