package com.example.fazarproject2.ui.sounds

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundSelectionScreen(
    onBack: () -> Unit,
    viewModel: SoundSelectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("System Sounds", "My Collection")

    val systemSounds by viewModel.systemSounds.collectAsState()
    val myCollection by viewModel.myCollection.collectAsState()
    val alarmSettings by viewModel.alarmSettings.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Take persistable URI permission for reboots
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.handleFilePicked(it, "Custom Sound")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scanSystemSounds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sound Selector") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = { filePickerLauncher.launch(arrayOf("audio/*")) }) {
                    Icon(Icons.Default.Add, contentDescription = "Pick Audio File")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> SystemSoundsTab(
                    sounds = systemSounds,
                    onAdd = { viewModel.addToCollection(it) }
                )

                1 -> MyCollectionTab(
                    sounds = myCollection,
                    activeUri = alarmSettings?.customAudioUri,
                    onSelect = { viewModel.selectAsActive(it.uri, it.title) },
                    onRemove = { viewModel.removeFromCollection(it) }
                )
            }
        }
    }
}

@Composable
fun SystemSoundsTab(
    sounds: List<com.example.fazarproject2.domain.model.AudioFile>,
    onAdd: (com.example.fazarproject2.domain.model.AudioFile) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(sounds) { sound ->
            ListItem(
                headlineContent = { Text(sound.title) },
                supportingContent = { Text("${sound.durationMs / 1000}s") },
                trailingContent = {
                    IconButton(onClick = { onAdd(sound) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add to Collection")
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun MyCollectionTab(
    sounds: List<com.example.fazarproject2.domain.model.AudioFile>,
    activeUri: String?,
    onSelect: (com.example.fazarproject2.domain.model.AudioFile) -> Unit,
    onRemove: (com.example.fazarproject2.domain.model.AudioFile) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(sounds) { sound ->
            val isActive = sound.uri == activeUri
            ListItem(
                modifier = Modifier.clickable { onSelect(sound) },
                headlineContent = {
                    Text(
                        text = sound.title,
                        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Unspecified
                    )
                },
                supportingContent = { Text("${sound.durationMs / 1000}s") },
                leadingContent = {
                    if (isActive) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingContent = {
                    IconButton(onClick = { onRemove(sound) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            )
            HorizontalDivider()
        }
    }
}
