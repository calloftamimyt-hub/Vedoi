package com.example.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

            OutlinedTextField(
                value = avatarUrl,
                onValueChange = { avatarUrl = it },
                label = { Text("Profile Picture URL (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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
