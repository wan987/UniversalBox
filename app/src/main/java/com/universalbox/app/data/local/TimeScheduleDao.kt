package com.universalbox.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.universalbox.app.data.model.TimeSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeScheduleDao {
    // 插入时间表
    @Insert
    suspend fun insertSchedule(schedule: TimeSchedule): Long
    
    // 批量插入时间表
    @Insert
    suspend fun insertSchedules(schedules: List<TimeSchedule>): List<Long>
    
    // 更新时间表
    @Update
    suspend fun updateSchedule(schedule: TimeSchedule)
    
    // 删除时间表
    @Delete
    suspend fun deleteSchedule(schedule: TimeSchedule)
    
    // 获取所有时间表
    @Query("SELECT * FROM time_schedules ORDER BY dayOfWeek, startTime")
    fun getAllSchedules(): Flow<List<TimeSchedule>>
    
    // 获取指定星期的时间表
    @Query("SELECT * FROM time_schedules WHERE dayOfWeek = :dayOfWeek ORDER BY startTime")
    fun getSchedulesByDay(dayOfWeek: Int): Flow<List<TimeSchedule>>
    
    // 根据ID获取时间表
    @Query("SELECT * FROM time_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): TimeSchedule?
    
    // 清空所有时间表
    @Query("DELETE FROM time_schedules")
    suspend fun clearAllSchedules()
    
    // 获取当前应该推荐的分类 (根据星期和时间)
    @Query("""
        SELECT * FROM time_schedules 
        WHERE dayOfWeek = :dayOfWeek 
        ORDER BY startTime
    """)
    suspend fun getSchedulesForDay(dayOfWeek: Int): List<TimeSchedule>
}
