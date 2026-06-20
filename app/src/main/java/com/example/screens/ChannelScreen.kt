package com.example.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.components.VideoItemCard
import com.example.model.Video
import com.example.utils.ImageUtils
import com.example.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    viewModel: VideoViewModel,
    channelId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onVideoClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
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

    // Navigation Tab state
    var selectedTab by remember { mutableStateOf(0) }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Channel metadata values
    var editName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editAvatar by remember { mutableStateOf("") }
    var editBanner by remember { mutableStateOf("") }

    // Hydrate fields initially
    LaunchedEffect(currentUser, isEditingProfile) {
        if (currentUser != null && isEditingProfile) {
            editName = currentUser?.displayName ?: ""
            editBio = currentUser?.bio ?: ""
            editAvatar = currentUser?.avatarUrl ?: ""
            editBanner = currentUser?.bannerUrl ?: ""
        }
    }

    // Determine current user Profile vs external mock channels
    val channelDisplayName = if (channelId == "user_me" || channelId == currentUser?.id) {
        currentUser?.displayName ?: "My Channel"
    } else {
        channelId.replace("_", " ").split(" ").joinToString(" ") { it.capitalize() }
    }

    val channelAvatarUrl = if (channelId == "user_me" || channelId == currentUser?.id) {
        currentUser?.avatarUrl ?: ""
    } else {
        // Mock profile image generators based on ID
        "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=120&auto=format&fit=crop"
    }

    val channelBannerUrl = if (channelId == "user_me" || channelId == currentUser?.id) {
        currentUser?.bannerUrl ?: ""
    } else {
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop"
    }

    val channelBio = if (channelId == "user_me" || channelId == currentUser?.id) {
        currentUser?.bio ?: "No bio yet."
    } else {
        "Official Youtube style channel stream sharing amazing, resilient creative videos globally."
    }

    // Filter videos created by this channel specifically
    val channelVideos = allVideos.filter {
        it.channelId == channelId || 
        (channelId == "user_me" && it.channelId == currentUser?.id)
    }

    if (showCropDialog && selectedUriForCrop != null) {
        ImageCropDialog(
            imageUri = selectedUriForCrop!!,
            onDismiss = { showCropDialog = false },
            onCropped = { croppedPath ->
                if (isEditingProfile) {
                    editAvatar = croppedPath
                } else {
                    viewModel.configureChannel(channelDisplayName, channelBio, croppedPath, channelBannerUrl)
                    Toast.makeText(context, "Logo updated and saved successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = channelDisplayName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    if (channelId == "user_me" || channelId == currentUser?.id) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                        }
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
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // 1. Channel Banner Image Area
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = channelBannerUrl.ifEmpty { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop" },
                            contentDescription = "Channel Banner Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 2. Overlapping Channel Avatar & Basic Details
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .offset(y = (-30).dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = channelId == "user_me" || channelId == currentUser?.id) {
                                        avatarLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (channelAvatarUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = channelAvatarUrl,
                                        contentDescription = "Channel Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (channelDisplayName.isNotEmpty()) channelDisplayName.take(1).uppercase() else "C",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                if (channelId == "user_me" || channelId == currentUser?.id) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Edit Profile Picture",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                Text(
                                    text = channelDisplayName,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@$channelId • ${channelVideos.size} videos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render action buttons: Edit Channel (if me)
                        if (channelId == "user_me" || channelId == currentUser?.id) {
                            if (!isEditingProfile) {
                                Button(
                                    onClick = { isEditingProfile = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("Customize Channel Metadata", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        } else {
                            val subbed = viewModel.isSubscribed(channelId)
                            var pushEnabled by remember { mutableStateOf(false) }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        viewModel.toggleSubscribeChannel(channelId)
                                        if (!subbed) pushEnabled = true // Enable by default when subscribing
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (subbed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.error,
                                        contentColor = if (subbed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                    )
                                ) {
                                    Text(text = if (subbed) "Following" else "Follow", fontWeight = FontWeight.Bold)
                                }

                                if (subbed) {
                                    IconButton(
                                        onClick = { pushEnabled = !pushEnabled },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (pushEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                            contentDescription = "Toggle push notifications",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Customize Channel Info inline Sheet (if editing is active)
                if (isEditingProfile) {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .offset(y = (-20).dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Customize My Brand", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    label = { Text("Display Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editBio,
                                    onValueChange = { editBio = it },
                                    label = { Text("Channel About / Bio") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )

                                OutlinedTextField(
                                    value = editAvatar,
                                    onValueChange = { editAvatar = it },
                                    label = { Text("Profile Pic Web URL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editBanner,
                                    onValueChange = { editBanner = it },
                                    label = { Text("Channel Banner Web URL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { isEditingProfile = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.configureChannel(editName, editBio, editAvatar, editBanner)
                                            isEditingProfile = false
                                            Toast.makeText(context, "Channel Saved Successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save")
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Scroll tabs: Videos vs About Section
                item {
                    val isMyChannel = channelId == "user_me" || channelId == currentUser?.id
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.offset(y = (-15).dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Videos (${channelVideos.size})", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("About", fontWeight = FontWeight.Bold) }
                        )
                        if (isMyChannel) {
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = { Text("History", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                text = { Text("Analytics", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // 5. Render active Tab items
                if (selectedTab == 0) {
                    if (channelVideos.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "This channel hasn't uploaded any videos yet.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(channelVideos) { v ->
                            VideoItemCard(
                                video = v,
                                onClick = { onVideoClick(v) },
                                onChannelClick = {}
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        }
                    }
                } else if (selectedTab == 1) {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .offset(y = (-5).dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "About", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(text = channelBio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(text = "Channel ID: @$channelId", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                } else if (selectedTab == 2) {
                    // History Tab
                    if (watchHistory.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No watch history yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(watchHistory) { video ->
                            VideoItemCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onChannelClick = {}
                            )
                            HorizontalDivider()
                        }
                    }
                } else if (selectedTab == 3) {
                    // Analytics stub using simple bar charts with Box
                    item {
                        Card(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .offset(y = (-5).dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "Channel Analytics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                
                                Text("Audience Retention", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val retentionData = listOf(0.9f, 0.85f, 0.7f, 0.65f, 0.5f, 0.45f, 0.4f, 0.35f)
                                    retentionData.forEach { factor ->
                                        Box(modifier = Modifier.weight(1f).fillMaxHeight(factor).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Click-Through Rate (CTR)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Impressions: 12.5k", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("6.8%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                LinearProgressIndicator(
                                    progress = 0.068f,
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
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

