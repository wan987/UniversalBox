package com.universalbox.app.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Schedule - Domain 层的时间表模型
 * 
 * 使用 Java 8+ 的 java.time API 提供更好的类型安全
 */
data class Schedule(
    val id: Long = 0,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val recommendCategory: ResourceCategory
) {
    /**
     * 检查指定时间是否在这个时间段内
     */
    fun isTimeInRange(time: LocalTime): Boolean {
        return !time.isBefore(startTime) && time.isBefore(endTime)
    }
    
    /**
     * 获取时间段的显示文本
     */
    fun getTimeRangeText(): String {
        return "${startTime} - ${endTime}"
    }
    
    /**
     * 获取星期几的中文名称
     */
    fun getDayNameChinese(): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }
    
    companion object {
        /**
         * 创建默认的一周作息模板
         * 工作日：上午学习，下午工作，晚上娱乐
         * 周末：全天娱乐
         */
        fun createDefaultSchedules(): List<Schedule> {
            val schedules = mutableListOf<Schedule>()
            
            // 工作日 (周一到周五)
            for (day in DayOfWeek.values().filter { it.value in 1..5 }) {
                schedules.add(
                    Schedule(
                        dayOfWeek = day,
                        startTime = LocalTime.of(9, 0),
                        endTime = LocalTime.of(12, 0),
                        recommendCategory = ResourceCategory.STUDY
                    )
                )
                schedules.add(
                    Schedule(
                        dayOfWeek = day,
                        startTime = LocalTime.of(14, 0),
                        endTime = LocalTime.of(18, 0),
                        recommendCategory = ResourceCategory.WORK
                    )
                )
                schedules.add(
                    Schedule(
                        dayOfWeek = day,
                        startTime = LocalTime.of(19, 0),
                        endTime = LocalTime.of(22, 0),
                        recommendCategory = ResourceCategory.ENTERTAINMENT
                    )
                )
            }
            
            // 周末 (周六、周日)
            for (day in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                schedules.add(
                    Schedule(
                        dayOfWeek = day,
                        startTime = LocalTime.of(9, 0),
                        endTime = LocalTime.of(22, 0),
                        recommendCategory = ResourceCategory.ENTERTAINMENT
                    )
                )
            }
            
            return schedules
        }
    }
}
