package com.example.nobet.ui.data

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nobet.ui.calendar.ScheduleViewModel

@Composable
fun VerilerimScreen(padding: PaddingValues, vm: ScheduleViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { writeToUri(context, it, vm.toJson()) } }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { readFromUri(context, it)?.let(vm::fromJson) } }
    )

    Column(
        modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Verilerim", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = {
            exportLauncher.launch("nobet-cizelgem.json")
        }, modifier = Modifier.fillMaxWidth()) { Text("Google Drive'a Yedekle (JSON Dışa Aktar)") }

        Button(onClick = {
            importLauncher.launch(arrayOf("application/json"))
        }, modifier = Modifier.fillMaxWidth()) { Text("Yedekten Geri Yükle (JSON İçe Aktar)") }

        Text("Not: iOS'ta aynı JSON formatını iCloud Drive'dan içe/dışa aktarabilirsiniz.")
    }
}

private fun writeToUri(context: Context, uri: Uri, content: String) {
    context.contentResolver.openOutputStream(uri)?.use { stream ->
        stream.writer().use { it.write(content) }
    }
}

private fun readFromUri(context: Context, uri: Uri): String? {
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        stream.reader().readText()
    }
}


