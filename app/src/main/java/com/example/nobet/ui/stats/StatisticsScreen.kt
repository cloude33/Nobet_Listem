package com.example.nobet.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nobet.ui.calendar.ScheduleViewModel
import com.example.nobet.ui.calendar.ShiftType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import kotlin.math.max

@Composable
fun StatisticsScreen(padding: PaddingValues, vm: ScheduleViewModel = viewModel()) {
    var current by remember { mutableStateOf(YearMonth.now()) }
    val locale = java.util.Locale.forLanguageTag("tr-TR")

    // Example holiday and bayram lists (replace with actual data)
    val holidays = remember {
        listOf(
            LocalDate.of(current.year, 1, 1), // New Year's Day
            LocalDate.of(current.year, 4, 23) // Example holiday
        )
    }
    val bayrams = remember {
        listOf(
            LocalDate.of(current.year, 6, 28), // Example bayram
            LocalDate.of(current.year, 6, 29)
        )
    }

    val monthText = current.month.getDisplayName(TextStyle.FULL, locale)
    val header = "${monthText.replaceFirstChar { it.titlecase(locale) }} ${current.year} İstatistik"

    // Her tip nöbetin sayısı
    val counts: Map<ShiftType, Int> = ShiftType.entries.associateWith { type ->
        vm.schedule.filter { (date, t) -> YearMonth.from(date) == current && t == type }.size
    }

    val overtimeResult = vm.calculateOvertime(current, holidays, bayrams)
    val missingHours = max(0, overtimeResult.expectedHours - overtimeResult.workedHours)

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            header,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Nöbet Tipleri ve Sayısı
        counts.forEach { (type, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(type.color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(type.label, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text("$count", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Toplam Çalışma Saati
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Toplam Çalışma Saati", style = MaterialTheme.typography.titleMedium)
            Text("${overtimeResult.workedHours} Saat", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        // Fazla Mesai
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Fazla Mesai", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                "${overtimeResult.overtimeHours} Saat",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (overtimeResult.overtimeHours >= 0) MaterialTheme.colorScheme.primary else Color.Red
                )
            )
        }

        // Eksik Mesai
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Eksik Mesai", style = MaterialTheme.typography.titleMedium)
            Text(
                "$missingHours Saat",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (missingHours > 0) Color.Red else Color.Black
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // Ay değiştirici butonlar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { current = current.minusMonths(1) }) { Text("Önceki Ay") }
            Button(onClick = { current = current.plusMonths(1) }) { Text("Sonraki Ay") }
        }
    }
}