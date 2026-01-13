package com.universalbox.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView

/**
 * 果冻按钮效果 - Apple 风格的物理回弹质感
 * 
 * 特性：
 * - 按压时缩放到 0.95 倍（FastOutSlowIn，100ms）
 * - 松开时弹簧回弹到 1.0 倍（Spring 动画，明显阻尼）
 * - 触感反馈（轻微震动）
 */
fun Modifier.bouncyClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    
    // 弹簧动画规范
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = if (isPressed) {
            // 按下：快速缩小
            tween(
                durationMillis = 100,
                easing = FastOutSlowInEasing
            )
        } else {
            // 松开：弹簧回弹
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "bouncyScale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (enabled) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        // 触发震动反馈
                        view.performHapticFeedback(
                            android.view.HapticFeedbackConstants.CONTEXT_CLICK
                        )
                        
                        // 等待手指松开
                        val released = tryAwaitRelease()
                        isPressed = false
                        
                        if (released) {
                            onClick()
                        }
                    }
                )
            }
        }
}

/**
 * 更强烈的果冻效果（用于主要按钮）
 */
fun Modifier.strongBouncyClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = if (isPressed) {
            tween(
                durationMillis = 80,
                easing = FastOutSlowInEasing
            )
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "strongBouncyScale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (enabled) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        // 更强的震动
                        view.performHapticFeedback(
                            android.view.HapticFeedbackConstants.KEYBOARD_TAP
                        )
                        
                        val released = tryAwaitRelease()
                        isPressed = false
                        
                        if (released) {
                            onClick()
                        }
                    }
                )
            }
        }
}

/**
 * 轻微的果冻效果（用于小按钮或图标）
 */
fun Modifier.subtleBouncyClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = if (isPressed) {
            tween(
                durationMillis = 100,
                easing = FastOutSlowInEasing
            )
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "subtleBouncyScale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (enabled) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(
                            android.view.HapticFeedbackConstants.CLOCK_TICK
                        )
                        
                        val released = tryAwaitRelease()
                        isPressed = false
                        
                        if (released) {
                            onClick()
                        }
                    }
                )
            }
        }
}
