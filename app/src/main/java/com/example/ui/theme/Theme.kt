package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = StreamPrimary,
    background = StreamBackground,
    surface = StreamBackground,
    surfaceVariant = StreamSurfaceVariant,
    onBackground = StreamOnBackground,
    onSurface = StreamOnBackground,
    onSurfaceVariant = StreamOnSurfaceVariant,
    secondaryContainer = StreamSecondaryContainer,
    onPrimary = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = StreamPrimary,
    background = StreamBackground,
    surface = StreamBackground,
    surfaceVariant = StreamSurfaceVariant,
    onBackground = StreamOnBackground,
    onSurface = StreamOnBackground,
    onSurfaceVariant = StreamOnSurfaceVariant,
    secondaryContainer = StreamSecondaryContainer,
    onPrimary = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color disabled to strictly apply the Sophisticated Dark theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> LightColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
