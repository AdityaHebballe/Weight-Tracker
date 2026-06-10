package com.aditya.weighttracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingFlat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aditya.weighttracker.backup.BackupFrequency
import com.aditya.weighttracker.backup.BackupStatus
import com.aditya.weighttracker.data.WeightEntry
import com.aditya.weighttracker.domain.BmiCategory
import com.aditya.weighttracker.domain.BmiCalculator
import com.aditya.weighttracker.domain.DuplicateImportPolicy
import com.aditya.weighttracker.domain.LogGrouping
import com.aditya.weighttracker.domain.UnitConverter
import com.aditya.weighttracker.domain.UnitSystem
import com.aditya.weighttracker.domain.WaistToHeightCategory
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TrackerRoute = "tracker"
private const val SettingsRoute = "settings"
private const val OnboardingRoute = "onboarding"

@Composable
fun WeightTrackerApp(
    viewModel: WeightTrackerViewModel,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
    onConnectDrive: () -> Unit,
    onBackupNow: () -> Unit,
    onRestoreDriveBackup: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showSettings = currentRoute == SettingsRoute
    val shouldShowOnboarding = !uiState.isLoading && !uiState.onboardingCompleted && uiState.entries.isEmpty()
    val settingsRotation by animateFloatAsState(
        targetValue = if (showSettings) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 120f),
        label = "settingsRotation",
    )
    WeightTrackerTheme {
        LaunchedEffect(uiState.message) {
            val message = uiState.message ?: return@LaunchedEffect
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
        LaunchedEffect(shouldShowOnboarding, currentRoute) {
            if (shouldShowOnboarding && currentRoute != OnboardingRoute) {
                navController.navigate(OnboardingRoute) {
                    popUpTo(TrackerRoute) { inclusive = true }
                    launchSingleTop = true
                }
            } else if (!shouldShowOnboarding && currentRoute == OnboardingRoute) {
                navController.navigate(TrackerRoute) {
                    popUpTo(OnboardingRoute) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = TrackerRoute,
                enterTransition = {
                    slideInHorizontally { it } + scaleIn(initialScale = 0.98f)
                },
                exitTransition = {
                    slideOutHorizontally { -it / 4 } + scaleOut(targetScale = 0.98f)
                },
                popEnterTransition = {
                    slideInHorizontally { -it / 4 } + scaleIn(initialScale = 0.98f)
                },
                popExitTransition = {
                    slideOutHorizontally { it } + scaleOut(targetScale = 0.98f)
                },
            ) {
                composable(TrackerRoute) {
                    WeightTrackerScreen(
                        uiState = uiState,
                        settingsRotation = settingsRotation,
                        onSaveEntry = viewModel::saveEntry,
                        onDeleteEntry = viewModel::deleteEntry,
                        onExportCsv = onExportCsv,
                        onOpenSettings = {
                            navController.navigate(SettingsRoute) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(SettingsRoute) {
                    SettingsScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onUnitSystemChange = viewModel::setUnitSystem,
                        onBirthYearChange = viewModel::setBirthYear,
                        onExportCsv = onExportCsv,
                        onImportCsv = onImportCsv,
                        onConnectDrive = onConnectDrive,
                        onBackupNow = onBackupNow,
                        onRestoreDriveBackup = onRestoreDriveBackup,
                        onDisconnectDrive = viewModel::disconnectGoogleDrive,
                        onBackupFrequencyChange = viewModel::setBackupFrequency,
                    )
                }
                composable(OnboardingRoute) {
                    OnboardingScreen(
                        unitSystem = uiState.unitSystem,
                        onComplete = viewModel::completeOnboarding,
                    )
                }
            }
            ExpressiveSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        uiState.pendingImport?.let { pending ->
            AlertDialog(
                onDismissRequest = { viewModel.resolveCsvImport(DuplicateImportPolicy.Cancel) },
                title = { Text("Import duplicates?") },
                text = {
                    Text("${pending.duplicateCount} dates already exist. Replace those logs or skip them?")
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.resolveCsvImport(DuplicateImportPolicy.Replace) }) {
                        Text("Replace")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.resolveCsvImport(DuplicateImportPolicy.Skip) }) {
                            Text("Skip")
                        }
                        TextButton(onClick = { viewModel.resolveCsvImport(DuplicateImportPolicy.Cancel) }) {
                            Text("Cancel")
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun WeightTrackerTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val colorScheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) darkColorScheme() else lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightTrackerScreen(
    uiState: WeightTrackerUiState,
    settingsRotation: Float,
    onSaveEntry: (WeightEntry?, LocalDate, Double, Double?, Double?) -> Unit,
    onDeleteEntry: (WeightEntry) -> Unit,
    onExportCsv: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<WeightEntry?>(null) }
    val weightDeltasById = remember(uiState.entries) {
        uiState.entries.mapIndexedNotNull { index, entry ->
            val previous = uiState.entries.getOrNull(index + 1) ?: return@mapIndexedNotNull null
            entry.id to (entry.weightKg - previous.weightKg)
        }.toMap()
    }
    val waistDeltasById = remember(uiState.entries) {
        uiState.entries.mapIndexedNotNull { index, entry ->
            val previousWaist = uiState.entries.getOrNull(index + 1)?.waistCm ?: return@mapIndexedNotNull null
            val currentWaist = entry.waistCm ?: return@mapIndexedNotNull null
            entry.id to (currentWaist - previousWaist)
        }.toMap()
    }
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            ExpressiveLogButton(
                onClick = {
                    editingEntry = null
                    showSheet = true
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Header(
                    entryCount = uiState.entries.size,
                    settingsRotation = settingsRotation,
                    onOpenSettings = onOpenSettings,
                )
            }
            item { SummaryPanel(uiState) }
            item { HealthCard(uiState = uiState) }
            item {
                SectionTitle(
                    title = "Recent logs",
                    action = if (uiState.entries.isEmpty()) null else "CSV",
                    onAction = if (uiState.entries.isEmpty()) null else onExportCsv,
                )
            }
            if (uiState.entries.isEmpty()) {
                item { EmptyState(onLog = { showSheet = true }) }
            } else {
                if (uiState.entries.size == 1) {
                    item {
                        LowDataState(onLog = { showSheet = true })
                    }
                }
                LogGrouping.groupByMonth(uiState.entries).forEach { group ->
                    item(key = group.month.toString()) {
                        MonthHeader(
                            label = group.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                                " ${group.month.year}",
                            count = group.entries.size,
                        )
                    }
                    items(group.entries, key = { it.id }) { entry ->
                        EntryRow(
                            entry = entry,
                            unitSystem = uiState.unitSystem,
                            weightDeltaKg = weightDeltasById[entry.id],
                            waistDeltaCm = waistDeltasById[entry.id],
                            modifier = Modifier.animateItem(),
                            onClick = {
                                editingEntry = entry
                                showSheet = true
                            },
                            onDelete = { onDeleteEntry(entry) },
                        )
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            EntrySheet(
                entry = editingEntry,
                latestEntry = uiState.entries.firstOrNull(),
                todayEntry = uiState.todayEntry,
                profileHeightCm = uiState.heightCm,
                unitSystem = uiState.unitSystem,
                onSave = { date, weightKg, waistCm, heightCm ->
                    onSaveEntry(editingEntry, date, weightKg, waistCm, heightCm)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                    }
                },
                onCancel = { showSheet = false },
            )
        }
    }
}

@Composable
private fun ExpressiveSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { data ->
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
            shadowElevation = 6.dp,
        ) {
            Text(
                text = data.visuals.message,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
        }
    }
}

@Composable
private fun ExpressiveLogButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 18.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(
                text = "Log weight",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun Header(
    entryCount: Int,
    settingsRotation: Float,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Weight Tracker",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$entryCount logs saved",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.graphicsLayer(rotationZ = settingsRotation),
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    uiState: WeightTrackerUiState,
    onBack: () -> Unit,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onBirthYearChange: (Int?) -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
    onConnectDrive: () -> Unit,
    onBackupNow: () -> Unit,
    onRestoreDriveBackup: () -> Unit,
    onDisconnectDrive: () -> Unit,
    onBackupFrequencyChange: (BackupFrequency) -> Unit,
) {
    val settingsBackground = MaterialTheme.colorScheme.surface
    val settingsText = MaterialTheme.colorScheme.onSurface
    var showBirthYearPicker by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(settingsBackground)
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = settingsText,
                )
            }
        }
        item {
            SettingsCard(title = "Units") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    UnitSystem.entries.forEach { unitSystem ->
                        val selected = uiState.unitSystem == unitSystem
                        Button(
                            onClick = { onUnitSystemChange(unitSystem) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(22.dp),
                            colors = if (selected) {
                                ButtonDefaults.buttonColors()
                            } else {
                                ButtonDefaults.filledTonalButtonColors()
                            },
                        ) {
                            Text(
                                text = if (unitSystem == UnitSystem.Metric) "kg / cm" else "lb / in",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
        item {
            SettingsCard(
                title = "Profile",
                icon = Icons.Outlined.Person,
                subtitle = "Birth year keeps health labels in the right context.",
            ) {
                FilledTonalButton(
                    onClick = { showBirthYearPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = uiState.birthYear?.toString() ?: "Pick birth year",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                }
                Text(
                    text = "Used to decide whether adult BMI categories apply.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            SettingsCard(title = "CSV") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FilledTonalButton(
                        onClick = onImportCsv,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                    ) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import")
                    }
                    Button(
                        onClick = onExportCsv,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                    ) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export")
                    }
                }
                Text(
                    text = "CSV import supports metric or imperial headers. Duplicate dates are handled when you import.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            SettingsCard(title = "Google Drive Backup") {
                val backup = uiState.backupSettings
                val backupInProgress = uiState.isBackupInProgress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (backup.connected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                    ) {
                        Icon(
                            imageVector = if (backup.connected) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = if (backup.connected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = if (backup.connected) "Connected to Google Drive" else "Not connected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = when {
                                backupInProgress -> "Uploading CSV and profile metadata..."
                                backup.lastBackupAtMillis != null -> "Last backup ${formatBackupTime(backup.lastBackupAtMillis)}"
                                else -> "Daily or weekly CSV backup in your Drive app data."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AnimatedVisibility(backupInProgress) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BackupFrequency.entries.forEach { frequency ->
                        val selected = backup.frequency == frequency
                        Button(
                            onClick = { onBackupFrequencyChange(frequency) },
                            modifier = Modifier.weight(1f),
                            enabled = !backupInProgress,
                            shape = RoundedCornerShape(22.dp),
                            colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                        ) {
                            Text(
                                text = frequency.label,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                if (backup.connected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = onBackupNow,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            enabled = !backupInProgress,
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                        ) {
                            AnimatedContent(
                                targetState = backupInProgress,
                                label = "backupButtonContent",
                            ) { inProgress ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(Icons.Outlined.Sync, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (inProgress) "Backing up" else "Backup now")
                                }
                            }
                        }
                        FilledTonalButton(
                            onClick = onRestoreDriveBackup,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            enabled = !backupInProgress,
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Restore")
                        }
                    }
                    TextButton(onClick = onDisconnectDrive) {
                        Text("Disconnect Google Drive")
                    }
                } else {
                    Button(
                        onClick = onConnectDrive,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    ) {
                        Icon(Icons.Outlined.CloudDone, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Connect Google Drive")
                    }
                }
                backup.lastError?.takeIf { backup.lastStatus == BackupStatus.Failed }?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
    if (showBirthYearPicker) {
        YearPickerDialog(
            selectedYear = uiState.birthYear,
            onDismiss = { showBirthYearPicker = false },
            onYearSelected = {
                onBirthYearChange(it)
                showBirthYearPicker = false
            },
        )
    }
}

private fun formatBackupTime(millis: Long): String =
    DateTimeFormatter.ofPattern("MMM d, yyyy")
        .format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (icon == null && subtitle == null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun OnboardingScreen(
    unitSystem: UnitSystem,
    onComplete: (UnitSystem, Int?, Double?, LocalDate?, Double?, Double?) -> Unit,
) {
    val pageCount = 5
    var page by remember { mutableStateOf(0) }
    var selectedUnitSystem by remember(unitSystem) { mutableStateOf(unitSystem) }
    var selectedBirthYear by remember { mutableStateOf<Int?>(null) }
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var waistText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = page > 0) {
        page -= 1
        error = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LinearProgressIndicator(
                progress = { (page + 1) / pageCount.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            Text(
                text = "Step ${page + 1} of $pageCount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedContent(
            targetState = page,
            label = "onboardingPage",
            modifier = Modifier.weight(1f),
        ) { currentPage ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    when (currentPage) {
                        0 -> OnboardingUnitsPage(
                            selectedUnitSystem = selectedUnitSystem,
                            onUnitSystemChange = {
                                selectedUnitSystem = it
                                error = null
                            },
                        )
                        1 -> OnboardingProfilePage(
                            selectedBirthYear = selectedBirthYear,
                            heightText = heightText,
                            unitSystem = selectedUnitSystem,
                            onShowYearPicker = { showYearPicker = true },
                            onHeightChange = {
                                heightText = it
                                error = null
                            },
                        )
                        2 -> OnboardingFirstLogPage(
                            selectedDate = selectedDate,
                            weightText = weightText,
                            waistText = waistText,
                            unitSystem = selectedUnitSystem,
                            onShowDatePicker = { showDatePicker = true },
                            onWeightChange = {
                                weightText = it
                                error = null
                            },
                            onWaistChange = {
                                waistText = it
                                error = null
                            },
                        )
                        3 -> OnboardingPreviewPage(
                            unitSystem = selectedUnitSystem,
                            weightText = weightText,
                            waistText = waistText,
                        )
                        else -> OnboardingSwipeDemoPage(
                            unitSystem = selectedUnitSystem,
                            selectedDate = selectedDate,
                            weightText = weightText,
                            waistText = waistText,
                        )
                    }
                }
                item {
                    AnimatedVisibility(error != null) {
                        Text(
                            text = error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
            tonalElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = {
                        if (page == 0) {
                            onComplete(selectedUnitSystem, null, null, null, null, null)
                        } else {
                            page -= 1
                            error = null
                        }
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .heightIn(min = 58.dp),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(if (page == 0) "Skip" else "Back", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        when (page) {
                            1 -> {
                                val parsedHeight = heightText.toDoubleOrNull()
                                val currentYear = Year.now().value
                                when {
                                    selectedBirthYear == null -> error = "Pick your birth year."
                                    selectedBirthYear !in 1900..currentYear -> {
                                        error = "Pick a birth year from 1900 to $currentYear."
                                    }
                                    parsedHeight == null || parsedHeight <= 0.0 -> {
                                        error = "Enter a valid height."
                                    }
                                    else -> {
                                        page += 1
                                        error = null
                                    }
                                }
                            }
                            2 -> {
                                val parsedWeight = weightText.toDoubleOrNull()
                                val parsedWaist = waistText.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                                when {
                                    parsedWeight == null || parsedWeight <= 0.0 -> {
                                        error = "Enter a valid weight."
                                    }
                                    waistText.isNotBlank() && (parsedWaist == null || parsedWaist <= 0.0) -> {
                                        error = "Enter a valid waist or leave it blank."
                                    }
                                    else -> {
                                        page += 1
                                        error = null
                                    }
                                }
                            }
                            4 -> {
                                val parsedHeight = heightText.toDoubleOrNull()
                                val parsedWeight = weightText.toDoubleOrNull()
                                val parsedWaist = waistText.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                                onComplete(
                                    selectedUnitSystem,
                                    selectedBirthYear,
                                    parsedHeight?.let { UnitConverter.lengthToCm(it, selectedUnitSystem) },
                                    selectedDate,
                                    parsedWeight?.let { UnitConverter.weightToKg(it, selectedUnitSystem) },
                                    parsedWaist?.let { UnitConverter.lengthToCm(it, selectedUnitSystem) },
                                )
                            }
                            else -> {
                                page += 1
                                error = null
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .heightIn(min = 58.dp),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = if (page == pageCount - 1) "Start tracking" else "Next",
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (page == pageCount - 1) {
                            Icons.Outlined.Check
                        } else {
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight
                        },
                        contentDescription = null,
                    )
                }
            }
        }
    }

    if (showYearPicker) {
        YearPickerDialog(
            selectedYear = selectedBirthYear,
            onDismiss = { showYearPicker = false },
            onYearSelected = {
                selectedBirthYear = it
                showYearPicker = false
                error = null
            },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = millis.toLocalDateUtc()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Use date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun OnboardingUnitsPage(
    selectedUnitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        OnboardingPageHeader(
            title = "Make it yours",
            subtitle = "Pick the units you want to see everywhere.",
        )
        SettingsCard(title = "Units") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UnitSystem.entries.forEach { option ->
                    val selected = selectedUnitSystem == option
                    Button(
                        onClick = { onUnitSystemChange(option) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(if (selected) 28.dp else 20.dp),
                        colors = if (selected) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        },
                    ) {
                        Text(
                            text = if (option == UnitSystem.Metric) "kg / cm" else "lb / in",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        OnboardingTipCard(
            title = "Private by default",
            text = "Your logs stay on this device unless you export CSV.",
        )
    }
}

@Composable
private fun OnboardingProfilePage(
    selectedBirthYear: Int?,
    heightText: String,
    unitSystem: UnitSystem,
    onShowYearPicker: () -> Unit,
    onHeightChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        OnboardingPageHeader(
            title = "Your profile",
            subtitle = "Height powers BMI and waist-to-height ratio. Birth year decides whether adult BMI labels apply.",
        )
        OnboardingInputCard(
            icon = Icons.Outlined.Person,
            title = "Profile",
            subtitle = "Set this once. You can change it later in Settings.",
        ) {
            FilledTonalButton(
                onClick = onShowYearPicker,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 62.dp),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            ) {
                Text(
                    text = selectedBirthYear?.toString() ?: "Pick birth year",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.ExtraBold,
                )
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            }
            OutlinedTextField(
                value = heightText,
                onValueChange = onHeightChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Height") },
                suffix = { Text(unitSystem.lengthLabel) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Height, contentDescription = null)
                },
                shape = RoundedCornerShape(22.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
    }
}

@Composable
private fun OnboardingFirstLogPage(
    selectedDate: LocalDate,
    weightText: String,
    waistText: String,
    unitSystem: UnitSystem,
    onShowDatePicker: () -> Unit,
    onWeightChange: (String) -> Unit,
    onWaistChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        OnboardingPageHeader(
            title = "First log",
            subtitle = "Start with today's weight. Waist is optional, but it unlocks the better shape signal.",
        )
        OnboardingInputCard(
            icon = Icons.Outlined.MonitorWeight,
            title = "Measurements",
            subtitle = "These values seed your dashboard preview.",
        ) {
            OutlinedTextField(
                value = selectedDate.toString(),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowDatePicker() },
                label = { Text("Date") },
                shape = RoundedCornerShape(18.dp),
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = onShowDatePicker) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Pick date",
                        )
                    }
                },
            )
            OutlinedTextField(
                value = weightText,
                onValueChange = onWeightChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Weight") },
                suffix = { Text(unitSystem.weightLabel) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.MonitorWeight, contentDescription = null)
                },
                shape = RoundedCornerShape(22.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                value = waistText,
                onValueChange = onWaistChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Waist") },
                suffix = { Text(unitSystem.lengthLabel) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Straighten, contentDescription = null)
                },
                shape = RoundedCornerShape(22.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Optional") },
            )
        }
    }
}

@Composable
private fun OnboardingPreviewPage(
    unitSystem: UnitSystem,
    weightText: String,
    waistText: String,
) {
    var expanded by remember { mutableStateOf(false) }
    val previewWeightKg = weightText.toDoubleOrNull()
        ?.let { UnitConverter.weightToKg(it, unitSystem) }
    val previewWaistCm = waistText.takeIf { it.isNotBlank() }?.toDoubleOrNull()
        ?.let { UnitConverter.lengthToCm(it, unitSystem) }
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        OnboardingPageHeader(
            title = "You are set",
            subtitle = "Cards are interactive. Try the Current card before you enter the app.",
        )
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(36.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "Current",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = previewWeightKg?.let { formatWeight(it, unitSystem) } ?: "-- ${unitSystem.weightLabel}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricPill(
                        label = "Last change",
                        value = "No trend",
                        modifier = Modifier.weight(1f),
                    )
                    MetricPill(
                        label = "Waist",
                        value = previewWaistCm?.let { formatLength(it, unitSystem) } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                }
                AnimatedVisibility(expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Weight trend",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.75f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val padding = 28.dp.toPx()
                                val points = listOf(
                                    Offset(padding, size.height * 0.62f),
                                    Offset(size.width * 0.34f, size.height * 0.48f),
                                    Offset(size.width * 0.58f, size.height * 0.54f),
                                    Offset(size.width - padding, size.height * 0.34f),
                                )
                                val path = Path().apply {
                                    points.forEachIndexed { index, point ->
                                        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                                    }
                                }
                                drawPath(
                                    path = Path().apply {
                                        addPath(path)
                                        lineTo(points.last().x, size.height - padding)
                                        lineTo(points.first().x, size.height - padding)
                                        close()
                                    },
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF6750A4).copy(alpha = 0.28f),
                                            Color(0xFF6750A4).copy(alpha = 0.04f),
                                        ),
                                    ),
                                )
                                drawPath(
                                    path = path,
                                    color = Color(0xFF6750A4),
                                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                                )
                                points.forEach { point ->
                                    drawCircle(Color(0xFF6750A4), radius = 6.dp.toPx(), center = point)
                                    drawCircle(Color.White, radius = 2.5.dp.toPx(), center = point)
                                }
                            }
                        }
                        Text(
                            text = "Tap cards like this to expand trends and details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingSwipeDemoPage(
    unitSystem: UnitSystem,
    selectedDate: LocalDate,
    weightText: String,
    waistText: String,
) {
    val previewWeightKg = weightText.toDoubleOrNull()
        ?.let { UnitConverter.weightToKg(it, unitSystem) }
        ?: UnitConverter.weightToKg(78.2, unitSystem)
    val previewWaistCm = waistText.takeIf { it.isNotBlank() }?.toDoubleOrNull()
        ?.let { UnitConverter.lengthToCm(it, unitSystem) }
    var shifted by remember { mutableStateOf(false) }
    val offset by animateDpAsState(
        targetValue = if (shifted) (-52).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = 180f),
        label = "onboardingSwipeDemoOffset",
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(900)
            shifted = true
            delay(950)
            shifted = false
            delay(900)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        OnboardingPageHeader(
            title = "Keep logs tidy",
            subtitle = "Recent logs can be edited with a tap. Swipe left when you want to delete one.",
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.errorContainer,
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Surface(
                    modifier = Modifier
                        .offset(x = offset)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TimelineDateBadge(
                            entry = WeightEntry(
                                id = 0,
                                date = selectedDate,
                                weightKg = previewWeightKg,
                                waistCm = previewWaistCm,
                                createdAtMillis = 0,
                                updatedAtMillis = 0,
                            ),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = formatRelativeLogDate(selectedDate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                text = formatWeight(previewWeightKg, unitSystem),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = previewWaistCm?.let { "Waist ${formatLength(it, unitSystem)}" } ?: "Swipe left to delete",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        OnboardingTipCard(
            title = "Confirmation first",
            text = "The app asks before deleting, so a stray swipe will not erase a log.",
        )
    }
}

@Composable
private fun OnboardingPageHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OnboardingInputCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(21.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun OnboardingTipCard(title: String, text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun YearPickerDialog(
    selectedYear: Int?,
    onDismiss: () -> Unit,
    onYearSelected: (Int) -> Unit,
) {
    val currentYear = Year.now().value
    val years = remember { (currentYear downTo 1900).toList() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Birth year") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(years) { year ->
                    val selected = year == selectedYear
                    FilledTonalButton(
                        onClick = { onYearSelected(year) },
                        shape = RoundedCornerShape(if (selected) 22.dp else 16.dp),
                        colors = if (selected) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    ) {
                        Text(year.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun SummaryPanel(uiState: WeightTrackerUiState) {
    var expanded by remember { mutableStateOf(false) }
    var trendMode by remember { mutableStateOf(TrendMode.Weight) }
    val latest = uiState.trend.latestWeightKg
    val unitSystem = uiState.unitSystem
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Current",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = latest?.let { formatWeight(it, unitSystem) } ?: "-- ${unitSystem.weightLabel}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricPill(
                    label = "Last change",
                    value = uiState.trend.previousDeltaKg?.let { formatDelta(it, unitSystem) } ?: "No trend",
                    modifier = Modifier.weight(1f),
                )
                MetricPill(
                    label = "Waist",
                    value = uiState.trend.latestWaistCm?.let { formatLength(it, unitSystem) } ?: "--",
                    modifier = Modifier.weight(1f),
                )
            }
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TrendModeSelector(
                        selected = trendMode,
                        onSelected = { trendMode = it },
                    )
                    TrendChartPanel(
                        entriesNewestFirst = uiState.entries,
                        mode = trendMode,
                    )
                }
            }
        }
    }
}

private enum class TrendMode {
    Weight,
    Waist,
}

@Composable
private fun TrendModeSelector(selected: TrendMode, onSelected: (TrendMode) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
        ) {
            val indicatorWidth = maxWidth / 2
            val indicatorOffset by animateDpAsState(
                targetValue = if (selected == TrendMode.Weight) 0.dp else indicatorWidth,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 360f),
                label = "trendModeIndicatorOffset",
            )
            Surface(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .fillMaxWidth(0.5f)
                    .height(44.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {}
            Row(modifier = Modifier.fillMaxWidth()) {
                TrendMode.entries.forEach { mode ->
                    val isSelected = mode == selected
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 260f),
                        label = "trendModeContentColor",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelected(mode) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = mode.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendChartPanel(entriesNewestFirst: List<WeightEntry>, mode: TrendMode) {
    val relevantCount = when (mode) {
        TrendMode.Weight -> entriesNewestFirst.size
        TrendMode.Waist -> entriesNewestFirst.count { it.waistCm != null }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.75f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
    ) {
        if (relevantCount < 2) {
            Text(
                text = "Add two logs to draw this trend.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
        } else {
            TrendCanvas(entriesNewestFirst = entriesNewestFirst, mode = mode)
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun HealthCard(uiState: WeightTrackerUiState) {
    var expanded by remember { mutableStateOf(false) }
    val bmi = uiState.bmi
    val waistToHeight = uiState.waistToHeight
    val age = uiState.birthYear?.let { Year.now().value - it }
    val adultBmiApplies = age != null && age >= 20

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Health",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = bmi?.let { formatOneDecimal(it.value) } ?: "--",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                ) {
                    Text(
                        text = when {
                            bmi == null -> "Add height"
                            age == null -> "Add birth year"
                            !adultBmiApplies -> "Under 20"
                            else -> bmi.category.label
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricPill(
                    label = "BMI",
                    value = bmi?.let { formatOneDecimal(it.value) } ?: "--",
                    modifier = Modifier.weight(1f),
                )
                MetricPill(
                    label = "Waist / height",
                    value = waistToHeight?.let { formatTwoDecimals(it.value) } ?: "--",
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = when {
                    bmi == null -> "Add height once and BMI will show up here."
                    age == null -> "Add birth year to apply adult BMI categories."
                    !adultBmiApplies -> "BMI categories for under-20 users need age and sex percentile charts."
                    else -> bmi.category.detail
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "BMI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    if (adultBmiApplies) {
                        BmiSegmentedRange(bmi = bmi)
                    } else {
                        Text(
                            text = "Adult BMI categories apply from age 20. Use BMI as a rough screening signal, not a diagnosis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.78f),
                        )
                    }
                    Text(
                        text = "Waist-to-height ratio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    WaistToHeightSegmentedRange(summary = waistToHeight)
                    Text(
                        text = "These are screening signals for trend checks, not medical advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.78f),
                    )
                }
            }
        }
    }
}

@Composable
private fun WaistToHeightSegmentedRange(summary: com.aditya.weighttracker.domain.WaistToHeightSummary?) {
    val progressTarget = summary?.let { ((it.value - 0.3) / 0.45).toFloat().coerceIn(0f, 1f) } ?: 0f
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 70f),
        label = "waistToHeightRange",
    )
    val categories = listOf(
        WaistToHeightCategory.Low to Color(0xFF4C7DFF),
        WaistToHeightCategory.InRange to Color(0xFF1B8F5A),
        WaistToHeightCategory.Increased to Color(0xFFF0A202),
        WaistToHeightCategory.High to Color(0xFFD12C4B),
    )
    val marker = MaterialTheme.colorScheme.onTertiaryContainer

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(50)),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                categories.forEach { (_, color) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(color),
                    )
                }
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val markerX = size.width * progress
                val centerY = size.height / 2f
                drawLine(
                    color = marker,
                    start = Offset(markerX, centerY - 18.dp.toPx()),
                    end = Offset(markerX, centerY + 18.dp.toPx()),
                    strokeWidth = 7.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(markerX, centerY),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            categories.forEach { (category, color) ->
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = summary?.category?.detail ?: "Add waist and height to calculate this.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.78f),
        )
    }
}

@Composable
private fun BmiSegmentedRange(bmi: com.aditya.weighttracker.domain.BmiSummary?) {
    val progressTarget = bmi?.let { ((it.value - 12.0) / 30.0).toFloat().coerceIn(0f, 1f) } ?: 0f
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 70f),
        label = "bmiRange",
    )
    val categories = listOf(
        BmiCategory.Light to Color(0xFF4C7DFF),
        BmiCategory.InRange to Color(0xFF1B8F5A),
        BmiCategory.GettingHeavy to Color(0xFFF0A202),
        BmiCategory.High to Color(0xFFE56B1F),
        BmiCategory.VeryHigh to Color(0xFFD12C4B),
    )
    val marker = MaterialTheme.colorScheme.onTertiaryContainer

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(50)),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                categories.forEach { (_, color) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(color),
                    )
                }
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val markerX = size.width * progress
                val centerY = size.height / 2f
                drawLine(
                    color = marker,
                    start = Offset(markerX, centerY - 18.dp.toPx()),
                    end = Offset(markerX, centerY + 18.dp.toPx()),
                    strokeWidth = 7.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(markerX, centerY),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            categories.forEach { (category, color) ->
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TrendCanvas(entriesNewestFirst: List<WeightEntry>, mode: TrendMode) {
    val modeProgress by animateFloatAsState(
        targetValue = if (mode == TrendMode.Weight) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 120f),
        label = "chartModeProgress",
    )
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val lineColor = MaterialTheme.colorScheme.onSecondaryContainer
    val entries = entriesNewestFirst.sortedBy { it.date }
    val weightSeries = ChartSeries(entries.map { it.weightKg }, primary, 1f - modeProgress)
    val waistSeries = ChartSeries(entries.map { it.waistCm }, tertiary, modeProgress)
    val activeColor = lerp(primary, tertiary, modeProgress)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val padding = 28.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        for (i in 0..3) {
            val y = padding + chartHeight * (i / 3f)
            drawLine(
                color = lineColor.copy(alpha = 0.13f),
                start = Offset(padding, y),
                end = Offset(size.width - padding, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        listOf(weightSeries, waistSeries).forEachIndexed { seriesIndex, chartSeries ->
            if (chartSeries.alpha <= 0.01f) return@forEachIndexed
            val points = chartSeries.values.mapIndexedNotNull { index, value ->
                value ?: return@mapIndexedNotNull null
                val values = chartSeries.values.filterNotNull()
                val min = values.minOrNull() ?: return@mapIndexedNotNull null
                val max = values.maxOrNull() ?: return@mapIndexedNotNull null
                val range = (max - min).takeIf { it > 0.01 } ?: 1.0
                val x = padding + chartWidth * (index / (entries.lastIndex).coerceAtLeast(1).toFloat())
                val normalized = ((value - min) / range).toFloat()
                val y = padding + chartHeight * (1f - normalized)
                Offset(x, y)
            }
            if (points.size < 2) return@forEachIndexed
            val path = Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }
            if (chartSeries.alpha > 0.5f) {
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(points.last().x, size.height - padding)
                    lineTo(points.first().x, size.height - padding)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.28f * chartSeries.alpha),
                            activeColor.copy(alpha = 0.04f * chartSeries.alpha),
                        ),
                    ),
                )
            }
            points.lastOrNull()?.let { lastPoint ->
                drawLine(
                    color = activeColor.copy(alpha = 0.18f * chartSeries.alpha),
                    start = Offset(padding, lastPoint.y),
                    end = Offset(size.width - padding, lastPoint.y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            drawPath(
                path = path,
                color = chartSeries.color.copy(alpha = chartSeries.alpha),
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
            )
            points.forEachIndexed { index, point ->
                val alpha = (if (index == points.lastIndex) 1f else 0.52f) * chartSeries.alpha
                drawCircle(color = chartSeries.color.copy(alpha = alpha), radius = 6.dp.toPx(), center = point)
                drawCircle(color = Color.White.copy(alpha = alpha), radius = 2.5.dp.toPx(), center = point)
            }
        }
    }
}

private data class ChartSeries(
    val values: List<Double?>,
    val color: Color,
    val alpha: Float,
)

@Composable
private fun SectionTitle(
    title: String,
    action: String?,
    onAction: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        if (action != null && onAction != null) {
            FilledTonalButton(
                onClick = onAction,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(action, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MonthHeader(label: String, count: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$count ${if (count == 1) "log" else "logs"}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EntryRow(
    entry: WeightEntry,
    unitSystem: UnitSystem,
    weightDeltaKg: Double?,
    waistDeltaCm: Double?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var highlight by remember(entry.updatedAtMillis) {
        mutableStateOf(System.currentTimeMillis() - entry.updatedAtMillis < 2500)
    }
    LaunchedEffect(entry.updatedAtMillis) {
        if (highlight) {
            delay(1400)
            highlight = false
        }
    }
    val containerColor by animateColorAsState(
        targetValue = if (highlight) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 130f),
        label = "entryHighlight",
    )
    val dateLabel = formatRelativeLogDate(entry.date)
    val fullDate = entry.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))

    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isRemoving by remember { mutableStateOf(false) }
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            showDeleteConfirmation = true
        }
    }
    AnimatedVisibility(
        visible = !isRemoving,
        modifier = modifier,
        exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.9f, stiffness = 260f)) + fadeOut(),
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            },
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clip(RoundedCornerShape(26.dp))
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(26.dp),
                color = containerColor,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TimelineDateBadge(entry = entry)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = fullDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = formatWeight(entry.weightKg, unitSystem),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DeltaChip(delta = weightDeltaKg, unitSystem = unitSystem, isWeight = true)
                            entry.waistCm?.let {
                                InfoChip(text = "Waist ${formatLength(it, unitSystem)}")
                            }
                            waistDeltaCm?.let {
                                DeltaChip(delta = it, unitSystem = unitSystem, isWeight = false)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                scope.launch { dismissState.reset() }
            },
            title = { Text("Delete log?") },
            text = {
                Text("This will remove the ${formatWeight(entry.weightKg, unitSystem)} log from $fullDate.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        scope.launch {
                            isRemoving = true
                            delay(220)
                            onDelete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Delete", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        scope.launch { dismissState.reset() }
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun TimelineDateBadge(entry: WeightEntry) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = entry.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = entry.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.78f),
                )
            }
        }
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeltaChip(delta: Double?, unitSystem: UnitSystem, isWeight: Boolean) {
    val stableDelta = delta?.takeIf { kotlin.math.abs(it) >= 0.05 }
    val tint = when {
        stableDelta == null -> MaterialTheme.colorScheme.onSurfaceVariant
        stableDelta < 0.0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    val icon = when {
        stableDelta == null -> Icons.AutoMirrored.Outlined.TrendingFlat
        stableDelta < 0.0 -> Icons.AutoMirrored.Outlined.TrendingDown
        else -> Icons.AutoMirrored.Outlined.TrendingUp
    }
    val label = when {
        delta == null -> "No prior"
        isWeight -> formatDelta(delta, unitSystem)
        else -> "${if (delta > 0.0) "+" else ""}${formatLength(delta, unitSystem)} waist"
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = tint,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
        }
    }
}

@Composable
private fun EmptyState(onLog: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Start with today's weight.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Waist is optional. Export appears after your first log.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onLog,
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text("Log weight", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun LowDataState(onLog: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "One more log unlocks trends.",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "Add another day to see movement in your weight card.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f),
                )
            }
            FilledTonalButton(
                onClick = onLog,
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EntrySheet(
    entry: WeightEntry?,
    latestEntry: WeightEntry?,
    todayEntry: WeightEntry?,
    profileHeightCm: Double?,
    unitSystem: UnitSystem,
    onSave: (LocalDate, Double, Double?, Double?) -> Unit,
    onCancel: () -> Unit,
) {
    val prefillEntry = entry ?: todayEntry
    var selectedDate by remember(entry) { mutableStateOf(entry?.date ?: LocalDate.now()) }
    var weightText by remember(entry, todayEntry, unitSystem) {
        mutableStateOf(
            prefillEntry?.weightKg
                ?.let { UnitConverter.weightFromKg(it, unitSystem) }
                ?.let(::formatOneDecimal)
                .orEmpty(),
        )
    }
    var waistText by remember(entry, todayEntry, latestEntry, unitSystem) {
        mutableStateOf(
            (prefillEntry?.waistCm ?: latestEntry?.waistCm)
                ?.let { UnitConverter.lengthFromCm(it, unitSystem) }
                ?.let(::formatOneDecimal)
                .orEmpty(),
        )
    }
    var heightText by remember(profileHeightCm, unitSystem) {
        mutableStateOf(
            profileHeightCm
                ?.let { UnitConverter.lengthFromCm(it, unitSystem) }
                ?.let(::formatOneDecimal)
                .orEmpty(),
        )
    }
    var error by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val isUpdatingToday = entry == null && todayEntry != null && selectedDate == LocalDate.now()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.MonitorWeight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = if (entry == null) "Log weight" else "Edit log",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = if (isUpdatingToday) {
                        "Today's log is ready to update."
                    } else {
                        "Weight is required. Waist and height can stay blank."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        OutlinedTextField(
            value = selectedDate.toString(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            label = { Text("Date") },
            shape = RoundedCornerShape(18.dp),
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Pick date",
                    )
                }
            },
            supportingText = {
                Text(if (isUpdatingToday) "Saving will update today's existing log." else "Tap to pick from calendar")
            },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Measurements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (prefillEntry != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = "Prefilled",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weight") },
                    suffix = { Text(unitSystem.weightLabel) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.MonitorWeight, contentDescription = null)
                    },
                    shape = RoundedCornerShape(22.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = waistText,
                    onValueChange = { waistText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Waist") },
                    suffix = { Text(unitSystem.lengthLabel) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Straighten, contentDescription = null)
                    },
                    shape = RoundedCornerShape(22.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Height") },
                    suffix = { Text(unitSystem.lengthLabel) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Height, contentDescription = null)
                    },
                    shape = RoundedCornerShape(22.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("Saved as your profile height") },
                )
            }
        }
        AnimatedVisibility(error != null) {
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(0.82f)
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val parsedWeight = weightText.toDoubleOrNull()
                        val parsedWaist = waistText.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                        val parsedHeight = heightText.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                        when {
                            parsedWeight == null || parsedWeight <= 0.0 -> error = "Enter a valid weight."
                            waistText.isNotBlank() && (parsedWaist == null || parsedWaist <= 0.0) -> {
                                error = "Enter a valid waist or leave it blank."
                            }
                            heightText.isNotBlank() && (parsedHeight == null || parsedHeight <= 0.0) -> {
                                error = "Enter a valid height or leave it blank."
                            }
                            else -> onSave(
                                selectedDate,
                                UnitConverter.weightToKg(parsedWeight, unitSystem),
                                parsedWaist?.let { UnitConverter.lengthToCm(it, unitSystem) },
                                parsedHeight?.let { UnitConverter.lengthToCm(it, unitSystem) },
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1.18f)
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = if (entry == null) "Save log" else "Save changes",
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = millis.toLocalDateUtc()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Use date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatOneDecimal(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun formatTwoDecimals(value: Double): String =
    String.format(Locale.US, "%.2f", value)

private fun formatWeight(weightKg: Double, unitSystem: UnitSystem): String =
    "${formatOneDecimal(UnitConverter.weightFromKg(weightKg, unitSystem))} ${unitSystem.weightLabel}"

private fun formatLength(lengthCm: Double, unitSystem: UnitSystem): String =
    "${formatOneDecimal(UnitConverter.lengthFromCm(lengthCm, unitSystem))} ${unitSystem.lengthLabel}"

private fun formatRelativeLogDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun formatDelta(value: Double, unitSystem: UnitSystem): String {
    val prefix = if (value > 0) "+" else ""
    return "$prefix${formatWeight(value, unitSystem)}"
}

@Preview
@Composable
private fun WeightTrackerPreview() {
    WeightTrackerTheme {
        WeightTrackerScreen(
            uiState = WeightTrackerUiState(
                entries = listOf(
                    WeightEntry(3, LocalDate.now(), 78.2, 88.0, 0, 0),
                    WeightEntry(2, LocalDate.now().minusDays(6), 79.1, 89.0, 0, 0),
                    WeightEntry(1, LocalDate.now().minusDays(13), 80.0, null, 0, 0),
                ),
                trend = com.aditya.weighttracker.domain.TrendCalculator.calculate(
                    listOf(
                        WeightEntry(3, LocalDate.now(), 78.2, 88.0, 0, 0),
                        WeightEntry(2, LocalDate.now().minusDays(6), 79.1, 89.0, 0, 0),
                        WeightEntry(1, LocalDate.now().minusDays(13), 80.0, null, 0, 0),
                    ),
                ),
                heightCm = 178.0,
                bmi = BmiCalculator.calculate(78.2, 178.0),
            ),
            settingsRotation = 0f,
            onSaveEntry = { _, _, _, _, _ -> },
            onDeleteEntry = {},
            onExportCsv = {},
            onOpenSettings = {},
        )
    }
}
