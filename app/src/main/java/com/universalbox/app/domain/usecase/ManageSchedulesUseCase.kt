package com.universalbox.app.domain.usecase

import com.universalbox.app.domain.model.Schedule
import com.universalbox.app.domain.repository.ScheduleRepository

/**
 * ManageSchedules - 管理时间表
 * 
 * 提供时间表的 CRUD 操作
 */
class ManageSchedulesUseCase(
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * 初始化默认时间表
     */
    suspend fun initializeDefaults() {
        scheduleRepository.initializeDefaultSchedules()
    }
    
    /**
     * 保存时间表
     */
    suspend fun saveSchedule(schedule: Schedule): Long {
        return if (schedule.id == 0L) {
            scheduleRepository.insertSchedule(schedule)
        } else {
            scheduleRepository.updateSchedule(schedule)
            schedule.id
        }
    }
    
    /**
     * 删除时间表
     */
    suspend fun deleteSchedule(schedule: Schedule) {
        scheduleRepository.deleteSchedule(schedule)
    }
    
    /**
     * 批量保存时间表（用于重置一周的模板）
     */
    suspend fun saveSchedules(schedules: List<Schedule>) {
        scheduleRepository.insertSchedules(schedules)
    }
}
