package com.universalbox.app.ui.screens.tools

import android.view.WindowManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * ZenClockScreen - 全屏时钟
 * 
 * 灵感来自 iOS 17 Standby 模式
 * 特性：
 * - 纯黑背景 + 巨大时间显示
 * - 屏幕常亮
 * - 点击切换颜色
 * - 秒针跳动效果（冒号闪烁）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenClockScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // 时间状态
    var currentTime by remember { mutableStateOf("00:00") }
    var showColon by remember { mutableStateOf(true) }
    
    // 颜色状态
    val colors = listOf(
        Color.White,
        Color(0xFFFF6B6B), // 红色
        Color(0xFF4ECDC4), // 青色
        Color(0xFFFFBE0B)  // 橙色
    )
    var currentColorIndex by remember { mutableStateOf(0) }
    
    // 保持屏幕常亮
    DisposableEffect(Unit) {
        val activity = (context as? androidx.activity.ComponentActivity)
        val window = activity?.window
        val insetsController = window?.let { WindowInsetsControllerCompat(it, it.decorView) }

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.statusBarColor = android.graphics.Color.BLACK
        window?.navigationBarColor = android.graphics.Color.BLACK
        insetsController?.isAppearanceLightStatusBars = false
        insetsController?.isAppearanceLightNavigationBars = false
        // 进入沉浸式：隐藏导航栏和状态栏
        window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController?.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // 恢复系统栏
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
            window?.let { WindowCompat.setDecorFitsSystemWindows(it, true) }
        }
    }
    
    // 更新时间
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            currentTime = sdf.format(Date())
            showColon = !showColon // 冒号闪烁
            delay(1000)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                currentColorIndex = (currentColorIndex + 1) % colors.size
            }
    ) {
        // 返回按钮浮层，避免出现顶栏
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = colors[currentColorIndex]
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = if (showColon) currentTime else currentTime.replace(":", " "),
                fontSize = 120.sp,
                fontWeight = FontWeight.Black,
                color = colors[currentColorIndex],
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateFormat = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE)
                Text(
                    text = dateFormat.format(Date()),
                    fontSize = 20.sp,
                    color = colors[currentColorIndex].copy(alpha = 0.6f),
                    fontWeight = FontWeight.Light
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (showColon)
                                colors[currentColorIndex]
                            else
                                colors[currentColorIndex].copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        )
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "轻触屏幕切换颜色",
                fontSize = 14.sp,
                color = colors[currentColorIndex].copy(alpha = 0.4f),
                fontWeight = FontWeight.Light
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            SecondProgressIndicator(
                color = colors[currentColorIndex],
                showColon = showColon
            )
        }
    }
}

/**
 * 秒数进度指示器 - 60秒循环的进度条
 */
@Composable
private fun SecondProgressIndicator(
    color: Color,
    showColon: Boolean
) {
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(showColon) {
        val calendar = Calendar.getInstance()
        val seconds = calendar.get(Calendar.SECOND)
        progress = seconds / 60f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ),
        label = "progress"
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(60) { index ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (index % 5 == 0) 12.dp else 8.dp)
                    .background(
                        color = if (index / 60f <= animatedProgress)
                            color.copy(alpha = 0.8f)
                        else
                            color.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.extraSmall
                    )
            )
        }
    }
}
