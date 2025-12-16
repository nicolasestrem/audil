package com.audil.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AudilCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    elevation: Dp = 0.dp, // Flat by default
    border: BorderStroke? = null, // No border by default for cleaner look, or subtle
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp), // Slightly tighter radius
        color = color,
        tonalElevation = elevation,
        shadowElevation = elevation,
        border = border ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
