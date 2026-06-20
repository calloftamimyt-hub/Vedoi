package com.example.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.viewmodel.VideoViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    viewModel: VideoViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val authError by viewModel.authError.collectAsState()

    // Screen views: 0 = Login, 1 = Sign Up, 2 = Forgot Password
    var activeMode by remember { mutableStateOf(0) }

    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Redirect automatically if logged in
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onAuthSuccess()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        errorBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ViewTube Logo Brand Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "ViewTube Logo",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ViewTube",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Join the global video sharing ecosystem",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Form Content card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = when (activeMode) {
                            0 -> "Sign In"
                            1 -> "Create Account"
                            else -> "Recover Password"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Error feeds
                    if (authError != null || feedbackMessage != null) {
                        Surface(
                            color = if (authError != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authError ?: feedbackMessage ?: "",
                                color = if (authError != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Fields
                    if (activeMode == 1) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                placeholder = { Text("First Name", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                                colors = textFieldColors,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("firstname_input")
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                placeholder = { Text("Last Name", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                                colors = textFieldColors,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("lastname_input")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email Address or Phone Number", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("email_input")
                    )

                    if (activeMode != 2) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Password", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                            colors = textFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("password_input")
                        )
                    }

                    if (activeMode == 1) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("Confirm Password", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                            colors = textFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("confirm_password_input")
                        )
                    }

                    // Forgot password link
                    if (activeMode == 0) {
                        TextButton(
                            onClick = { activeMode = 2 },
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(32.dp)
                                .offset(y = (-6).dp)
                        ) {
                            Text("Forgot Password?", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }
                    }

                    // Main Action Button (Thinner)
                    Button(
                        onClick = {
                            feedbackMessage = null
                            if (activeMode == 0) {
                                viewModel.performLogin(email, password)
                            } else if (activeMode == 1) {
                                if (password != confirmPassword) {
                                    feedbackMessage = "Passwords do not match."
                                } else {
                                    viewModel.performSignUp(email, "$firstName $lastName".trim(), password)
                                }
                            } else {
                                viewModel.performForgotPassword(email)
                                feedbackMessage = "Recovery credentials link dispatched successfully!"
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("submit_button"),
                        enabled = !isAuthenticating
                    ) {
                        if (isAuthenticating) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text("Please wait...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            Text(
                                text = when (activeMode) {
                                    0 -> "Login"
                                    1 -> "Sign Up"
                                    else -> "Send Recovery Link"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (activeMode != 2) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Text(" or ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }

                        // Google button (Sleeker & Thinner)
                        OutlinedButton(
                            onClick = { 
                                viewModel.performGoogleSignIn() 
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color(0xFFEA4335), shape = RoundedCornerShape(4.dp))
                                        .padding(2.dp)
                                ) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Continue with Google", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // Mode toggles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (activeMode) {
                                0 -> "Don't have an account?"
                                1 -> "Already have an account?"
                                else -> "Recall details?"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        TextButton(
                            onClick = {
                                feedbackMessage = null
                                activeMode = when (activeMode) {
                                    0 -> 1
                                    1 -> 0
                                    else -> 0
                                }
                            }
                        ) {
                            Text(
                                text = when (activeMode) {
                                    0 -> "Sign Up"
                                    1 -> "Login"
                                    else -> "Back to Login"
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
