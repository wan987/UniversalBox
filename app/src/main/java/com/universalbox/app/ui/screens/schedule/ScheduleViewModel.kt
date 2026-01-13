package com.universalbox.app.ui.screens.schedule

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.time.LocalTime

/**
 * 日程表 ViewModel
 */
class ScheduleViewModel : ViewModel() {
    
    // 日程列表
    private val _schedules = mutableStateListOf<ScheduleUiModel>()
    val schedules: List<ScheduleUiModel> get() = _schedules
    
    init {
        // 加载Mock数据
        loadMockData()
    }
    
    /**
     * Mock数据 - 模拟课程表（马卡龙配色）
     */
    private fun loadMockData() {
        _schedules.addAll(listOf(
            // 周一
            ScheduleUiModel(
                id = 1,
                title = "高等数学",
                dayOfWeek = 1,
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(9, 40),
                color = ScheduleColors.PastelBlue,
                type = ScheduleType.COURSE,
                location = "教学楼A301"
            ),
            ScheduleUiModel(
                id = 2,
                title = "英语听力",
                dayOfWeek = 1,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(11, 40),
                color = ScheduleColors.PastelMint,
                type = ScheduleType.COURSE,
                location = "语音室B102"
            ),
            ScheduleUiModel(
                id = 3,
                title = "数据结构",
                dayOfWeek = 1,
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(15, 40),
                color = ScheduleColors.PastelLavender,
                type = ScheduleType.COURSE,
                location = "计算机楼C205"
            ),
            
            // 周二
            ScheduleUiModel(
                id = 4,
                title = "线性代数",
                dayOfWeek = 2,
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(9, 40),
                color = ScheduleColors.PastelPeach,
                type = ScheduleType.COURSE,
                location = "教学楼A302"
            ),
            ScheduleUiModel(
                id = 5,
                title = "体育课",
                dayOfWeek = 2,
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(15, 40),
                color = ScheduleColors.PastelSky,
                type = ScheduleType.COURSE,
                location = "体育馆"
            ),
            
            // 周三
            ScheduleUiModel(
                id = 6,
                title = "高等数学",
                dayOfWeek = 3,
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(9, 40),
                color = ScheduleColors.PastelBlue,
                type = ScheduleType.COURSE,
                location = "教学楼A301"
            ),
            ScheduleUiModel(
                id = 7,
                title = "计算机网络",
                dayOfWeek = 3,
                startTime = LocalTime.of(19, 0),
                endTime = LocalTime.of(20, 40),
                color = ScheduleColors.PastelRose,
                type = ScheduleType.COURSE,
                location = "晚间选修"
            ),
            
            // 周四
            ScheduleUiModel(
                id = 8,
                title = "操作系统",
                dayOfWeek = 4,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(11, 40),
                color = ScheduleColors.PastelCoral,
                type = ScheduleType.COURSE,
                location = "计算机楼C301"
            ),
            ScheduleUiModel(
                id = 9,
                title = "算法设计",
                dayOfWeek = 4,
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(15, 40),
                color = ScheduleColors.PastelLime,
                type = ScheduleType.COURSE,
                location = "计算机楼C205"
            ),
            
            // 周五
            ScheduleUiModel(
                id = 10,
                title = "英语写作",
                dayOfWeek = 5,
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(9, 40),
                color = ScheduleColors.PastelMint,
                type = ScheduleType.COURSE,
                location = "外语楼D102"
            ),
            ScheduleUiModel(
                id = 11,
                title = "数据库原理",
                dayOfWeek = 5,
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(17, 30),
                color = ScheduleColors.PastelLavender,
                type = ScheduleType.COURSE,
                location = "实验室E201"
            ),
            
            // 周六 - 自习任务
            ScheduleUiModel(
                id = 12,
                title = "图书馆自习",
                dayOfWeek = 6,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(12, 0),
                color = ScheduleColors.PastelSky,
                type = ScheduleType.TASK,
                location = "图书馆三楼"
            )
        ))
    }
    
    /**
     * 添加日程
     */
    fun addSchedule(schedule: ScheduleUiModel) {
        _schedules.add(schedule)
    }
    
    /**
     * 删除日程
     */
    fun deleteSchedule(id: Long) {
        _schedules.removeIf { it.id == id }
    }
    
    /**
     * 获取指定星期的日程
     */
    fun getSchedulesForDay(dayOfWeek: Int): List<ScheduleUiModel> {
        return _schedules.filter { it.dayOfWeek == dayOfWeek }
    }
}
