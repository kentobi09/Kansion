package com.kansion.musicplayer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kansion.musicplayer.Playlist
import com.kansion.musicplayer.R
import com.kansion.musicplayer.Song
import com.kansion.musicplayer.ui.theme.AccentGold
import com.kansion.musicplayer.ui.theme.PrimaryGold
import com.kansion.musicplayer.ui.theme.TextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    activePlaylist: Playlist?,
    playlistSongs: List<Song>,
    onSongClick: (Song) -> Unit,
    onRemoveSongFromPlaylist: (Long, Song) -> Unit,
    onBackToBack: () -> Unit = {}, // padding param if any, or just ignore
    onBackToPlaylists: () -> Unit,
    allSongs: List<Song>,
    onAddSongToPlaylist: (Long, Song) -> Unit,
    currentSong: Song?,
    isPlaybackActive: Boolean
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var showAddSongsDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    
    val songsAvailableToAdd = remember(allSongs, playlistSongs) {
        val existingIds = playlistSongs.map { it.id }.toSet()
        allSongs.filter { it.id !in existingIds }
    }
    val selectedSongs = remember { mutableStateListOf<Song>() }

    LaunchedEffect(showAddSongsDialog) {
        if (showAddSongsDialog) {
            selectedSongs.clear()
        }
    }

    if (activePlaylist != null) {
        // Detailed Playlist View
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackToPlaylists) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go Back",
                            tint = PrimaryGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activePlaylist.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Button(
                    onClick = { showAddSongsDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGold,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Inayon ti Kanta", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (playlistSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_songs_found),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(playlistSongs) { song ->
                        val isPlaying = currentSong?.id == song.id
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            isPlaying = isPlaying,
                            isPlaybackActive = isPlaybackActive,
                            trailingIcon = {
                                IconButton(onClick = { onRemoveSongFromPlaylist(activePlaylist.id, song) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(id = R.string.remove_from_playlist),
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    } else {
        // Playlists Overview
        Box(modifier = Modifier.fillMaxSize()) {
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awan ti listaan a nasarakan.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onDelete = { playlistToDelete = playlist }
                        )
                    }
                }
            }

            // Floating Action Button to create playlist
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PrimaryGold,
                contentColor = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 100.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.create_playlist))
            }
        }
    }

    // Dialog for creating playlist
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newPlaylistName = ""
            },
            title = {
                Text(
                    text = stringResource(id = R.string.create_playlist),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGold
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text(text = stringResource(id = R.string.playlist_name), color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGold,
                        unfocusedBorderColor = TextSecondary,
                        focusedLabelColor = PrimaryGold,
                        cursorColor = PrimaryGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = MaterialTheme.colorScheme.background)
                ) {
                    Text(text = stringResource(id = R.string.create))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        newPlaylistName = ""
                    }
                ) {
                    Text(text = stringResource(id = R.string.cancel), color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddSongsDialog) {
        AlertDialog(
            onDismissRequest = { showAddSongsDialog = false },
            title = {
                Text(
                    text = "Dungag ti Kankansion",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGold
                )
            },
            text = {
                if (songsAvailableToAdd.isEmpty()) {
                    Text(text = "Awan ti kanta a mabalin nga inayon.", color = TextSecondary)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(songsAvailableToAdd) { song ->
                            val isSelected = selectedSongs.contains(song)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedSongs.remove(song) else selectedSongs.add(song)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked == true) selectedSongs.add(song) else selectedSongs.remove(song)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryGold,
                                        uncheckedColor = TextSecondary,
                                        checkmarkColor = MaterialTheme.colorScheme.background
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = song.title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                                    Text(text = song.artist, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            },
            confirmButton = {
                if (songsAvailableToAdd.isNotEmpty()) {
                    Button(
                        onClick = {
                            selectedSongs.forEach { song ->
                                onAddSongToPlaylist(activePlaylist!!.id, song)
                            }
                            showAddSongsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = MaterialTheme.colorScheme.background)
                    ) {
                        Text(text = "Inayon")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSongsDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel), color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (playlistToDelete != null) {
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = {
                Text(
                    text = "Siguradoka?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGold
                )
            },
            text = {
                Text(
                    text = "Pukawen daytoy a listaan: \"${playlistToDelete?.name}\"? Saanmon a maisubli daytoy.",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        playlistToDelete?.let { onDeletePlaylist(it.id) }
                        playlistToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = "Pukawen")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text(text = "Ibaba", color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon holder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AccentGold, PrimaryGold)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${playlist.songCount} kanta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_playlist),
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    isPlaying: Boolean = false,
    isPlaybackActive: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isPlaying) PrimaryGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                EqualizerAnimation(
                    modifier = Modifier.size(16.dp),
                    color = PrimaryGold,
                    isAnimating = isPlaybackActive
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = PrimaryGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isPlaying) PrimaryGold else MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1
            )
        }

        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}

@Composable
fun EqualizerAnimation(
    modifier: Modifier = Modifier,
    color: Color = PrimaryGold,
    isAnimating: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    
    val height1Spec = if (isAnimating) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 450, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar1"
        )
    } else {
        remember { mutableStateOf(0.5f) }
    }
    
    val height2Spec = if (isAnimating) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar2"
        )
    } else {
        remember { mutableStateOf(0.7f) }
    }
    
    val height3Spec = if (isAnimating) {
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar3"
        )
    } else {
        remember { mutableStateOf(0.4f) }
    }

    val height1 = height1Spec.value
    val height2 = height2Spec.value
    val height3 = height3Spec.value

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val barWidth = 3.dp
        Box(modifier = Modifier.width(barWidth).fillMaxHeight(height1).background(color))
        Box(modifier = Modifier.width(barWidth).fillMaxHeight(height2).background(color))
        Box(modifier = Modifier.width(barWidth).fillMaxHeight(height3).background(color))
    }
}
