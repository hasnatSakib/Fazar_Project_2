package com.example.fazarproject2.ui.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fazarproject2.R
import com.example.fazarproject2.util.permission.PermissionUtils

@Composable
fun PermissionGate(
    viewModel: PermissionViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Check for Exact Alarm permission separately as it's a Special App Access
    var hasExactAlarmPermission by remember {
        mutableStateOf(
            PermissionUtils.canScheduleExactAlarms(
                context
            )
        )
    }

    // Refresh permissions when coming back to the app
    DisposableEffect(Unit) {
        viewModel.checkPermissions()
        hasExactAlarmPermission = PermissionUtils.canScheduleExactAlarms(context)
        onDispose {}
    }

    if (uiState.isAllRequiredGranted && uiState.isStorageGranted && hasExactAlarmPermission) {
        content()
    } else if (!hasExactAlarmPermission) {
        ExactAlarmRationale(onGrantClick = { PermissionUtils.openExactAlarmSettings(context) })
    } else {
        OnboardingPermissionScreen(
            onGrantClick = { /* Launcher is defined inside the screen */ }
        )
    }
}

@Composable
fun OnboardingPermissionScreen(
    onGrantClick: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.checkPermissions()
        val allGranted = result.values.all { it }
        if (!allGranted) {
            showRationale = true
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permissions Required") },
            text = { Text("Location access is needed to calculate the exact sunrise time, Notifications are required to alert you, and Music/Audio access is needed to select and play your custom alarm sounds.") },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    PermissionUtils.openAppSettings(context)
                }) {
                    Text("Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ruku),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to Sunrise Alarm",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To provide accurate sunrise-based alarms, we need a few permissions:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        PermissionItem(
            icon = Icons.Default.Place,
            title = "Location",
            description = "Used to fetch the sunrise time for your current coordinates."
        )
        Spacer(modifier = Modifier.height(16.dp))
        PermissionItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Necessary to ring the alarm and show reminders."
        )
        Spacer(modifier = Modifier.height(16.dp))
        PermissionItem(
            icon = Icons.Default.List,
            title = "Music & Audio",
            description = "Used to browse and play your custom alarm sounds."
        )

        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {
                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                launcher.launch(permissions.toTypedArray())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permissions")
        }
    }
}

@Composable
fun ExactAlarmRationale(onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Exact Alarms Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To ensure your alarm rings exactly at the calculated sunrise offset, Android requires a special 'Exact Alarm' permission. Please enable it in the next screen.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onGrantClick) {
            Text("Enable in Settings")
        }
    }
}

@Composable
fun PermissionItem(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
