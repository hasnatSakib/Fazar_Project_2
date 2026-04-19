package com.example.fazarproject2.ui.dashboard

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fazarproject2.domain.model.SunriseResults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSoundSelector: () -> Unit,
    onNavigateToSunriseDetails: (SunriseResults) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.alarmSettings.collectAsState()
    val sunriseResults by viewModel.latestSunriseResults.collectAsState()

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DashboardViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var showPermissionRationale by remember { mutableStateOf(false) }
    var permissionMessage by remember { mutableStateOf("") }

    val permissionsToRequest = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.refreshSunriseAndSchedule()
        } else {
            permissionMessage =
                "Location and Notification permissions are required for the alarm to work accurately."
            showPermissionRationale = true
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Permissions Required") },
            text = { Text(permissionMessage) },
            confirmButton = {
                Button(onClick = {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                }) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sunrise Alarm") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            settings?.let { alarm ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        sunriseResults?.let { onNavigateToSunriseDetails(it) }
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (alarm.isEnabled) "Alarm is ON" else "Alarm is OFF",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Next Sunrise: ${alarm.nextSunriseTime ?: "Unknown"}")
                        Text("Triggering at offset: ${alarm.offsetMinutes} mins before")

                        if (sunriseResults != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Click to see more details",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Alarm", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = { viewModel.toggleAlarm(it) }
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Wake up offset",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${alarm.offsetMinutes} mins before",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Local state for smooth sliding
                        var sliderPosition by remember(alarm.offsetMinutes) {
                            mutableFloatStateOf(alarm.offsetMinutes.toFloat())
                        }

                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            onValueChangeFinished = {
                                viewModel.updateOffset(sliderPosition.toInt())
                            },
                            valueRange = 0f..120f,
                            steps = 23 // Steps every 5 minutes (0, 5, 10... 120)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0m", style = MaterialTheme.typography.labelSmall)
                            Text("60m", style = MaterialTheme.typography.labelSmall)
                            Text("120m", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Text(
                    "Alarm Sound",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = onNavigateToSoundSelector,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(alarm.customAudioFileName ?: "Default (fazar_alarm_sound)")
                }
            } ?: CircularProgressIndicator()
        }
    }
}
