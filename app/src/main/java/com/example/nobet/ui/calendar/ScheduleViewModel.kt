package com.example.nobet.ui.calendar

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class ScheduleViewModel : ViewModel() {
    private val gson = Gson()

    val schedule = mutableStateMapOf<LocalDate, ShiftType>()

    fun set(date: LocalDate, type: ShiftType?) {
        if (type == null) schedule.remove(date) else schedule[date] = type
    }

    fun totalFor(month: java.time.YearMonth): Int =
        schedule.filterKeys { java.time.YearMonth.from(it) == month }.values.sumOf { it.hours }

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
}


