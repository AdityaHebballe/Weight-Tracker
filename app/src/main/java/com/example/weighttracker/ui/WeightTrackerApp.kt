package com.example.weighttracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
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
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun WeightTrackerApp(
    viewModel: WeightTrackerViewModel,
    onExportCsv: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WeightTrackerTheme {
        WeightTrackerScreen(
            uiState = uiState,
            onSaveEntry = viewModel::saveEntry,
            onDeleteEntry = viewModel::deleteEntry,
            onExportCsv = onExportCsv,
            onMessageShown = viewModel::clearMessage,
        )
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
    onSaveEntry: (WeightEntry?, LocalDate, Double, Double?) -> Unit,
    onDeleteEntry: (WeightEntry) -> Unit,
    onExportCsv: () -> Unit,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingEntry = null
                    showSheet = true
                },
                text = { Text("Log weight") },
                icon = { Text("+", style = MaterialTheme.typography.headlineSmall) },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                )
            }
            item { SummaryPanel(uiState) }
            item { WeightChart(entriesNewestFirst = uiState.entries) }
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
                items(uiState.entries, key = { it.id }) { entry ->
                    EntryRow(
                        entry = entry,
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

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            EntrySheet(
                entry = editingEntry,
                onSave = { date, weightKg, waistCm ->
                    onSaveEntry(editingEntry, date, weightKg, waistCm)
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
private fun Header(
    entryCount: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Weight Tracker",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "$entryCount logs saved locally",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryPanel(uiState: WeightTrackerUiState) {
    val latest = uiState.trend.latestWeightKg
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Current",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = latest?.let { "${formatOneDecimal(it)} kg" } ?: "-- kg",
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
                    value = uiState.trend.previousDeltaKg?.let { formatDelta(it) } ?: "No trend",
                    modifier = Modifier.weight(1f),
                )
                MetricPill(
                    label = "Waist",
                    value = uiState.trend.latestWaistCm?.let { "${formatOneDecimal(it)} cm" } ?: "Optional",
                    modifier = Modifier.weight(1f),
                )
            }
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
                    TrendCanvas(entriesNewestFirst)
                }
            }
        }
    }
}

@Composable
private fun TrendCanvas(entriesNewestFirst: List<WeightEntry>) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 55f),
        label = "chartProgress",
    )
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val lineColor = MaterialTheme.colorScheme.onSecondaryContainer
    val entries = entriesNewestFirst.sortedBy { it.date }
    val values = entries.map { it.weightKg }
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val range = (max - min).takeIf { it > 0.01 } ?: 1.0

    Canvas(modifier = Modifier.fillMaxSize()) {
        val padding = 28.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val points = entries.mapIndexed { index, entry ->
            val x = padding + chartWidth * (index / (entries.lastIndex).toFloat())
            val normalized = ((entry.weightKg - min) / range).toFloat()
            val y = padding + chartHeight * (1f - normalized)
            Offset(x, y)
        }

        for (i in 0..3) {
            val y = padding + chartHeight * (i / 3f)
            drawLine(
                color = lineColor.copy(alpha = 0.13f),
                start = Offset(padding, y),
                end = Offset(size.width - padding, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val visibleLast = (points.lastIndex * progress).toInt().coerceAtLeast(1)
        val visiblePoints = points.take(visibleLast + 1)
        val path = Path().apply {
            visiblePoints.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        val fillPath = Path().apply {
            addPath(path)
            lineTo(visiblePoints.last().x, size.height - padding)
            lineTo(visiblePoints.first().x, size.height - padding)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primary.copy(alpha = 0.38f), tertiary.copy(alpha = 0.04f)),
            ),
        )
        drawPath(
            path = path,
            color = primary,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
        )
        visiblePoints.forEachIndexed { index, point ->
            val alpha = if (index == visiblePoints.lastIndex) 1f else 0.52f
            drawCircle(color = primary.copy(alpha = alpha), radius = 6.dp.toPx(), center = point)
            drawCircle(color = Color.White.copy(alpha = alpha), radius = 2.5.dp.toPx(), center = point)
        }
    }
}

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
                        text = "${formatOneDecimal(entry.weightKg)} kg",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "${entry.date}${entry.waistCm?.let { " | ${formatOneDecimal(it)} cm waist" } ?: ""}",
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
    onSave: (LocalDate, Double, Double?) -> Unit,
    onCancel: () -> Unit,
) {
    var dateText by remember(entry) { mutableStateOf((entry?.date ?: LocalDate.now()).toString()) }
    var weightText by remember(entry) { mutableStateOf(entry?.weightKg?.let(::formatOneDecimal).orEmpty()) }
    var waistText by remember(entry) { mutableStateOf(entry?.waistCm?.let(::formatOneDecimal).orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

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
            value = dateText,
            onValueChange = { dateText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date") },
            singleLine = true,
            supportingText = { Text("Use YYYY-MM-DD") },
        )
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Weight") },
            suffix = { Text("kg") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        OutlinedTextField(
            value = waistText,
            onValueChange = { waistText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Waist") },
            suffix = { Text("cm") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        val parsedDate = try {
                            LocalDate.parse(dateText.trim())
                        } catch (_: DateTimeParseException) {
                            null
                        }
                        val parsedWeight = weightText.toDoubleOrNull()
                        val parsedWaist = waistText.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                        when {
                            parsedDate == null -> error = "Enter a valid date."
                            parsedWeight == null || parsedWeight <= 0.0 -> error = "Enter a valid weight."
                            waistText.isNotBlank() && (parsedWaist == null || parsedWaist <= 0.0) -> {
                                error = "Enter a valid waist or leave it blank."
                            }
                            else -> onSave(parsedDate, parsedWeight, parsedWaist)
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
}

private fun formatOneDecimal(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun formatDelta(value: Double): String {
    val prefix = if (value > 0) "+" else ""
    return "$prefix${formatOneDecimal(value)} kg"
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
            ),
            onSaveEntry = { _, _, _, _ -> },
            onDeleteEntry = {},
            onExportCsv = {},
            onMessageShown = {},
        )
    }
}
