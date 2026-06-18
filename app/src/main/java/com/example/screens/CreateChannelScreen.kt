package com.example.screens

import androidx.compose.foundation.layout.*
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
                .padding(16.dp),
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
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = usernameExists
                )
                if (usernameExists) {
                    Text(
                        text = "Username already exists. Suggestion: $suggestedUsername",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.createChannel(channelName, username)
                    onChannelCreated()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = channelName.isNotBlank() && username.isNotBlank() && !usernameExists
            ) {
                Text("Create Channel")
            }
        }
    }
}
