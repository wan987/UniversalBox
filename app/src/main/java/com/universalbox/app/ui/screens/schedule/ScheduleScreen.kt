package com.universalbox.app.ui.screens.schedule

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.universalbox.app.ui.theme.ScheduleBrush

/**
 * 日程表主页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel: ScheduleViewModel = viewModel()
    val schedules = viewModel.schedules
    
    // 控制 BottomSheet 显示
    var showAddSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "我的课表",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    // 添加课程按钮放右上角
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "添加课程",
                            tint = Color(0xFF42A5F5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScheduleBrush)
                .padding(paddingValues)
        ) {
            ScheduleWeekView(
                schedules = schedules,
                modifier = Modifier.fillMaxSize(),
                onCellClick = { dayOfWeek, hour ->
                    val dayName = when(dayOfWeek) {
                        1 -> "周一"
                        2 -> "周二"
                        3 -> "周三"
                        4 -> "周四"
                        5 -> "周五"
                        6 -> "周六"
                        7 -> "周日"
                        else -> "未知"
                    }
                    Toast.makeText(
                        context,
                        "点击了 $dayName ${hour}:00",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onScheduleClick = { schedule ->
                    Toast.makeText(
                        context,
                        "${schedule.title}\n${schedule.location}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
    
    // 添加课程 BottomSheet
    if (showAddSheet) {
        AddScheduleBottomSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { newSchedule ->
                viewModel.addSchedule(newSchedule)
                showAddSheet = false
                Toast.makeText(
                    context,
                    "已添加: ${newSchedule.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}
