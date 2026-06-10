package com.aditya.weighttracker

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.aditya.weighttracker.ui.WeightTrackerApp
import com.aditya.weighttracker.ui.WeightTrackerViewModel

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

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.use { input ->
                viewModel.previewCsvImport(input.bufferedReader().readText())
            }
        }
    }

    private val driveAuthorizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        viewModel.finishGoogleDriveAuthorization(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val launchDriveAuthorization = { pendingIntent: android.app.PendingIntent ->
                driveAuthorizationLauncher.launch(IntentSenderRequest.Builder(pendingIntent).build())
            }
            WeightTrackerApp(
                viewModel = viewModel,
                onExportCsv = {
                    pendingCsv = viewModel.csv()
                    exportLauncher.launch("weight-tracker.csv")
                },
                onImportCsv = {
                    importLauncher.launch(arrayOf("text/*", "text/csv", "application/csv", "application/vnd.ms-excel"))
                },
                onConnectDrive = {
                    viewModel.connectGoogleDrive(launchDriveAuthorization)
                },
                onBackupNow = {
                    viewModel.backupNow(launchDriveAuthorization)
                },
                onRestoreDriveBackup = {
                    viewModel.restoreDriveBackup(launchDriveAuthorization)
                },
            )
        }
    }
}
