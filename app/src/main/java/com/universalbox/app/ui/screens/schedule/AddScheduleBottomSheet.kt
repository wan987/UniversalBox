package com.universalbox.app.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime

/**
 * 添加课程的 BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (ScheduleUiModel) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedDay by remember { mutableIntStateOf(1) }
    var startHour by remember { mutableIntStateOf(8) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(9) }
    var endMinute by remember { mutableIntStateOf(40) }
    var selectedColor by remember { mutableStateOf(ScheduleColors.PastelBlue) }
    var selectedType by remember { mutableStateOf(ScheduleType.COURSE) }
    
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 标题
            Text(
                text = "添加课程",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            // 课程名称
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("课程名称") },
                placeholder = { Text("例如：高等数学") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // 上课地点
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("上课地点（可选）") },
                placeholder = { Text("例如：教学楼A301") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // 星期选择
            Text(
                text = "选择星期",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weekDays.size) { index ->
                    val day = index + 1
                    val isSelected = selectedDay == day
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDay = day },
                        label = { Text(weekDays[index]) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF42A5F5),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            // 时间选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 开始时间
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "开始时间",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TimePickerRow(
                        hour = startHour,
                        minute = startMinute,
                        onHourChange = { startHour = it },
                        onMinuteChange = { startMinute = it }
                    )
                }
                
                // 结束时间
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "结束时间",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TimePickerRow(
                        hour = endHour,
                        minute = endMinute,
                        onHourChange = { endHour = it },
                        onMinuteChange = { endMinute = it }
                    )
                }
            }
            
            // 颜色选择
            Text(
                text = "选择颜色",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ScheduleColors.all) { color ->
                    ColorCircle(
                        color = color,
                        isSelected = selectedColor == color,
                        onClick = { selectedColor = color }
                    )
                }
            }
            
            // 类型选择
            Text(
                text = "类型",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScheduleType.entries.forEach { type ->
                    val label = when(type) {
                        ScheduleType.COURSE -> "课程"
                        ScheduleType.TASK -> "任务"
                        ScheduleType.SLEEP -> "休息"
                    }
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF42A5F5),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 确认按钮
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newSchedule = ScheduleUiModel(
                            id = System.currentTimeMillis(),
                            title = title,
                            dayOfWeek = selectedDay,
                            startTime = LocalTime.of(startHour, startMinute),
                            endTime = LocalTime.of(endHour, endMinute),
                            color = selectedColor,
                            type = selectedType,
                            location = location
                        )
                        onConfirm(newSchedule)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF42A5F5)
                ),
                enabled = title.isNotBlank()
            ) {
                Text(
                    text = "添加课程",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 时间选择行
 */
@Composable
private fun TimePickerRow(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 小时选择
        NumberPicker(
            value = hour,
            range = 0..23,
            onValueChange = onHourChange
        )
        
        Text(
            text = ":",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666)
        )
        
        // 分钟选择
        NumberPicker(
            value = minute,
            range = listOf(0, 10, 20, 30, 40, 50),
            onValueChange = onMinuteChange
        )
    }
}

/**
 * 简易数字选择器
 */
@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(4.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            range.forEach { num ->
                DropdownMenuItem(
                    text = { Text(String.format("%02d", num)) },
                    onClick = {
                        onValueChange(num)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun NumberPicker(
    value: Int,
    range: List<Int>,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(4.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            range.forEach { num ->
                DropdownMenuItem(
                    text = { Text(String.format("%02d", num)) },
                    onClick = {
                        onValueChange(num)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 颜色选择圆圈
 */
@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color(0xFF333333) else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选择",
                tint = ScheduleColors.TextOnPastel,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
