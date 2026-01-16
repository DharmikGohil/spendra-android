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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
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

/**
 * SuggestedActionCard: Prompts the user to take a specific financial action.
 */
@Composable
fun SuggestedActionCard(
    title: String,
    description: String,
    primaryActionLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    SpendraCard(modifier = modifier) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(modifier = Modifier.padding(end = 8.dp)) { icon() }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SpendraBlack
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = SpendraBlack
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 8.dp))
            SpendraButton(
                onClick = onPrimaryClick,
                text = primaryActionLabel,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

/**
 * BudgetRecommendationCard: Shows a suggested budget vs average spend.
 */
@Composable
fun BudgetRecommendationCard(
    categoryName: String,
    suggestedAmount: Double,
    averageSpend: Double,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    SpendraCard(modifier = modifier) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Budget for $categoryName",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SpendraBlack
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 8.dp))
            
            // Comparison Visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                androidx.compose.foundation.layout.Column {
                    Text(text = "Avg Spend", style = MaterialTheme.typography.labelSmall)
                    Text(text = "₹${averageSpend.toInt()}", style = MaterialTheme.typography.bodyLarge)
                }
                androidx.compose.material.icons.Icons.Default.ArrowForward.let { 
                    androidx.compose.material3.Icon(it, contentDescription = null) 
                }
                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Suggested", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "₹${suggestedAmount.toInt()}", 
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 12.dp))
            SpendraButton(
                onClick = onApply,
                text = "Apply Limit",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * AlertBanner: High visibility alert for critical info.
 */
@Composable
fun AlertBanner(
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val backgroundColor = if (isError) Color(0xFFFFCDD2) else Color(0xFFFFF9C4) // Red-ish or Yellow-ish
    val borderColor = SpendraBlack
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material.icons.Icons.Default.Warning.let {
                androidx.compose.material3.Icon(it, contentDescription = null, tint = SpendraBlack)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = SpendraBlack
            )
        }
    }
}
