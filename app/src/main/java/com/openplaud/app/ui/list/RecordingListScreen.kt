package com.openplaud.app.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openplaud.app.LocalRepo
import com.openplaud.app.data.model.V1Recording
import com.openplaud.app.ui.theme.PlaudColors
import com.openplaud.app.ui.theme.PlaudTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingListScreen(
    onRecordingClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val repo = LocalRepo.current
    val viewModel: RecordingListViewModel = viewModel(factory = RecordingListViewModelFactory(repo))
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.recordings.size - 3 && state.hasMore && !state.isLoading
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.loadMore() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Plaud", fontWeight = FontWeight.Bold, color = PlaudColors.textPrimary) },
            actions = {
                IconButton(onClick = { viewModel.syncPlaud() }) {
                    if (state.isSyncing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PlaudColors.accent, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Sync, "Sync", tint = PlaudColors.accent)
                }
                IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh", tint = PlaudColors.textSecondary) }
                IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, "Settings", tint = PlaudColors.textSecondary) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PlaudColors.background)
        )

        OutlinedTextField(
            value = state.searchQuery, onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search recordings...", color = PlaudColors.textSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PlaudColors.textSecondary) },
            trailingIcon = { if (state.searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.onSearchQueryChange("") }) { Icon(Icons.Default.Clear, null, tint = PlaudColors.textSecondary) } },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PlaudColors.textPrimary, unfocusedTextColor = PlaudColors.textPrimary, focusedBorderColor = PlaudColors.primary, unfocusedBorderColor = PlaudTheme.colors.surfaceVariant, focusedContainerColor = PlaudColors.surface, unfocusedContainerColor = PlaudColors.surface)
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PlaudColors.primary) }
            state.error != null && state.recordings.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, null, tint = PlaudColors.error, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp)); Text(state.error!!, color = PlaudColors.error, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp)); Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }
            state.recordings.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MicOff, null, tint = PlaudColors.textSecondary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp)); Text("No recordings yet", color = PlaudColors.textSecondary, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp)); Text("Tap Sync to pull from Plaud", color = PlaudColors.textSecondary, fontSize = 13.sp)
                }
            }
            else -> LazyColumn(state = listState, contentPadding = PaddingValues(vertical = 8.dp)) {
                items(state.recordings, key = { it.id }) { recording ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onRecordingClick(recording.id) },
                        colors = CardDefaults.cardColors(containerColor = PlaudColors.surface)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, null, tint = if (recording.hasTranscription) PlaudColors.success else PlaudColors.textSecondary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(recording.title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = PlaudColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(formatDuration(recording.durationMs), fontSize = 13.sp, color = PlaudColors.textSecondary)
                                    if (recording.hasTranscription) {
                                        Spacer(Modifier.width(8.dp)); Icon(Icons.Default.Description, null, tint = PlaudColors.success, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(2.dp)); Text("Transcribed", fontSize = 12.sp, color = PlaudColors.success)
                                    }
                                }
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = PlaudColors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val t = ms / 1000
    return if (t / 3600 > 0) "%d:%02d:%02d".format(t / 3600, (t % 3600) / 60, t % 60)
    else "%d:%02d".format(t / 60, t % 60)
}
