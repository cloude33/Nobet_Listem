package com.example.nobet.ui.calendar

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

enum class ShiftType(val label: String, val hours: Int, val color: androidx.compose.ui.graphics.Color) {
    MORNING("8-16", 8, androidx.compose.ui.graphics.Color(0xFF64B5F6)),
    NIGHT("16-8", 16, androidx.compose.ui.graphics.Color(0xFF9575CD)),
    FULL("8-8", 24, androidx.compose.ui.graphics.Color(0xFFF44336)),
    DAY16("8-24", 16, androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    EVENING("16-24", 8, androidx.compose.ui.graphics.Color(0xFFFF9800))
}

data class OvertimeResult(
    val workedHours: Int,
    val overtimeHours: Int,
    val workingDays: Int,
    val expectedHours: Int
)

class ScheduleViewModel : ViewModel() {
    private val gson = Gson()
    val schedule = mutableStateMapOf<LocalDate, ShiftType>()

    fun set(date: LocalDate, type: ShiftType?) {
        if (type == null) schedule.remove(date) else schedule[date] = type
    }

    fun toJson(): String {
        val map = schedule.mapKeys { it.key.toString() }
        return gson.toJson(map)
    }

    fun fromJson(json: String) {
        val type = object : TypeToken<Map<String, String>>() {}.type
        val parsed: Map<String, String> = gson.fromJson(json, type)
        val mapped: Map<LocalDate, ShiftType> = parsed
            .mapKeys { LocalDate.parse(it.key) }
            .mapValues { ShiftType.valueOf(it.value) }
        schedule.clear()
        schedule.putAll(mapped)
    }

    fun totalFor(month: YearMonth, holidays: List<LocalDate>, bayrams: List<LocalDate>): Int {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        return generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .sumOf { date ->
                val shiftHours = schedule[date]?.hours ?: 0
                val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                val isHoliday = holidays.contains(date)
                val isBayramArife = bayrams.any { it.minusDays(1) == date && date.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }

                when {
                    isBayramArife -> 5
                    shiftHours > 0 -> shiftHours // Prioritize assigned shifts
                    isWeekend || isHoliday -> 0
                    else -> 0
                } as Int
            }
    }

    fun overtimeFor(month: YearMonth, holidays: List<LocalDate>, bayrams: List<LocalDate>): Int {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()

        val normalHours = generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .sumOf { date ->
                val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                val isHoliday = holidays.contains(date)
                val isBayramArife = bayrams.any { it.minusDays(1) == date && date.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }

                when {
                    isBayramArife -> 5
                    isWeekend || isHoliday -> 0
                    else -> 8
                } as Int
            }

        val realHours = totalFor(month, holidays, bayrams)
        return (realHours - normalHours).coerceAtLeast(0)
    }

    fun calculateOvertime(month: YearMonth, holidays: List<LocalDate> = emptyList(), bayrams: List<LocalDate> = emptyList()): OvertimeResult {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()

        val workingDays = generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .count { date ->
                val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                val isHoliday = holidays.contains(date)
                !isWeekend && !isHoliday
            }

        val workedHours = totalFor(month, holidays, bayrams)
        val overtimeHours = overtimeFor(month, holidays, bayrams)
        val expectedHours = workingDays * 8

        return OvertimeResult(
            workedHours = workedHours,
            overtimeHours = overtimeHours,
            workingDays = workingDays,
            expectedHours = expectedHours
        )
    }
}