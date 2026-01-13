package com.universalbox.app.domain.repository

import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * ScheduleRepository - Domain 层的时间表仓库接口
 */
interface ScheduleRepository {
    /**
     * 获取所有时间表的 Flow
     */
    fun getAllSchedules(): Flow<List<Schedule>>
    
    /**
     * 获取指定星期几的时间表
     */
    fun getSchedulesByDay(dayOfWeek: DayOfWeek): Flow<List<Schedule>>
    
    /**
     * 获取当前时间推荐的分类
     * @return 推荐的分类，如果没有匹配的时间段则返回 null
     */
    suspend fun getCurrentRecommendedCategory(): ResourceCategory?
    
    /**
     * 获取指定时间的推荐分类
     */
    suspend fun getRecommendedCategory(
        dayOfWeek: DayOfWeek,
        time: LocalTime
    ): ResourceCategory?
    
    /**
     * 插入时间表
     */
    suspend fun insertSchedule(schedule: Schedule): Long
    
    /**
     * 批量插入时间表
     */
    suspend fun insertSchedules(schedules: List<Schedule>)
    
    /**
     * 更新时间表
     */
    suspend fun updateSchedule(schedule: Schedule)
    
    /**
     * 删除时间表
     */
    suspend fun deleteSchedule(schedule: Schedule)
    
    /**
     * 初始化默认时间表（如果数据库为空）
     */
    suspend fun initializeDefaultSchedules()
}
