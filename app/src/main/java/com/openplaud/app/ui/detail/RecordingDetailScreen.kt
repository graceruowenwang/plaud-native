package com.openplaud.app.ui.detail

import android.net.Uri
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.openplaud.app.data.model.V1RecordingDetail
import com.openplaud.app.data.model.V1Summary
import com.openplaud.app.data.model.V1Transcript
import com.openplaud.app.ui.list.formatDuration
import com.openplaud.app.ui.theme.PlaudColors
import com.openplaud.app.ui.theme.PlaudTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingDetailScreen(
    recordingId: String,
    onBack: () -> Unit,
    viewModel: RecordingDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(recordingId) {
        viewModel.loadRecording(recordingId)
    }

    // Audio player
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var playerInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(state.audioUrl) {
        if (state.audioUrl.isNotEmpty() && !playerInitialized) {
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val uri = Uri.parse(state.audioUrl)
                val headers = mapOf(
                    "Authorization" to "Bearer ${getApiKey(context)}"
                )
                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .setCustomCacheKey(state.audioUrl)
                    .build()
                setMediaItem(mediaItem)
                prepare()
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        viewModel.onPlaybackStateChanged(
                            isPlaying,
                            currentPosition,
                            duration.takeIf { it > 0 } ?: 0
                        )
                    }
                })
            }
            playerInitialized = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer?.release() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    state.recording?.title ?: "Recording",
                    fontWeight = FontWeight.Bold,
                    color = PlaudColors.textPrimary,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = PlaudColors.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PlaudColors.background
            )
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PlaudColors.primary)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, null, tint = PlaudColors.error, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(state.error!!, color = PlaudColors.error)
                    }
                }
            }

            state.recording != null -> {
                val recording = state.recording!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Player section
                    PlayerSection(
                        isPlaying = state.isPlaying,
                        position = state.playbackPosition,
                        duration = state.playbackDuration,
                        onPlayPause = {
                            if (state.isPlaying) exoPlayer?.pause() else exoPlayer?.play()
                        },
                        onSeek = { pos ->
                            exoPlayer?.seekTo(pos)
                        }
                    )

                    // Info section
                    InfoSection(recording)

                    // Transcript section
                    if (recording.transcript != null) {
                        TranscriptSection(recording.transcript)
                    }

                    // Summary section
                    if (recording.summary != null) {
                        SummarySection(recording.summary)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun PlayerSection(
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = PlaudColors.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Play button
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = PlaudColors.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatDuration(position),
                    fontSize = 12.sp,
                    color = PlaudColors.textSecondary
                )
                Text(
                    formatDuration(duration),
                    fontSize = 12.sp,
                    color = PlaudColors.textSecondary
                )
            }

            Spacer(Modifier.height(4.dp))

            Slider(
                value = if (duration > 0) position.toFloat() else 0f,
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = PlaudColors.primary,
                    activeTrackColor = PlaudColors.primary,
                    inactiveTrackColor = PlaudTheme.colors.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun InfoSection(recording: V1RecordingDetail) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Details",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = PlaudColors.textPrimary
        )
        Spacer(Modifier.height(8.dp))

        InfoRow("Duration", formatDuration(recording.durationMs))
        InfoRow("Size", formatFileSize(recording.filesizeBytes))
        InfoRow("Recorded", recording.recordedAt.take(16).replace("T", " "))
        recording.device?.let {
            InfoRow("Device", "${it.model ?: "Plaud"} (${it.serialNumber.takeLast(6)})")
        }
        InfoRow("Status", buildString {
            append(if (recording.hasTranscription) "✓ Transcribed" else "○ No transcript")
            append(" | ")
            append(if (recording.hasSummary) "✓ Summary" else "○ No summary")
        })
    }
}

@Composable
fun TranscriptSection(transcript: V1Transcript) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, null, tint = PlaudColors.success, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Transcript",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PlaudColors.textPrimary
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${transcript.provider} · ${transcript.model}",
                fontSize = 11.sp,
                color = PlaudColors.textSecondary
            )
        }

        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = PlaudColors.surface)
        ) {
            Text(
                transcript.text,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = PlaudColors.textPrimary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun SummarySection(summary: V1Summary) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = PlaudColors.accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "AI Summary",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PlaudColors.textPrimary
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${summary.provider} · ${summary.model}",
                fontSize = 11.sp,
                color = PlaudColors.textSecondary
            )
        }

        Spacer(Modifier.height(8.dp))

        // Summary text
        if (!summary.text.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PlaudColors.surface)
            ) {
                Text(
                    summary.text,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = PlaudColors.textPrimary,
                    lineHeight = 22.sp
                )
            }
        }

        // Key points
        if (!summary.keyPoints.isNullOrEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Key Points",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = PlaudColors.textSecondary
            )
            summary.keyPoints.forEachIndexed { i, point ->
                Row(modifier = Modifier.padding(top = 4.dp, start = 8.dp)) {
                    Text("•", color = PlaudColors.accent, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(point, fontSize = 13.sp, color = PlaudColors.textPrimary)
                }
            }
        }

        // Action items
        if (!summary.actionItems.isNullOrEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Action Items",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = PlaudColors.textSecondary
            )
            summary.actionItems.forEachIndexed { i, item ->
                Row(modifier = Modifier.padding(top = 4.dp, start = 8.dp)) {
                    Text("☐", color = PlaudColors.warning, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(item, fontSize = 13.sp, color = PlaudColors.textPrimary)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = PlaudColors.textSecondary,
            modifier = Modifier.width(80.dp)
        )
        Text(value, fontSize = 13.sp, color = PlaudColors.textPrimary)
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
        else -> "$bytes B"
    }
}

// Temporary helper until DI is wired
private fun getApiKey(context: android.content.Context): String {
    // This is a temporary workaround - will be properly injected
    return ""
}
