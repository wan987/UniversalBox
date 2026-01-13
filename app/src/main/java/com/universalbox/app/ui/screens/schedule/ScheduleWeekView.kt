package com.universalbox.app.ui.screens.schedule

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * 周视图组件
 */
@Composable
fun ScheduleWeekView(
    schedules: List<ScheduleUiModel>,
    modifier: Modifier = Modifier,
    startHour: Int = 8,
    endHour: Int = 23,
    hourHeight: Dp = 50.dp,
    dayWidth: Dp = 46.dp,
    timeColumnWidth: Dp = 40.dp,
    onCellClick: (dayOfWeek: Int, hour: Int) -> Unit = { _, _ -> },
    onScheduleClick: (ScheduleUiModel) -> Unit = {}
) {
    val today = remember { LocalDate.now().dayOfWeek.value }
    val totalHours = endHour - startHour
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeight.toPx() }
    val dayWidthPx = with(density) { dayWidth.toPx() }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 顶部星期栏（固定）
        WeekHeader(
            timeColumnWidth = timeColumnWidth,
            dayWidth = dayWidth,
            today = today,
            horizontalScrollState = horizontalScrollState
        )
        
        // 可滚动的时间表内容
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // 左侧时间轴（仅垂直滚动）
            TimeColumn(
                startHour = startHour,
                endHour = endHour,
                hourHeight = hourHeight,
                width = timeColumnWidth,
                verticalScrollState = verticalScrollState
            )
            
            // 主网格区域（水平+垂直滚动）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState)
            ) {
                // 网格背景
                ScheduleGrid(
                    days = 7,
                    startHour = startHour,
                    endHour = endHour,
                    hourHeight = hourHeight,
                    dayWidth = dayWidth,
                    onCellClick = onCellClick
                )
                
                // 课程色块层
                ScheduleBlocks(
                    schedules = schedules,
                    startHour = startHour,
                    hourHeight = hourHeight,
                    dayWidth = dayWidth,
                    onScheduleClick = onScheduleClick
                )
            }
        }
    }
}

/**
 * 顶部星期栏
 */
