package com.example.fazarproject2.ui.sunrise

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.fazarproject2.domain.model.SunriseResults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunriseDetailsScreen(
    results: SunriseResults,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sunrise Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            InfoCard(title = "General Info") {
                DetailRow(Icons.Default.CalendarMonth, "Date", results.date ?: "N/A")
                DetailRow(Icons.Default.LocationOn, "Timezone", results.timezone ?: "N/A")
                DetailRow(Icons.Default.Schedule, "UTC Offset", "${results.utcOffset ?: "N/A"}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(title = "Sun Events") {
                DetailRow(Icons.Default.WbSunny, "Sunrise", results.sunrise ?: "N/A")
                DetailRow(Icons.Default.LightMode, "Sunset", results.sunset ?: "N/A")
                DetailRow(Icons.Default.AccessTime, "Day Length", results.dayLength ?: "N/A")
                DetailRow(Icons.Default.WbSunny, "Solar Noon", results.solarNoon ?: "N/A")
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(title = "Twilight & Light") {
                DetailRow(Icons.Default.WbTwilight, "First Light", results.firstLight ?: "N/A")
                DetailRow(Icons.Default.WbTwilight, "Last Light", results.lastLight ?: "N/A")
                DetailRow(Icons.Default.WbTwilight, "Dawn", results.dawn ?: "N/A")
                DetailRow(Icons.Default.WbTwilight, "Dusk", results.dusk ?: "N/A")
                DetailRow(Icons.Default.LightMode, "Golden Hour", results.goldenHour ?: "N/A")
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
