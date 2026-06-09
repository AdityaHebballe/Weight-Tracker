package com.example.weighttracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weighttracker.data.WeightEntry
import com.example.weighttracker.domain.BmiCategory
import com.example.weighttracker.domain.BmiCalculator
import com.example.weighttracker.domain.DuplicateImportPolicy
import com.example.weighttracker.domain.LogGrouping
import com.example.weighttracker.domain.UnitConverter
import com.example.weighttracker.domain.UnitSystem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun WeightTrackerApp(
    viewModel: WeightTrackerViewModel,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    WeightTrackerTheme {
        BackHandler(enabled = showSettings) {
            showSettings = false
        }
        if (showSettings) {
            SettingsScreen(
                uiState = uiState,
                onBack = { showSettings = false },
                onUnitSystemChange = viewModel::setUnitSystem,
                onExportCsv = onExportCsv,
                onImportCsv = onImportCsv,
            )
        } else {
            WeightTrackerScreen(
                uiState = uiState,
                onSaveEntry = viewModel::saveEntry,
                onDeleteEntry = viewModel::deleteEntry,
                onExportCsv = onExportCsv,
                onOpenSettings = { showSettings = true },
                onMessageShown = viewModel::clearMessage,
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
    onSaveEntry: (WeightEntry?, LocalDate, Double, Double?, Double?) -> Unit,
    onDeleteEntry: (WeightEntry) -> Unit,
    onExportCsv: () -> Unit,
    onOpenSettings: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<WeightEntry?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageShown()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { ExpressiveSnackbarHost(snackbarHostState) },
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
                    onOpenSettings = onOpenSettings,
                )
            }
            item { SummaryPanel(uiState) }
            item { BmiCard(uiState = uiState) }
            item {
                SectionTitle(
                    title = "Recent logs",
                    action = if (uiState.entries.isEmpty()) null else "CSV",
                    onAction = if (uiState.entries.isEmpty()) null else onExportCsv,
                )
            }
            if (uiState.entries.isEmpty()) {
                item { EmptyState() }
            } else {
                LogGrouping.groupByMonth(uiState.entries).forEach { group ->
                    item(key = group.month.toString()) {
                        Text(
                            text = group.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                                " ${group.month.year}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    items(group.entries, key = { it.id }) { entry ->
                        EntryRow(
                            entry = entry,
                            unitSystem = uiState.unitSystem,
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
private fun ExpressiveSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
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
                text = "$entryCount logs saved locally",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onOpenSettings) {
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
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
) {
    val settingsBackground = MaterialTheme.colorScheme.surface
    val settingsText = MaterialTheme.colorScheme.onSurface
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
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
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
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TrendMode.entries.forEach { mode ->
                val isSelected = mode == selected
                FilledTonalButton(
                    onClick = { onSelected(mode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = if (isSelected) {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = mode.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
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
        contentAlignment = Alignment.Center,
    ) {
        if (relevantCount < 2) {
            Text(
                text = "Add two logs to draw this trend.",
                modifier = Modifier.padding(24.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun BmiCard(uiState: WeightTrackerUiState) {
    var expanded by remember { mutableStateOf(false) }
    val bmi = uiState.bmi

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
                        text = "BMI",
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
                        text = bmi?.category?.label ?: "Add height",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Text(
                text = bmi?.category?.detail ?: "Add height once and BMI will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BmiSegmentedRange(bmi = bmi)
                    Text(
                        text = "BMI is a rough signal, not a diagnosis. Use it as a quick trend check.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.78f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BmiSegmentedRange(bmi: com.example.weighttracker.domain.BmiSummary?) {
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
private fun WeightChart(entriesNewestFirst: List<WeightEntry>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionTitle(title = "Trend", action = null, onAction = null)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.65f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)),
                contentAlignment = Alignment.Center,
            ) {
                if (entriesNewestFirst.size < 2) {
                    Text(
                        text = "Add two logs to draw your first trend.",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    TrendCanvas(entriesNewestFirst, TrendMode.Weight)
                }
            }
        }
    }
}

@Composable
private fun TrendCanvas(entriesNewestFirst: List<WeightEntry>, mode: TrendMode) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 55f),
        label = "chartProgress",
    )
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val lineColor = MaterialTheme.colorScheme.onSecondaryContainer
    val entries = entriesNewestFirst.sortedBy { it.date }
    val series = buildList {
        if (mode == TrendMode.Weight) {
            add(ChartSeries(entries.map { it.weightKg }, primary))
        }
        if (mode == TrendMode.Waist) {
            add(ChartSeries(entries.map { it.waistCm }, tertiary))
        }
    }

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

        series.forEachIndexed { seriesIndex, chartSeries ->
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
            val visibleLast = (points.lastIndex * progress).toInt().coerceAtLeast(1)
            val visiblePoints = points.take(visibleLast + 1)
            val path = Path().apply {
                visiblePoints.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }
            if (seriesIndex == 0) {
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(visiblePoints.last().x, size.height - padding)
                    lineTo(visiblePoints.first().x, size.height - padding)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(chartSeries.color.copy(alpha = 0.28f), chartSeries.color.copy(alpha = 0.04f)),
                    ),
                )
            }
            drawPath(
                path = path,
                color = chartSeries.color,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
            )
            visiblePoints.forEachIndexed { index, point ->
                val alpha = if (index == visiblePoints.lastIndex) 1f else 0.52f
                drawCircle(color = chartSeries.color.copy(alpha = alpha), radius = 6.dp.toPx(), center = point)
                drawCircle(color = Color.White.copy(alpha = alpha), radius = 2.5.dp.toPx(), center = point)
            }
        }
    }
}

private data class ChartSeries(
    val values: List<Double?>,
    val color: Color,
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
private fun EntryRow(
    entry: WeightEntry,
    unitSystem: UnitSystem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = entry.date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = formatWeight(entry.weightKg, unitSystem),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "${entry.date}${entry.waistCm?.let { " | ${formatLength(it, unitSystem)} waist" } ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FilledTonalButton(
                onClick = onDelete,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text("Delete", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EmptyState() {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = if (entry == null) "Log weight" else "Edit log",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        OutlinedTextField(
            value = selectedDate.toString(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { showDatePicker = true },
            label = { Text("Date") },
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
            supportingText = { Text("Tap to pick from calendar") },
        )
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Weight") },
            suffix = { Text(unitSystem.weightLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        OutlinedTextField(
            value = waistText,
            onValueChange = { waistText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Waist") },
            suffix = { Text(unitSystem.lengthLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Height") },
            suffix = { Text(unitSystem.lengthLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            supportingText = { Text("Saved as your profile height") },
        )
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

private fun formatWeight(weightKg: Double, unitSystem: UnitSystem): String =
    "${formatOneDecimal(UnitConverter.weightFromKg(weightKg, unitSystem))} ${unitSystem.weightLabel}"

private fun formatLength(lengthCm: Double, unitSystem: UnitSystem): String =
    "${formatOneDecimal(UnitConverter.lengthFromCm(lengthCm, unitSystem))} ${unitSystem.lengthLabel}"

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
                trend = com.example.weighttracker.domain.TrendCalculator.calculate(
                    listOf(
                        WeightEntry(3, LocalDate.now(), 78.2, 88.0, 0, 0),
                        WeightEntry(2, LocalDate.now().minusDays(6), 79.1, 89.0, 0, 0),
                        WeightEntry(1, LocalDate.now().minusDays(13), 80.0, null, 0, 0),
                    ),
                ),
                heightCm = 178.0,
                bmi = BmiCalculator.calculate(78.2, 178.0),
            ),
            onSaveEntry = { _, _, _, _, _ -> },
            onDeleteEntry = {},
            onExportCsv = {},
            onOpenSettings = {},
            onMessageShown = {},
        )
    }
}
