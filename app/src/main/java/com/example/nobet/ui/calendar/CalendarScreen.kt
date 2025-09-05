package com.example.nobet.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

// --- CalendarScreen ---
@Composable
fun CalendarScreen(padding: PaddingValues, vm: ScheduleViewModel = viewModel()) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var dialogFor by remember { mutableStateOf<LocalDate?>(null) }
    var selectedShiftColor by remember { mutableStateOf<Color?>(null) }

    val locale = java.util.Locale.forLanguageTag("tr-TR")
    val overtimeResult = vm.calculateOvertime(currentMonth)

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Başlık
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                "Nöbet",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                "Takvimim",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Ay navigasyonu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Önceki ay")
            }

            val monthText = currentMonth.month.getDisplayName(TextStyle.FULL, locale)
            Text(
                "${monthText.replaceFirstChar { it.titlecase(locale) }} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Sonraki ay")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Takvim
        key(currentMonth) {
            SelectableCalendar(
                calendarState = rememberSelectableCalendarState(initialMonth = currentMonth),
                dayContent = { dayState ->
                    DayCell(
                        date = dayState.date,
                        schedule = vm.schedule,
                        isSelected = selectedDate == dayState.date,
                        selectedShiftColor = selectedShiftColor,
                        onClick = {
                            selectedDate = it
                            dialogFor = it
                            selectedShiftColor = vm.schedule[it]?.color
                        }
                    )
                },
                monthHeader = { monthState -> currentMonth = monthState.currentMonth },
                firstDayOfWeek = DayOfWeek.MONDAY
            )
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        // Legend
        Legend()
        Spacer(Modifier.height(16.dp))

        // İstatistikler
        val monthText = currentMonth.month.getDisplayName(TextStyle.FULL, locale)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toplam çalışma saati
            Text(
                text = "${monthText.replaceFirstChar { it.titlecase(locale) }} ${currentMonth.year} Toplam Çalışma Saatin: ${overtimeResult.workedHours}",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Fazla mesai bilgisi
            if (overtimeResult.overtimeHours != 0) {
                val overtimeText = if (overtimeResult.overtimeHours > 0) {
                    "✅ Fazla mesai: +${overtimeResult.overtimeHours} saat"
                } else {
                    "⚠️ Eksik mesai: ${overtimeResult.overtimeHours} saat"
                }

                Text(
                    text = overtimeText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        color = if (overtimeResult.overtimeHours > 0) Color.Green else Color.Red
                    ),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            // Detaylı bilgi
            Text(
                text = "Çalışılan gün: ${overtimeResult.workingDays} gün × 8 saat = ${overtimeResult.expectedHours} saat",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = Color.Gray
            )
        }
    }

    if (dialogFor != null) {
        ShiftDialog(
            onDismiss = { dialogFor = null; selectedShiftColor = null },
            onSelect = { type -> dialogFor?.let { vm.set(it, type); selectedShiftColor = type.color }; dialogFor = null },
            onClear = { dialogFor?.let { vm.set(it, null); selectedShiftColor = null }; dialogFor = null }
        )
    }
}

// --- Gün Hücresi ---
@Composable
private fun DayCell(
    date: LocalDate,
    schedule: Map<LocalDate, ShiftType>,
    isSelected: Boolean,
    selectedShiftColor: Color?,
    onClick: (LocalDate) -> Unit
) {
    val shift = schedule[date]
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    val isToday = date == LocalDate.now()

    val bgColor: Color = when {
        shift != null -> shift.color
        isSelected && selectedShiftColor != null -> selectedShiftColor.copy(alpha = 0.7f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        isWeekend -> Color.LightGray.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    val textColor: Color = when {
        shift != null -> if (shift.color.luminance() < 0.5f) Color.White else Color.Black
        isSelected && selectedShiftColor != null -> if (selectedShiftColor.luminance() < 0.5f) Color.White else Color.Black
        isSelected -> if (MaterialTheme.colorScheme.primary.luminance() < 0.5f) Color.White else Color.Black
        isWeekend -> Color.Red
        else -> Color.Black
    }

    Surface(onClick = { onClick(date) }, color = Color.Transparent) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bgColor)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontWeight = if (isWeekend) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                fontSize = 16.sp
            )
        }
    }
}

// --- Legend ---
@Composable
private fun Legend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ShiftType.MORNING.LegendBoxLarge(showSeparator = true)
            ShiftType.NIGHT.LegendBoxLarge(showSeparator = true)
            ShiftType.FULL.LegendBoxLarge(showSeparator = false)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ShiftType.DAY16.LegendBoxLarge(showSeparator = true)
            ShiftType.EVENING.LegendBoxLarge(showSeparator = false)
        }
    }
}

@Composable
private fun ShiftType.LegendBoxLarge(showSeparator: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(this@LegendBoxLarge.color)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = this@LegendBoxLarge.label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        if (showSeparator) {
            Spacer(Modifier.width(12.dp))
            Text(
                text = "|",
                fontSize = 20.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Spacer(Modifier.width(12.dp))
        }
    }
}

// --- ShiftDialog ---
@Composable
private fun ShiftDialog(onDismiss: () -> Unit, onSelect: (ShiftType) -> Unit, onClear: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Nöbet Seçiniz") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShiftType.entries.forEach { type ->
                    Button(
                        onClick = { onSelect(type) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = type.color, contentColor = Color.White)
                    ) {
                        Text("${type.label} (${type.hours} Saat)")
                    }
                }
                OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                    Text("Seçimi Temizle")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("İptal")
                }
            }
        }
    )
}