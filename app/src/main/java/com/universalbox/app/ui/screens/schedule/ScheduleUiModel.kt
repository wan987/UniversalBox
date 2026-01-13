package com.universalbox.app.ui.screens.schedule

import androidx.compose.ui.graphics.Color
import java.time.LocalTime

/**
 * 日程类型
 */
enum class ScheduleType {
    COURSE,  // 课程
    TASK,    // 任务
    SLEEP    // 睡眠
}

/**
 * 日程UI数据模型
 */
data class ScheduleUiModel(
    val id: Long,
    val title: String,
    val dayOfWeek: Int, // 1=周一, 7=周日
    val startTime: LocalTime,
    val endTime: LocalTime,
    val color: Color,
    val type: ScheduleType = ScheduleType.COURSE,
    val location: String = "" // 可选：上课地点
)

/**
 * 马卡龙色系调色板 (Pastel/Macaron Colors)
 * 低饱和度、高亮度的糖果色，更柔和优雅
 */
object ScheduleColors {
    val PastelBlue = Color(0xFFACD7FF)      // 淡蓝
    val PastelCoral = Color(0xFFFFB7B2)     // 淡珊瑚红
    val PastelMint = Color(0xFFB5EAD7)      // 淡薄荷绿
    val PastelLime = Color(0xFFE2F0CB)      // 淡青柠
    val PastelLavender = Color(0xFFD4C4FB)  // 淡薰衣草紫
    val PastelPeach = Color(0xFFFFDFBA)     // 淡蜜桃
    val PastelRose = Color(0xFFF8C8DC)      // 淡玫瑰粉
    val PastelSky = Color(0xFFC7ECEE)       // 淡天空蓝
    
    // 文字颜色 - 深灰色，不用纯白
    val TextOnPastel = Color(0xFF4A4A4A)
    
    val all = listOf(
        PastelBlue, PastelCoral, PastelMint, PastelLime,
        PastelLavender, PastelPeach, PastelRose, PastelSky
    )
    
    // 随机获取一个颜色
    fun random(): Color = all.random()
}
