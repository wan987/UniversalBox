package com.universalbox.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS 风格设计规范
 * 
 * 极简、高级、统一的设计系统
 */
object AppTheme {
    
    /**
     * 色彩系统
     */
    object Colors {
        val AppBackground = Color(0xFFF5F5F7)      // 全局背景，极浅冷灰
        val CardBackground = Color(0xFFFFFFFF)     // 纯白卡片
        val InputBackground = Color(0xFFE5E5EA)    // 输入框背景，浅灰
        val PrimaryColor = Color(0xFF007AFF)       // iOS 蓝
        val TextPrimary = Color(0xFF000000)        // 纯黑
        val TextSecondary = Color(0xFF8E8E93)      // 次级文字，中灰
        val Divider = Color(0xFFE5E5EA)            // 分割线
        val Success = Color(0xFF34C759)            // 成功绿
        val Warning = Color(0xFFFF9500)            // 警告橙
        val Error = Color(0xFFFF3B30)              // 错误红
    }
    
    /**
     * 形状系统
     */
    object Shapes {
        val CardShape = RoundedCornerShape(20.dp)      // 卡片圆角
        val ButtonShape = RoundedCornerShape(12.dp)    // 按钮圆角
        val InputShape = RoundedCornerShape(12.dp)     // 输入框圆角
        val SmallShape = RoundedCornerShape(8.dp)      // 小圆角
    }
    
    /**
     * 阴影系统
     * 
     * 使用极低透明度的微弱阴影，避免黑乎乎
     */
    object Shadows {
        val CardElevation = 0.dp                       // 卡片 elevation 为 0
        val CardShadowElevation = 10.dp                // 使用 shadow 的高度
        val CardShadowColor = Color(0x0D000000)        // 极低透明度（5%）
    }
    
    /**
     * 文字样式
     */
    object Typography {
        val LargeTitle = TextStyle(
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Colors.TextPrimary,
            letterSpacing = 0.5.sp
        )
        
        val Title = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Colors.TextPrimary
        )
        
        val Headline = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Colors.TextPrimary
        )
        
        val Body = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = Colors.TextPrimary
        )
        
        val Subheadline = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = Colors.TextSecondary
        )
        
        val Footnote = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Colors.TextSecondary
        )
    }
    
    /**
     * 间距系统
     */
    object Spacing {
        val ExtraSmall = 4.dp
        val Small = 8.dp
        val Medium = 16.dp
        val Large = 24.dp
        val ExtraLarge = 32.dp
    }
    
    /**
     * 输入框颜色配置（无边框样式）
     */
    @Composable
    fun inputFieldColors() = TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedContainerColor = Colors.InputBackground,
        unfocusedContainerColor = Colors.InputBackground,
        disabledContainerColor = Colors.InputBackground.copy(alpha = 0.5f),
        focusedTextColor = Colors.TextPrimary,
        unfocusedTextColor = Colors.TextPrimary,
        cursorColor = Colors.PrimaryColor,
        focusedLabelColor = Colors.TextSecondary,
        unfocusedLabelColor = Colors.TextSecondary
    )
}
