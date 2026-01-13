package com.universalbox.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.universalbox.app.ui.theme.AppTheme

/**
 * iOS 风格的页面顶栏
 * 
 * 大标题 + 圆形返回按钮
 */
@Composable
fun IOSTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.Spacing.Large)
            .padding(top = AppTheme.Spacing.Large, bottom = AppTheme.Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圆形返回按钮
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(
                    elevation = AppTheme.Shadows.CardShadowElevation,
                    shape = CircleShape,
                    spotColor = AppTheme.Shadows.CardShadowColor
                )
                .background(
                    color = AppTheme.Colors.CardBackground,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = AppTheme.Colors.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(AppTheme.Spacing.Medium))
        
        // 大标题
        Text(
            text = title,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.Colors.TextPrimary,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * iOS 风格的卡片（带微弱阴影）
 */
@Composable
fun IOSCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = AppTheme.Shadows.CardShadowElevation,
                shape = AppTheme.Shapes.CardShape,
                spotColor = AppTheme.Shadows.CardShadowColor
            )
            .background(
                color = AppTheme.Colors.CardBackground,
                shape = AppTheme.Shapes.CardShape
            )
            .padding(AppTheme.Spacing.Large),
        content = content
    )
}

/**
 * iOS 风格的输入框（无边框）
 */
@Composable
fun IOSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = AppTheme.Typography.Footnote,
                color = AppTheme.Colors.TextSecondary,
                modifier = Modifier.padding(bottom = AppTheme.Spacing.Small)
            )
        }
        
        androidx.compose.material3.TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = AppTheme.Colors.TextSecondary
                )
            },
            colors = AppTheme.inputFieldColors(),
            shape = AppTheme.Shapes.InputShape,
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
