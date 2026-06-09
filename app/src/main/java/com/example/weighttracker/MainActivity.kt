package com.example.weighttracker

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.weighttracker.ui.WeightTrackerApp
import com.example.weighttracker.ui.WeightTrackerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WeightTrackerViewModel by viewModels()
    private var pendingCsv: String? = null

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri: Uri? ->
        val csv = pendingCsv
        pendingCsv = null
        if (uri != null && csv != null) {
            contentResolver.openOutputStream(uri)?.use { output ->
                output.write(csv.toByteArray(Charsets.UTF_8))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeightTrackerApp(
                viewModel = viewModel,
                onExportCsv = {
                    pendingCsv = viewModel.csv()
                    exportLauncher.launch("weight-tracker.csv")
                },
            )
        }
    }
}
