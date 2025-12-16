package com.audil.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.audil.presentation.history.HistoryViewModel
import com.audil.presentation.history.MeetingItem
import com.audil.ui.components.AudilButton
import com.audil.ui.theme.ElectricBlue
import com.audil.ui.theme.VibrantTeal

@Composable
fun HomeScreen(
    onRecordClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val meetings by viewModel.meetings.collectAsState()
    val recentMeetings = meetings.take(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Back",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // distinct "Start New Recording" button with gradient look logic handled by AudilButton usually, 
        // but let's customize it to match the "Electric Blue" glow if needed.
        // For now, primary color is ElectricBlue so it should match.
        AudilButton(
            text = "Start New Recording",
            onClick = onRecordClick,
            modifier = Modifier.height(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("History")
            }
            androidx.compose.material3.OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Settings")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Meetings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onHistoryClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recentMeetings) { meeting ->
                MeetingItem(meeting = meeting, onClick = { /* Navigate to detail? We need a callback here */ })
                // Note: HomeScreen callback structure in MainActivity might need update to handle meeting click 
                // or we pass a no-op / separate callback. 
                // For now, strictly UI polish, let's keep it simple or redirect to history.
            }
        }
    }
}
