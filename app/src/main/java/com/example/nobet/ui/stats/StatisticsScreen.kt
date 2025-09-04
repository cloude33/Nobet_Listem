package com.example.nobet.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nobet.ui.calendar.ScheduleViewModel
import com.example.nobet.ui.calendar.ShiftType
import java.time.YearMonth
import java.time.format.TextStyle

@Composable
fun StatisticsScreen(padding: PaddingValues, vm: ScheduleViewModel = viewModel()) {
    var current by remember { mutableStateOf(YearMonth.now()) }
    val locale = java.util.Locale.forLanguageTag("tr-TR")

    val monthText = current.month.getDisplayName(TextStyle.FULL, locale)
    val header = "${monthText.replaceFirstChar { it.titlecase(locale) }} ${current.year} İstatistik"

    val counts: Map<ShiftType, Int> = ShiftType.entries.associateWith { type ->
        vm.schedule.filter { (date, t) -> java.time.YearMonth.from(date) == current && t == type }.size
    }

    Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(header, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { current = current.minusMonths(1) }) { Text("Önceki Ay") }
            Button(onClick = { current = current.plusMonths(1) }) { Text("Sonraki Ay") }
        }
    }
}


