package com.universalbox.app.ui.screens.pomodoro

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.universalbox.app.ui.components.strongBouncyClick
import com.universalbox.app.ui.components.subtleBouncyClick
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * 番茄专注页面 - 极简全屏设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onBack: () -> Unit
) {
    // 状态管理
    var totalSeconds by remember { mutableStateOf(25 * 60) } // 25分钟
    var remainingSeconds by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    // 倒计时逻辑
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            // 倒计时结束
            isRunning = false
            // TODO: 可以添加震动或通知
        }
    }

    // 呼吸动画（数字跳动效果）
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    // 深色护眼背景
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("番茄专注", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2D3440)
                )
            )
        },
        containerColor = Color(0xFF2D3440)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // 圆形进度条 + 倒计时数字
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(300.dp)
                ) {
                    // 背景圆环
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF3A4351),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 16.dp.toPx())
                        )
                    }

                    // 进度圆环
                    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFFFF6B6B),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(
                                width = 16.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // 倒计时数字（带呼吸动画）
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(breathScale)
                    ) {
                        Text(
                            text = formatTime(remainingSeconds),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRunning) "专注中..." else if (isPaused) "已暂停" else "准备开始",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // 控制按钮组
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 重置按钮
                    if (remainingSeconds != totalSeconds) {
                        FloatingActionButton(
                            onClick = {},
                            containerColor = Color(0xFF3A4351),
                            contentColor = Color.White,
                            modifier = Modifier.subtleBouncyClick {
                                isRunning = false
                                isPaused = false
                                remainingSeconds = totalSeconds
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "重置")
                        }
                    }

                    // 主按钮（开始/暂停）
                    FloatingActionButton(
                        onClick = {},
                        modifier = Modifier
                            .size(72.dp)
                            .strongBouncyClick {
                                if (isRunning) {
                                    isRunning = false
                                    isPaused = true
                                } else {
                                    isRunning = true
                                    isPaused = false
                                }
                            },
                        containerColor = if (isRunning) Color(0xFFFFB74D) else Color(0xFFFF6B6B),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "暂停" else "开始",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // 预设时间快捷按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimePresetButton(
                        label = "15分钟",
                        minutes = 15,
                        isActive = !isRunning && totalSeconds == 15 * 60,
                        onClick = {
                            if (!isRunning) {
                                totalSeconds = 15 * 60
                                remainingSeconds = totalSeconds
                            }
                        }
                    )
                    TimePresetButton(
                        label = "25分钟",
                        minutes = 25,
                        isActive = !isRunning && totalSeconds == 25 * 60,
                        onClick = {
                            if (!isRunning) {
                                totalSeconds = 25 * 60
                                remainingSeconds = totalSeconds
                            }
                        }
                    )
                    TimePresetButton(
                        label = "45分钟",
                        minutes = 45,
                        isActive = !isRunning && totalSeconds == 45 * 60,
                        onClick = {
                            if (!isRunning) {
                                totalSeconds = 45 * 60
                                remainingSeconds = totalSeconds
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * 时间预设按钮 - 带果冻效果
 */
@Composable
fun TimePresetButton(
    label: String,
    minutes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color(0xFFFF6B6B) else Color(0xFF3A4351),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .height(40.dp)
            .subtleBouncyClick(onClick = onClick)
    ) {
        Text(label, fontSize = 14.sp)
    }
}

/**
 * 格式化时间显示 (mm:ss)
 */
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
