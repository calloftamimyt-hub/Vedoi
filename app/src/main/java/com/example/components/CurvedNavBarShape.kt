package com.example.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class CurvedNavBarShape(private val curveHeightPx: Float, private val curveWidthPx: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val width = size.width
        val height = size.height
        
        // Start top-left
        path.moveTo(0f, curveHeightPx)
        
        // Draw line to the point where the curve should start
        val center = width / 2f
        
        path.lineTo(center - curveWidthPx / 2f, curveHeightPx)
        
        // Draw cubic bezier curve for the bump
        path.cubicTo(
            center - curveWidthPx / 4f, curveHeightPx,
            center - curveWidthPx / 2.5f, 0f,
            center, 0f
        )
        path.cubicTo(
            center + curveWidthPx / 2.5f, 0f,
            center + curveWidthPx / 4f, curveHeightPx,
            center + curveWidthPx / 2f, curveHeightPx
        )
        
        // Draw line to top-right
        path.lineTo(width, curveHeightPx)
        
        // Draw line to bottom-right
        path.lineTo(width, height)
        
        // Draw line to bottom-left
        path.lineTo(0f, height)
        
        // Close path
        path.close()
        
        return Outline.Generic(path)
    }
}
