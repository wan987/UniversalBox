package com.universalbox.app.ui.screens.tools

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.universalbox.app.R
import com.universalbox.app.ui.components.strongBouncyClick
import com.universalbox.app.ui.theme.ToolsBrush

/**
 * 工具页 - 深色渐变风格
 */
@Composable
fun ToolsScreen(
    onNavigateToPomodoro: () -> Unit,
    onNavigateToOCR: () -> Unit,
    onNavigateToQRCode: () -> Unit,
    onNavigateToZenClock: () -> Unit,
    onNavigateToDecisionMaker: () -> Unit
) {
    // 缓存图片资源ID，避免重复创建
    val toolsData = remember {
        listOf(
            Triple("番茄钟", R.drawable.img_3d_tomatoclock, listOf(Color(0xFFFF6B6B), Color(0xFFEE5A6F))),
            Triple("OCR识别", R.drawable.img_3d_ocr, listOf(Color(0xFF4ECDC4), Color(0xFF44A08D))),
            Triple("二维码", R.drawable.img_3d_qrcode, listOf(Color(0xFF8E54E9), Color(0xFF6F42C1))),
            Triple("全屏时钟", R.drawable.img_3d_clock, listOf(Color(0xFFFFA726), Color(0xFFFB8C00))),
            Triple("帮我决定", R.drawable.img_3d_decision, listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
        )
    }
    
    // 单页展示，避免二级分页
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ToolsBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "实用工具",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 32.dp, bottom = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ToolCard(
                        title = "番茄钟",
                        imageRes = R.drawable.img_3d_tomatoclock,
                        gradient = listOf(Color(0xFFFF6B6B), Color(0xFFEE5A6F)),
                        onClick = onNavigateToPomodoro,
                        modifier = Modifier.weight(1f)
                    )
                    ToolCard(
                        title = "OCR识别",
                        imageRes = R.drawable.img_3d_ocr,
                        gradient = listOf(Color(0xFF4ECDC4), Color(0xFF44A08D)),
                        onClick = onNavigateToOCR,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ToolCard(
                        title = "二维码",
                        imageRes = R.drawable.img_3d_qrcode,
                        gradient = listOf(Color(0xFF8E54E9), Color(0xFF6F42C1)),
                        onClick = onNavigateToQRCode,
                        modifier = Modifier.weight(1f)
                    )
                    ToolCard(
                        title = "全屏时钟",
                        imageRes = R.drawable.img_3d_clock,
                        gradient = listOf(Color(0xFFFFA726), Color(0xFFFB8C00)),
                        onClick = onNavigateToZenClock,
                        modifier = Modifier.weight(1f)
                    )
                }

                ToolCard(
                    title = "帮我决定",
                    imageRes = R.drawable.img_3d_decision,
                    gradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                    onClick = onNavigateToDecisionMaker,
                    modifier = Modifier.fillMaxWidth(),
                    isSquare = true
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "💡 点击卡片即可使用工具",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ToolCard(
    title: String,
    imageRes: Int,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSquare: Boolean = false
) {
    // 渐变色容器
    Column(
        modifier = modifier
            .height(160.dp)
            .background(
                brush = Brush.linearGradient(colors = gradient),
                shape = RoundedCornerShape(24.dp)
            )
            .strongBouncyClick(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 放大图标到80dp，更显眼
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 白色文字，适配渐变背景
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
