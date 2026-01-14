package com.dharmikgohil.spendra.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dharmikgohil.spendra.ui.theme.SpendraBlack
import com.dharmikgohil.spendra.ui.theme.SpendraGray
import com.dharmikgohil.spendra.ui.theme.SpendraWhite

/**
 * SpendraCard: A Neo-Brutalist card with a thick border and hard shadow.
 */
@Composable
fun SpendraCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SpendraWhite,
    borderColor: Color = SpendraBlack,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        // Hard Shadow (Bottom Layer)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(color = borderColor, shape = shape)
        )

        // Card Content (Top Layer)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = backgroundColor,
            border = BorderStroke(borderWidth, borderColor),
            content = content
        )
    }
}

/**
 * SpendraButton: A Neo-Brutalist button with press animation.
 */
@Composable
fun SpendraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderColor: Color = SpendraBlack,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate offset when pressed
    val currentOffset = if (isPressed) 0.dp else 4.dp
    
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default ripple
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Shadow Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(color = borderColor, shape = RoundedCornerShape(8.dp))
        )
        
        // Content Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = if (isPressed) 4.dp else 0.dp, y = if (isPressed) 4.dp else 0.dp)
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .background(containerColor, RoundedCornerShape(8.dp))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}
