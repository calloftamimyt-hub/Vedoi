package com.example.screens

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.viewmodel.VideoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class UploadStep {
    CAMERA,
    EDITING,
    UPLOAD_FORM
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UploadScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val gifPermissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    
    var currentStep by remember { mutableStateOf(UploadStep.CAMERA) }
    var selectedVideoMode by remember { mutableStateOf("Shorts") } // "Shorts" (9:16) or "Video" (16:9)
    
    // Video configurations
    var videoDurationSeconds by remember { mutableStateOf(15) }
    var trimRange by remember { mutableStateOf(0f..15f) }
    var cropAspect by remember { mutableStateOf("9:16") }
    var selectedVideoUri by remember { mutableStateOf<String?>(null) }

    // Launcher for selecting video from gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedVideoUri = uri.toString()
            videoDurationSeconds = 45 // Simulated picked video length
            trimRange = 0f..45f
            cropAspect = if (selectedVideoMode == "Shorts") "9:16" else "16:9"
            currentStep = UploadStep.EDITING
            Toast.makeText(context, "Video loaded into Editor!", Toast.LENGTH_SHORT).show()
        }
    }

    // Dynamic camera permission request immediately upon entering
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (currentStep) {
            UploadStep.CAMERA -> {
                CameraScreen(
                    cameraPermissionState = cameraPermissionState,
                    gifPermissionState = gifPermissionState,
                    selectedMode = selectedVideoMode,
                    onModeChange = { selectedVideoMode = it },
                    onStartGalleryPicker = {
                        galleryLauncher.launch("video/*")
                    },
                    onRecordingCompleted = { duration ->
                        videoDurationSeconds = duration
                        trimRange = 0f..duration.toFloat()
                        cropAspect = if (selectedVideoMode == "Shorts") "9:16" else "16:9"
                        currentStep = UploadStep.EDITING
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            UploadStep.EDITING -> {
                VideoEditScreen(
                    videoMode = selectedVideoMode,
                    maxDurationSeconds = videoDurationSeconds.toFloat(),
                    trimRange = trimRange,
                    onTrimRangeChange = { trimRange = it },
                    cropAspect = cropAspect,
                    onCropAspectChange = { cropAspect = it },
                    onBack = { currentStep = UploadStep.CAMERA },
                    onNext = {
                        // Keep state consistent of custom ratio
                        selectedVideoMode = if (cropAspect == "16:9") "Video" else "Shorts"
                        currentStep = UploadStep.UPLOAD_FORM
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            UploadStep.UPLOAD_FORM -> {
                InteractiveUploadFormScreen(
                    viewModel = viewModel,
                    mode = selectedVideoMode,
                    cropAspect = cropAspect,
                    trimmedStart = trimRange.start.toInt(),
                    trimmedEnd = trimRange.endInclusive.toInt(),
                    onBack = { currentStep = UploadStep.EDITING },
                    onUploadSuccess = {
                        currentStep = UploadStep.CAMERA
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    cameraPermissionState: com.google.accompanist.permissions.PermissionState,
    gifPermissionState: com.google.accompanist.permissions.PermissionState,
    selectedMode: String,
    onModeChange: (String) -> Unit,
    onStartGalleryPicker: () -> Unit,
    onRecordingCompleted: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    
    // Recording state
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }

    // Recording clock simulation
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
                if (selectedMode == "Shorts" && recordingSeconds >= 60) {
                    isRecording = false
                    onRecordingCompleted(recordingSeconds)
                }
            }
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        if (cameraPermissionState.status.isGranted) {
            // Live Camera Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, preview
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, preview
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            )
        } else {
            // Graceful Permission Pending Placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Requesting Camera permissions dynamically...", color = Color.White, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Grant Camera Permission")
                    }
                }
            }
        }

        // Top UI Controls Overlay
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { Toast.makeText(context, "Closing Camera Studio", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                // Dynamic timer badge
                if (isRecording) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("00:%02d", recordingSeconds),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = if (selectedMode == "Shorts") "Shorts Mode (Max 60s)" else "Video Mode (Max 10Min)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Lens", tint = Color.White)
                }
            }

            // GIF permission and other creative settings at center-right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Modern Dynamic GIF Permission Button (Requests system storage permission natively)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable {
                                // Direct Dynamic Android Studio Style storage pop-up request
                                gifPermissionState.launchPermissionRequest()
                                if (gifPermissionState.status.isGranted) {
                                    Toast.makeText(context, "GIF Storage Permission Granted!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Launching Dynamic Permission Dialog...", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = if (gifPermissionState.status.isGranted) Icons.Default.GifBox else Icons.Default.Gif,
                            contentDescription = "GIF Permission",
                            tint = if (gifPermissionState.status.isGranted) Color.Green else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GIF Permission",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom camera features selection and shutter trigger
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(bottom = 32.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery selector button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onStartGalleryPicker() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Gallery", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    // Large Shutter Button (Action to record / stop video)
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable {
                                if (isRecording) {
                                    isRecording = false
                                    onRecordingCompleted(if (recordingSeconds > 0) recordingSeconds else 12)
                                } else {
                                    isRecording = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRecording) {
                            // Stop recording icon (Square)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Red)
                            )
                            // Outer recording stroke outline
                            Canvas(modifier = Modifier.size(76.dp)) {
                                drawCircle(
                                    color = Color.Red,
                                    style = Stroke(width = 4.dp.toPx())
                                )
                            }
                        } else {
                            // Record trigger circle
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedMode == "Shorts") Color.Red else Color.White)
                            )
                        }
                    }

                    // Magic Sound Filters selection button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { Toast.makeText(context, "Default Filters Selected", Toast.LENGTH_SHORT).show() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Audiotrack, contentDescription = "Filters", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Add Sound", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Beautiful Sliding Mode Selector (Shorts vs Video 16.9)
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Shorts", "Video").forEach { m ->
                        val isSelected = selectedMode == m
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onModeChange(m) }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = m,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditScreen(
    videoMode: String,
    maxDurationSeconds: Float,
    trimRange: ClosedFloatingPointRange<Float>,
    onTrimRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    cropAspect: String,
    onCropAspectChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                title = { Text("Edit & Polish Creator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    Button(
                        onClick = onNext,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp))
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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Central adaptive frame reflecting aspect ratio & crop bounding options
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Adaptive box aspect container
                val previewRatio = when (cropAspect) {
                    "9:16" -> 9f / 16f
                    "16:9" -> 16f / 9f
                    else -> 1f
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.92f)
                        .aspectRatio(previewRatio)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF212121))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulating a loop clip playing
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "PREVIEW ${cropAspect}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Trimmed time stamp overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format("Trimmed: %ds - %ds", trimRange.start.toInt(), trimRange.endInclusive.toInt()),
                            color = Color.Yellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Trim controls: enable users to make video length smaller or larger
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trim Video Duration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        val diff = trimRange.endInclusive - trimRange.start
                        Text(
                            text = String.format("%02d:%02d", diff.toInt() / 60, diff.toInt() % 60),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Material 3 Range Slider for robust precision trimming
                    RangeSlider(
                        value = trimRange,
                        onValueChange = onTrimRangeChange,
                        valueRange = 0f..maxDurationSeconds,
                        modifier = Modifier.fillMaxWidth().testTag("video_trim_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Start: ${trimRange.start.toInt()}s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Make smaller/larger length-wise", fontSize = 11.sp, color = Color.Gray)
                        Text("End: ${trimRange.endInclusive.toInt()}s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Crop choices container ( presets: 9:16, 16:9, 1:1 Square)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Crop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Crop Aspect Ratio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val crops = listOf(
                            Triple("9:16", "Shorts aspect", Icons.Default.Smartphone),
                            Triple("16:9", "Wide aspect", Icons.Default.Tv),
                            Triple("1:1", "Square aspect", Icons.Default.CropSquare)
                        )
                        crops.forEach { item ->
                            val isSelected = cropAspect == item.first
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { onCropAspectChange(item.first) }
                                    .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = item.third,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.first,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveUploadFormScreen(
    viewModel: VideoViewModel,
    mode: String,
    cropAspect: String,
    trimmedStart: Int,
    trimmedEnd: Int,
    onBack: () -> Unit,
    onUploadSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tech") }
    var selectedAudience by remember { mutableStateOf("Public") }
    var commentsEnabled by remember { mutableStateOf(true) }

    // Upload Pop-up Dialog State
    var showProgressDialog by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf("Initializing files...") }
    var currentProgressPercentage by remember { mutableStateOf(0f) }
    var uploadFinishedStatus by remember { mutableStateOf(false) }

    // Start publishing action
    val onPublishAction: () -> Unit = {
        if (title.trim().isEmpty()) {
            Toast.makeText(context, "Please write a title before publishing!", Toast.LENGTH_SHORT).show()
        } else {
            showProgressDialog = true
            uploadFinishedStatus = false
            currentProgressPercentage = 0f
            
            // Simulating real S3 Cloudflare upload interaction progress
            coroutineScope.launch {
                uploadStatusMessage = "Connecting to ViewTube servers..."
                delay(1000)
                
                uploadStatusMessage = "Uploading video segments and processing media..."
                while (currentProgressPercentage < 0.95f) {
                    delay(300)
                    currentProgressPercentage = (currentProgressPercentage + 0.15f).coerceAtMost(0.95f)
                }
                
                uploadStatusMessage = "Registering video metadata..."
                delay(800)
                currentProgressPercentage = 1.0f
                uploadFinishedStatus = true
                uploadStatusMessage = "Video published successfully!"
                
                // Fire the live repo insertion
                val formattedDuration = String.format("%02d:%02d", (trimmedEnd - trimmedStart) / 60, (trimmedEnd - trimmedStart) % 60)
                viewModel.requestVideoUpload(
                    title,
                    description.ifEmpty { "Created with Creator Studio. #$selectedCategory" },
                    selectedCategory,
                    formattedDuration,
                    (mode == "Shorts"),
                    context
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(text = if (mode == "Shorts") "TikTok Shorts Publisher" else "YouTube Video Publisher", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        
        if (mode == "Shorts") {
            // TikTok (Shorts 9:16) Style: Side-by-Side layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        // Horizontal Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Column: Small portrait video preview card
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(178.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoCall,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${trimmedEnd - trimmedStart}s",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Right Column: Title input and description
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Shorts Title") },
                                    placeholder = { Text("What is this short about?") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2,
                                    singleLine = false
                                )

                                OutlinedTextField(
                                    value = hashtags,
                                    onValueChange = { hashtags = it },
                                    label = { Text("Hashtags") },
                                    placeholder = { Text("#viral #trending #shorts") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    item {
                        // Category & Extra Options Box
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Shorts Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            val categories = listOf("Tech", "Coding", "Music", "Food", "Lifestyle", "Comedy")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { cat ->
                                    val isSelected = selectedCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Settings Switches
                            ListItem(
                                leadingContent = { Icon(Icons.Default.Visibility, contentDescription = null) },
                                headlineContent = { Text("Audience Visibility") },
                                supportingContent = { Text(selectedAudience) },
                                trailingContent = {
                                    Switch(
                                        checked = selectedAudience == "Public",
                                        onCheckedChange = { checked ->
                                            selectedAudience = if (checked) "Public" else "Private"
                                        }
                                    )
                                }
                            )

                            ListItem(
                                leadingContent = { Icon(Icons.Default.Chat, contentDescription = null) },
                                headlineContent = { Text("Comments section") },
                                supportingContent = { Text(if (commentsEnabled) "Comments Enabled" else "Comments Disabled") },
                                trailingContent = {
                                    Switch(
                                        checked = commentsEnabled,
                                        onCheckedChange = { checked ->
                                            commentsEnabled = checked
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                // Publish action call
                Button(
                    onClick = onPublishAction,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Short", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        } else {
            // 16:9 YouTube Long Video Upload style
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top section: Large 16:9 video preview card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Long format: 16:9 Landscape Card", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = String.format("%02d:%02d", (trimmedEnd - trimmedStart) / 60, (trimmedEnd - trimmedStart) % 60),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Inputs
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Video Title") },
                        placeholder = { Text("Give an amazing, click-worthy title...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Video Description") },
                        placeholder = { Text("Explain what happens in this video, add credit info, details...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Category Selection", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val categories = listOf("Tech", "Coding", "Music", "Food", "Lifestyle", "Comedy", "Education")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Privacy & Visibility", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf("Public", "Unlisted", "Private").forEach { aud ->
                                    val isSelected = selectedAudience == aud
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                            .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .clickable { selectedAudience = aud }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            aud,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onPublishAction,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publish Video", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Cloudflare R2 Connection & Upload Pop-up Progress Dialog
    if (showProgressDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss while uploading */ },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                if (uploadFinishedStatus) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(42.dp))
                } else {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                }
            },
            title = {
                Text(
                    text = if (uploadFinishedStatus) "Published!" else "Uploading to ViewTube",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = uploadStatusMessage,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (!uploadFinishedStatus) {
                        LinearProgressIndicator(
                            progress = { currentProgressPercentage },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "${(currentProgressPercentage * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Your media file is completely uploaded to ViewTube. Your subscribers can view it immediately!",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                if (uploadFinishedStatus) {
                    Button(
                        onClick = {
                            showProgressDialog = false
                            onUploadSuccess()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        )
    }
}
