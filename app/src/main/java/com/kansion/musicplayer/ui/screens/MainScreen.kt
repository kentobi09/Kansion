package com.kansion.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kansion.musicplayer.Playlist
import com.kansion.musicplayer.R
import com.kansion.musicplayer.Song
import com.kansion.musicplayer.SortOrder
import com.kansion.musicplayer.ui.theme.AccentGold
import com.kansion.musicplayer.ui.theme.PrimaryGold
import com.kansion.musicplayer.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    queue: List<Song>,
    currentSongIndex: Int,
    isPlaying: Boolean,
    activePlaylist: Playlist?,
    playlistSongs: List<Song>,
    onSongPlay: (Song) -> Unit,
    onQueueSelect: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onRemoveSongFromPlaylist: (Long, Song) -> Unit,
    onBackToPlaylists: () -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onAddSongToPlaylist: (Long, Song) -> Unit,
    onAddSongToQueue: (Song) -> Unit,
    onPlayPauseToggle: () -> Unit,
    onExpandPlayer: () -> Unit,
    onQueueMove: (Int, Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val selectedTab = pagerState.currentPage
    var searchQuery by remember { mutableStateOf("") }
    var showSortDialog by remember { mutableStateOf(false) }

    val tabTitles = listOf(
        stringResource(id = R.string.title_songs),
        stringResource(id = R.string.title_playlists),
        stringResource(id = R.string.title_queue)
    )

    // Filter songs based on search query
    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val currentSong = if (queue.isNotEmpty() && currentSongIndex >= 0 && currentSongIndex < queue.size) {
        queue[currentSongIndex]
    } else {
        null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSettingsClick)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Kansion Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "kansion",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = 1.sp
                        ),
                        color = PrimaryGold
                    )
                }
                
                if (selectedTab == 0) {
                    IconButton(
                        onClick = { showSortDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = stringResource(id = R.string.sort_by),
                            tint = PrimaryGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }

            // Tab Row
            CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryGold
                        )
                    },
                    divider = {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                                // Reset playlist drilldown when clicking playlist tab
                                if (index == 1) {
                                    onBackToPlaylists()
                                }
                            },
                             text = {
                                 Text(
                                     text = title,
                                     maxLines = 1,
                                     overflow = TextOverflow.Ellipsis,
                                     style = MaterialTheme.typography.bodyMedium.copy(
                                         fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                     ),
                                     color = if (selectedTab == index) PrimaryGold else TextSecondary
                                 )
                             }
                        )
                    }
                }
            }

            // Tab Content
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                    0 -> { // Songs List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            item {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text(text = stringResource(id = R.string.search_hint), color = TextSecondary) },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary)
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    maxLines = 1,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryGold,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                        cursorColor = PrimaryGold,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            if (filteredSongs.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.no_songs_found),
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            } else {
                                items(filteredSongs) { song ->
                                    var showMenu by remember { mutableStateOf(false) }
                                    var showPlaylistPicker by remember { mutableStateOf(false) }
                                    val isSongPlaying = currentSong?.id == song.id

                                    SongListItem(
                                        song = song,
                                        onClick = { onSongPlay(song) },
                                        isPlaying = isSongPlaying,
                                        isPlaybackActive = isPlaying,
                                        trailingIcon = {
                                            Box {
                                                IconButton(onClick = { showMenu = true }) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "Options",
                                                        tint = TextSecondary
                                                    )
                                                }
                                                
                                                DropdownMenu(
                                                    expanded = showMenu,
                                                    onDismissRequest = { showMenu = false },
                                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text(text = "Dungag ti uray (Play Next)", color = MaterialTheme.colorScheme.onBackground) },
                                                        onClick = {
                                                            onAddSongToQueue(song)
                                                            showMenu = false
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text(text = stringResource(id = R.string.add_to_playlist), color = MaterialTheme.colorScheme.onBackground) },
                                                        onClick = {
                                                            showPlaylistPicker = true
                                                            showMenu = false
                                                        }
                                                    )
                                                }
                                            }

                                            // Playlist picker inside list item options
                                            if (showPlaylistPicker) {
                                                AlertDialog(
                                                    onDismissRequest = { showPlaylistPicker = false },
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
                                                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                                                items(playlists) { playlist ->
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clickable {
                                                                                onAddSongToPlaylist(playlist.id, song)
                                                                                showPlaylistPicker = false
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
                                                        TextButton(onClick = { showPlaylistPicker = false }) {
                                                            Text(text = stringResource(id = R.string.cancel), color = TextSecondary)
                                                        }
                                                    },
                                                    containerColor = MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    1 -> { // Playlists
                        PlaylistsScreen(
                            playlists = playlists,
                            onCreatePlaylist = onCreatePlaylist,
                            onDeletePlaylist = onDeletePlaylist,
                            onPlaylistClick = onPlaylistClick,
                            activePlaylist = activePlaylist,
                            playlistSongs = playlistSongs,
                            onSongClick = onSongPlay,
                            onRemoveSongFromPlaylist = onRemoveSongFromPlaylist,
                            onBackToPlaylists = onBackToPlaylists,
                            allSongs = songs,
                            onAddSongToPlaylist = onAddSongToPlaylist,
                            currentSong = currentSong,
                            isPlaybackActive = isPlaying
                        )
                    }
                    2 -> { // Queue
                        QueueScreen(
                            queue = queue,
                            currentSongIndex = currentSongIndex,
                            onSongSelect = onQueueSelect,
                            onRemoveSong = onRemoveFromQueue,
                            onClearQueue = onClearQueue,
                            onQueueMove = onQueueMove,
                            isPlaybackActive = isPlaying
                        )
                    }
                }
            }
            }
        }

        // Mini Bottom Player Bar
        if (currentSong != null) {
            Card(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable(onClick = onExpandPlayer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Song Details
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
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPrevious) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = stringResource(id = R.string.previous),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(onClick = onPlayPauseToggle) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = stringResource(id = if (isPlaying) R.string.pause else R.string.play),
                                tint = PrimaryGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(onClick = onNext) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = stringResource(id = R.string.next),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Sorting Selection Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.sort_by),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGold
                )
            },
            text = {
                Column {
                    listOf(
                        SortOrder.TITLE_ASC to stringResource(id = R.string.sort_title) + " (A-Z)",
                        SortOrder.TITLE_DESC to stringResource(id = R.string.sort_title) + " (Z-A)",
                        SortOrder.ARTIST_ASC to stringResource(id = R.string.sort_artist),
                        SortOrder.DATE_ADDED_DESC to stringResource(id = R.string.sort_date),
                        SortOrder.DURATION_DESC to "Pinagpaut (Duration)"
                    ).forEach { (order, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSortChange(order)
                                    showSortDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel), color = TextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Transparent

    @Composable
    override fun rippleAlpha() = RippleAlpha(0f, 0f, 0f, 0f)
}