@Composable
private fun WeekHeader(
    timeColumnWidth: Dp,
    dayWidth: Dp,
    today: Int,
    horizontalScrollState: androidx.compose.foundation.ScrollState
) {
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp)
    ) {
        // 左上角空白
        Spacer(modifier = Modifier.width(timeColumnWidth))
        
        // 星期
        Row(
            modifier = Modifier.horizontalScroll(horizontalScrollState)
        ) {
            weekDays.forEachIndexed { index, day ->
                val isToday = index + 1 == today
                Box(
                    modifier = Modifier
                        .width(dayWidth)
                        .background(
                            if (isToday) Color(0xFF42A5F5).copy(alpha = 0.15f)
                            else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) Color(0xFF1976D2) else Color(0xFF666666)
                        )
                        if (isToday) {
                            Text(
                                text = "今天",
                                fontSize = 8.sp,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 左侧时间轴
 */
@Composable
private fun TimeColumn(
    startHour: Int,
    endHour: Int,
    hourHeight: Dp,
    width: Dp,
    verticalScrollState: androidx.compose.foundation.ScrollState
) {
    Column(
        modifier = Modifier
            .width(width)
            .verticalScroll(verticalScrollState)
    ) {
        for (hour in startHour until endHour) {
            Box(
                modifier = Modifier
                    .height(hourHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = String.format("%02d:00", hour),
                    fontSize = 11.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

/**
 * 网格背景 + 当前时间红线
 */
@Composable
private fun ScheduleGrid(
    days: Int,
    startHour: Int,
    endHour: Int,
    hourHeight: Dp,
    dayWidth: Dp,
    onCellClick: (dayOfWeek: Int, hour: Int) -> Unit
) {
    val totalHours = endHour - startHour
    val gridWidth = dayWidth * days
    val gridHeight = hourHeight * totalHours
    
    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeight.toPx() }
    val dayWidthPx = with(density) { dayWidth.toPx() }
    
    // 获取当前时间，用于绘制红线
    val currentTime = remember { LocalTime.now() }
    val currentMinutes = currentTime.hour * 60 + currentTime.minute
    val startMinutes = startHour * 60
    val endMinutes = endHour * 60
    
    Box(
        modifier = Modifier
            .width(gridWidth)
            .height(gridHeight)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val dayIndex = (offset.x / dayWidthPx).toInt().coerceIn(0, days - 1)
                    val hourIndex = (offset.y / hourHeightPx).toInt().coerceIn(0, totalHours - 1)
                    val clickedHour = startHour + hourIndex
                    val dayOfWeek = dayIndex + 1
                    Log.d("Schedule", "点击了周$dayOfWeek ${clickedHour}:00")
                    onCellClick(dayOfWeek, clickedHour)
                }
            }
    ) {
        // 使用Canvas绘制网格线
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color(0xFFE0E0E0)
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            
            // 绘制横线（小时分隔线）
            for (i in 0..totalHours) {
                val y = i * hourHeightPx
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            
            // 绘制竖线（天分隔线）
            for (i in 0..days) {
                val x = i * dayWidthPx
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (i == 0 || i == days) 1f else 0.5f
                )
            }
            
            // 绘制半小时虚线
            for (i in 0 until totalHours) {
                val y = i * hourHeightPx + hourHeightPx / 2
                drawLine(
                    color = lineColor.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.5f,
                    pathEffect = dashedEffect
                )
            }
            
            // ====== 绘制当前时间红线 ======
            if (currentMinutes in startMinutes until endMinutes) {
                val currentY = ((currentMinutes - startMinutes) / 60f) * hourHeightPx
                val redLineColor = Color(0xFFFF3B30)
                
                // 画红色细实线横穿整个屏幕
                drawLine(
                    color = redLineColor,
                    start = Offset(0f, currentY),
                    end = Offset(size.width, currentY),
                    strokeWidth = 2f
                )
                
                // 在线的左端画一个小红点
                drawCircle(
                    color = redLineColor,
                    radius = 6f,
                    center = Offset(0f, currentY)
                )
            }
        }
    }
}

/**
 * 课程色块层
 */
@Composable
private fun ScheduleBlocks(
    schedules: List<ScheduleUiModel>,
    startHour: Int,
    hourHeight: Dp,
    dayWidth: Dp,
    onScheduleClick: (ScheduleUiModel) -> Unit
) {
    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeight.toPx() }
    val dayWidthPx = with(density) { dayWidth.toPx() }
    
    // 使用自定义 Layout 精确放置课程卡片
    Layout(
        content = {
            schedules.forEach { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    onClick = { onScheduleClick(schedule) }
                )
            }
        },
        modifier = Modifier
    ) { measurables, constraints ->
        val placeables = measurables.mapIndexed { index, measurable ->
            val schedule = schedules[index]
            
            // 计算色块高度（根据时长）
            val startMinutes = schedule.startTime.hour * 60 + schedule.startTime.minute
            val endMinutes = schedule.endTime.hour * 60 + schedule.endTime.minute
            val durationMinutes = endMinutes - startMinutes
            val blockHeight = (durationMinutes / 60f * hourHeightPx).toInt()
            
            // 计算色块宽度（留边距）
            val blockWidth = (dayWidthPx - 8).toInt()
            
            measurable.measure(
                constraints.copy(
                    minWidth = blockWidth,
                    maxWidth = blockWidth,
                    minHeight = blockHeight,
                    maxHeight = blockHeight
                )
            )
        }
        
        // 计算总布局大小
        val width = (dayWidthPx * 7).toInt()
        val height = (hourHeightPx * 15).toInt() // 15小时（8:00-23:00）
        
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val schedule = schedules[index]
                
                // 计算X偏移（根据星期几）
                val x = ((schedule.dayOfWeek - 1) * dayWidthPx + 4).toInt()
                
                // 计算Y偏移（根据开始时间）
                val startMinutes = schedule.startTime.hour * 60 + schedule.startTime.minute
                val startHourMinutes = startHour * 60
                val offsetMinutes = startMinutes - startHourMinutes
                val y = (offsetMinutes / 60f * hourHeightPx).toInt()
                
                placeable.place(x, y)
            }
        }
    }
}

/**
 * 单个课程卡片
 */
@Composable
private fun ScheduleCard(
    schedule: ScheduleUiModel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(schedule.color)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column {
            Text(
                text = schedule.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ScheduleColors.TextOnPastel,
                maxLines = 3,
                lineHeight = 12.sp,
                overflow = TextOverflow.Ellipsis
            )
            if (schedule.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = schedule.location,
                    fontSize = 8.sp,
                    color = ScheduleColors.TextOnPastel.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
