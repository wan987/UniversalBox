package com.universalbox.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 时间表 - 用于存储用户的作息模板
 * 可以根据时间段推荐不同类别的资源
 */
@Entity(tableName = "time_schedules")
data class TimeSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,          // 星期几 (1-7, 1=周一, 7=周日)
    val startTime: String,       // 开始时间 (格式 "HH:mm", 例如 "09:00")
    val endTime: String,         // 结束时间 (格式 "HH:mm", 例如 "12:00")
    val recommendCategory: String // 推荐的资源分类 (对应 ResourceCategory)
) {
    /**
     * 检查指定时间是否在这个时间段内
     * @param hour 小时 (0-23)
     * @param minute 分钟 (0-59)
     * @return true 如果在时间段内
     */
    fun isTimeInRange(hour: Int, minute: Int): Boolean {
        val currentMinutes = hour * 60 + minute
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        
        val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
        val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
        
        return currentMinutes in startMinutes until endMinutes
    }
    
    /**
     * 获取星期几的中文名称
     */
    fun getDayName(): String {
        return when (dayOfWeek) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> "未知"
        }
    }
    
    companion object {
        /**
         * 创建默认的时间表模板
         * 工作日：9:00-12:00 学习, 14:00-18:00 工作, 19:00-22:00 娱乐
         */
        fun createDefaultSchedules(): List<TimeSchedule> {
            val schedules = mutableListOf<TimeSchedule>()
            
            // 周一到周五
            for (day in 1..5) {
                // 早上学习时间
                schedules.add(
                    TimeSchedule(
                        dayOfWeek = day,
                        startTime = "09:00",
                        endTime = "12:00",
                        recommendCategory = ResourceCategory.STUDY
                    )
                )
                // 下午工作时间
                schedules.add(
                    TimeSchedule(
                        dayOfWeek = day,
                        startTime = "14:00",
                        endTime = "18:00",
                        recommendCategory = ResourceCategory.WORK
                    )
                )
                // 晚上娱乐时间
                schedules.add(
                    TimeSchedule(
                        dayOfWeek = day,
                        startTime = "19:00",
                        endTime = "22:00",
                        recommendCategory = ResourceCategory.ENTERTAINMENT
                    )
                )
            }
            
            // 周末全天娱乐
            for (day in 6..7) {
                schedules.add(
                    TimeSchedule(
                        dayOfWeek = day,
                        startTime = "09:00",
                        endTime = "22:00",
                        recommendCategory = ResourceCategory.ENTERTAINMENT
                    )
                )
            }
            
            return schedules
        }
    }
}
