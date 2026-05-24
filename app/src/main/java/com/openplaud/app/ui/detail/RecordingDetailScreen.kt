package com.openplaud.app.ui.detail

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.openplaud.app.LocalApi
import com.openplaud.app.LocalPrefs
import com.openplaud.app.data.model.V1RecordingDetail
import com.openplaud.app.data.model.V1Summary
import com.openplaud.app.data.model.V1Transcript
import com.openplaud.app.ui.list.formatDuration
import com.openplaud.app.ui.theme.PlaudColors
import com.openplaud.app.ui.theme.PlaudTheme
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingDetailScreen(recordingId: String, onBack: () -> Unit) {
    val repo = com.openplaud.app.LocalRepo.current
    val prefs = LocalPrefs.current
    val api = LocalApi.current
    val viewModel: RecordingDetailViewModel = viewModel(factory = RecordingDetailViewModelFactory(repo, prefs))
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(recordingId) { viewModel.loadRecording(recordingId) }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var playerInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(state.audioUrl) {
        if (state.audioUrl.isNotEmpty() && !playerInitialized) {
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val apiKey = runBlocking { prefs.getApiKey() } ?: ""
                setMediaItem(MediaItem.fromUri(Uri.parse(state.audioUrl)))
                prepare()
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        viewModel.onPlaybackStateChanged(isPlaying, currentPosition, duration.takeIf { it > 0 } ?: 0)
                    }
                })
            }
            playerInitialized = true
        }
    }

    DisposableEffect(Unit) { onDispose { exoPlayer?.release() } }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(state.recording?.title ?: "Recording", fontWeight = FontWeight.Bold, color = PlaudColors.textPrimary, maxLines = 1) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PlaudColors.textPrimary) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PlaudColors.background)
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PlaudColors.primary) }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Error, null, tint = PlaudColors.error, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp)); Text(state.error!!, color = PlaudColors.error)
                }
            }
            state.recording != null -> {
                val r = state.recording!!
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // Player
                    Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = PlaudColors.surface)) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { if (state.isPlaying) exoPlayer?.pause() else exoPlayer?.play() }, modifier = Modifier.size(64.dp)) {
                                Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, if (state.isPlaying) "Pause" else "Play", tint = PlaudColors.primary, modifier = Modifier.size(48.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(formatDuration(state.playbackPosition), fontSize = 12.sp, color = PlaudColors.textSecondary)
                                Text(formatDuration(state.playbackDuration), fontSize = 12.sp, color = PlaudColors.textSecondary)
                            }
                        }
                    }
                    // Info
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text("Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PlaudColors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) { Text("Duration", fontSize = 13.sp, color = PlaudColors.textSecondary, modifier = Modifier.width(80.dp)); Text(formatDuration(r.durationMs), fontSize = 13.sp, color = PlaudColors.textPrimary) }
                    }
                    // Transcript
                    if (r.transcript != null) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, null, tint = PlaudColors.success, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                                Text("Transcript", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PlaudColors.textPrimary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = PlaudColors.surface)) {
                                Text(r.transcript.text, modifier = Modifier.padding(16.dp), fontSize = 14.sp, color = PlaudColors.textPrimary, lineHeight = 22.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
