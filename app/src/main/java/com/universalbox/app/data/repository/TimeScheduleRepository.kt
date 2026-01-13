package com.universalbox.app.data.repository

import com.universalbox.app.data.local.TimeScheduleDao
import com.universalbox.app.data.mapper.toData
import com.universalbox.app.data.mapper.toDomain
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.model.Schedule
import com.universalbox.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * ScheduleRepositoryImpl - ScheduleRepository 接口的实现
 */
class ScheduleRepositoryImpl(
    private val timeScheduleDao: TimeScheduleDao
) : ScheduleRepository {

    override fun getAllSchedules(): Flow<List<Schedule>> {
        return timeScheduleDao.getAllSchedules().map { schedules ->
            schedules.map { it.toDomain() }
        }
    }

    override fun getSchedulesByDay(dayOfWeek: DayOfWeek): Flow<List<Schedule>> {
        return timeScheduleDao.getSchedulesByDay(dayOfWeek.value).map { schedules ->
            schedules.map { it.toDomain() }
        }
    }

    override suspend fun getCurrentRecommendedCategory(): ResourceCategory? {
        val now = LocalTime.now()
        val today = DayOfWeek.from(java.time.LocalDate.now())
        
        return getRecommendedCategory(today, now)
    }

    override suspend fun getRecommendedCategory(
        dayOfWeek: DayOfWeek,
        time: LocalTime
    ): ResourceCategory? {
        val schedules = timeScheduleDao.getSchedulesForDay(dayOfWeek.value)
        
        val matchingSchedule = schedules
            .map { it.toDomain() }
            .firstOrNull { it.isTimeInRange(time) }
        
        return matchingSchedule?.recommendCategory
    }

    override suspend fun insertSchedule(schedule: Schedule): Long {
        return timeScheduleDao.insertSchedule(schedule.toData())
    }

    override suspend fun insertSchedules(schedules: List<Schedule>) {
        timeScheduleDao.insertSchedules(schedules.map { it.toData() })
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        timeScheduleDao.updateSchedule(schedule.toData())
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        timeScheduleDao.deleteSchedule(schedule.toData())
    }

    override suspend fun initializeDefaultSchedules() {
        val existingSchedules = timeScheduleDao.getSchedulesForDay(1)
        
        // 只在数据库为空时初始化
        if (existingSchedules.isEmpty()) {
            val defaultSchedules = Schedule.createDefaultSchedules()
            insertSchedules(defaultSchedules)
        }
    }
}
