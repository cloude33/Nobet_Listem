package com.example.nobet.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.nobet.ui.calendar.CalendarScreen
import com.example.nobet.ui.data.VerilerimScreen
import com.example.nobet.ui.stats.StatisticsScreen
import com.example.nobet.ui.about.AboutScreen

@Composable
fun AppRoot() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = { selectedIndex = 0 },
                    icon = { Icon(Icons.Filled.Today, contentDescription = null) },
                    label = { Text("Takvim") }
                )
                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = { selectedIndex = 1 },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                    label = { Text("İstatistik") }
                )
                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = { selectedIndex = 2 },
                    icon = { Icon(Icons.Filled.Storage, contentDescription = null) },
                    label = { Text("Verilerim") }
                )
                NavigationBarItem(
                    selected = selectedIndex == 3,
                    onClick = { selectedIndex = 3 },
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    label = { Text("Hakkında") }
                )
            }
        }
    ) { padding ->
        when (selectedIndex) {
            0 -> CalendarScreen(padding)
            1 -> StatisticsScreen(padding)
            2 -> VerilerimScreen(padding)
            else -> AboutScreen(padding)
        }
    }
}


