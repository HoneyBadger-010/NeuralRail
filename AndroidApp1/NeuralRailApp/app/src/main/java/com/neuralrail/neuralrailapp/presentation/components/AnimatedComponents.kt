package com.neuralrail.neuralrailapp.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuralrail.neuralrailapp.presentation.theme.*

// Animated Button with press feedback
@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = BlueAccent,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "btn_scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        interactionSource = interactionSource,
        content = content
    )
}


// Animated Card with press and appear animation
@Composable
fun AnimatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        isVisible = true
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = when { isPressed -> 0.97f; isVisible -> 1f; else -> 0.9f },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "card_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "card_alpha"
    )
    val offsetY by animateIntAsState(
        targetValue = if (isVisible) 0 else 30,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "card_offset"
    )
    
    Surface(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = offsetY.toFloat()
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundCard
    ) {
        content()
    }
}

// Animated Icon Button with bounce effect
@Composable
fun AnimatedIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "icon_scale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        interactionSource = interactionSource
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size))
    }
}

// Animated List Item with stagger effect
@Composable
fun AnimatedListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        isVisible = true
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = when { isPressed -> 0.98f; isVisible -> 1f; else -> 0.95f },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "item_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(250),
        label = "item_alpha"
    )
    val offsetX by animateIntAsState(
        targetValue = if (isVisible) 0 else 50,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 350f),
        label = "item_offset"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationX = offsetX.toFloat()
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        content()
    }
}

// Animated FAB with pulse effect
@Composable
fun AnimatedFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = BlueAccent,
    text: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "fab_scale"
    )
    
    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            containerColor = containerColor,
            contentColor = Color.White,
            interactionSource = interactionSource,
            icon = { Icon(icon, null) },
            text = { Text(text, fontWeight = FontWeight.Bold) }
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            containerColor = containerColor,
            contentColor = Color.White,
            interactionSource = interactionSource
        ) {
            Icon(icon, null)
        }
    }
}

// Screen enter animation wrapper
@Composable
fun AnimatedScreenContent(
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "screen_alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 20f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "screen_offset"
    )
    
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = offsetY
        }
    ) {
        content()
    }
}

// Animated visibility with scale
@Composable
fun AnimatedVisibilityScale(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)) + fadeIn(tween(200)),
        exit = scaleOut(animationSpec = spring(dampingRatio = 0.8f)) + fadeOut(tween(150))
    ) {
        content()
    }
}
