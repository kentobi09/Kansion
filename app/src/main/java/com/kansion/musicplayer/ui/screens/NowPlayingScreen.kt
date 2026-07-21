package com.kansion.musicplayer.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.kansion.musicplayer.Playlist
import com.kansion.musicplayer.R
import com.kansion.musicplayer.Song
import com.kansion.musicplayer.ui.theme.AccentGold
import com.kansion.musicplayer.ui.theme.PrimaryGold
import com.kansion.musicplayer.ui.theme.TextSecondary
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NowPlayingScreen(
    song: Song,
    isPlaying: Boolean,
    progress: Long,
    duration: Long,
    isShuffle: Boolean,
    repeatMode: Int,
    playlists: List<Playlist>,
    playbackSource: String,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onAddSongToPlaylist: (Long, Song) -> Unit,
    onCollapse: () -> Unit,
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showVolumeDialog by remember { mutableStateOf(false) }
    var albumArt by remember(song) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(song) {
        withContext(Dispatchers.IO) {
            albumArt = try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(song.path)
                val art = retriever.embeddedPicture
                retriever.release()
                if (art != null) {
                    BitmapFactory.decodeByteArray(art, 0, art.size)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    // Vinyl Record rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinylRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )
    
    val animatedRotation = if (isPlaying) rotation else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse",
                    tint = TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.now_playing).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = PrimaryGold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = playbackSource,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showVolumeDialog = true }) {
                    Icon(
                        imageVector = if (volume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = TextSecondary
                    )
                }
                IconButton(onClick = { showAddToPlaylistDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = stringResource(id = R.string.add_to_playlist),
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Vinyl Record Disk Representation
        Box(
            modifier = Modifier
                .size(280.dp)
                .rotate(animatedRotation),
            contentAlignment = Alignment.Center
        ) {
            // Draw Vinyl Grooves
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val outerRadius = size.width / 2
                
                // Base black plate
                drawCircle(
                    color = Color(0xFF1E1E1E),
                    radius = outerRadius
                )
                
                // Outer gold highlight ring
                drawCircle(
                    color = PrimaryGold,
                    radius = outerRadius - 2f,
                    style = Stroke(width = 2f)
                )
                
                // Groove lines
                for (r in (outerRadius.toInt() - 20 downTo 60 step 15)) {
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.4f),
                        radius = r.toFloat(),
                        style = Stroke(width = 1f)
                    )
                }
                
                // Center Label
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentGold, PrimaryGold)
                    ),
                    radius = 55f
                )
                
                // Center pin hole
                drawCircle(
                    color = Color(0xFF121110),
                    radius = 12f
                )
            }
            
            if (albumArt != null) {
                Image(
                    bitmap = albumArt!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                )
                // pinhole
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                )
            } else {
                // Custom text on CD label
                Text(
                    text = "kansion".uppercase(),
                    color = Color.Black.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Song Information
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Slider and Progress Indicators
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = progress.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    activeTrackColor = PrimaryGold,
                    inactiveTrackColor = DividerColor(MaterialTheme.colorScheme),
                    thumbColor = PrimaryGold
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(progress),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = onShuffleToggle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = stringResource(id = R.string.shuffle),
                    tint = if (isShuffle) PrimaryGold else TextSecondary
                )
            }
            
            // Previous
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = stringResource(id = R.string.previous),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // Play/Pause Playback Button with circular gold background
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AccentGold, PrimaryGold)
                        )
                    )
                    .clickable(onClick = onPlayPauseToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(id = if (isPlaying) R.string.pause else R.string.play),
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Next
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = stringResource(id = R.string.next),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // Repeat
            IconButton(onClick = onRepeatToggle) {
                val repeatIcon = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                val repeatTint = if (repeatMode != Player.REPEAT_MODE_OFF) PrimaryGold else TextSecondary
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = stringResource(id = R.string.repeat),
                    tint = repeatTint
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Add to Playlist Dialog
    if (showAddToPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.add_to_playlist),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGold
                )
            },
            text = {
                if (playlists.isEmpty()) {
                    Text(text = "Awan ti listaan a nasarakan. Mangaramidka iti baro iti tab a Listaan.", color = TextSecondary)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddSongToPlaylist(playlist.id, song)
                                        showAddToPlaylistDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.QueueMusic, contentDescription = null, tint = PrimaryGold)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = playlist.name, color = MaterialTheme.colorScheme.onBackground)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddToPlaylistDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel), color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Volume Dialog Modal
    if (showVolumeDialog) {
        AlertDialog(
            onDismissRequest = { showVolumeDialog = false },
            title = {
                Text(
                    text = "Denggeg (Volume)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGold
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (volume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                        contentDescription = "Volume Down",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            activeTrackColor = PrimaryGold,
                            inactiveTrackColor = DividerColor(MaterialTheme.colorScheme),
                            thumbColor = PrimaryGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume Up",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showVolumeDialog = false }) {
                    Text(text = "Nalpas (Done)", color = PrimaryGold, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Utility to draw divider color
@Composable
fun DividerColor(scheme: ColorScheme): Color = scheme.outline.copy(alpha = 0.5f)

// Helper function to format duration in ms to MM:SS format
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
