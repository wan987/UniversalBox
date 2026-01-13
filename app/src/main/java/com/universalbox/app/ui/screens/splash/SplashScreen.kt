package com.universalbox.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.universalbox.app.ui.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * 启动页面 - 带 Lottie 动画
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Lottie 动画组合状态
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.universalbox.app.R.raw.happy_new_year_cat_jumping)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1, // 播放一次
        speed = 1f
    )

    // 渐入渐出动画
    val alpha by animateFloatAsState(
        targetValue = if (progress < 0.9f) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "splashAlpha"
    )

    // 监听动画完成
    LaunchedEffect(progress) {
        if (progress >= 0.99f) {
            delay(300) // 等待渐出动画
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.Colors.AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Lottie 动画
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(280.dp)
            )

            // App 名称
            Text(
                text = "UniversalBox",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.Colors.TextPrimary
            )

            Text(
                text = "你的数字百宝箱",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.weight(1f))

            // 底部文字
            Text(
                text = "2026 · Made with ❤️",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        }
    }
}
