package com.universalbox.app.data.mapper

import com.universalbox.app.data.model.TimeSchedule
import com.universalbox.app.domain.model.ResourceCategory
import com.universalbox.app.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Mapper - TimeSchedule (Room Entity) 和 Schedule (Domain Model) 之间转换
 */

/**
 * TimeSchedule (Room Entity) -> Schedule (Domain Model)
 */
fun TimeSchedule.toDomain(): Schedule {
    return Schedule(
        id = this.id,
        dayOfWeek = DayOfWeek.of(this.dayOfWeek),
        startTime = LocalTime.parse(this.startTime),
        endTime = LocalTime.parse(this.endTime),
        recommendCategory = ResourceCategory.fromString(this.recommendCategory)
    )
}

/**
 * Schedule (Domain Model) -> TimeSchedule (Room Entity)
 */
fun Schedule.toData(): TimeSchedule {
    return TimeSchedule(
        id = this.id,
        dayOfWeek = this.dayOfWeek.value,
        startTime = this.startTime.toString(),
        endTime = this.endTime.toString(),
        recommendCategory = this.recommendCategory.displayName
    )
}
