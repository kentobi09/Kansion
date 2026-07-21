package com.kansion.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.kansion.musicplayer.ui.screens.MainScreen
import com.kansion.musicplayer.ui.screens.NowPlayingScreen
import com.kansion.musicplayer.ui.screens.PermissionScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kansion.musicplayer.ui.theme.PrimaryGold
import com.kansion.musicplayer.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.kansion.musicplayer.ui.theme.KansionTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: MusicRepository
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController by mutableStateOf<MediaController?>(null)

    // State holding arrays
    private var songs by mutableStateOf<List<Song>>(emptyList())
    private var playlists by mutableStateOf<List<Playlist>>(emptyList())
    private var queue by mutableStateOf<List<Song>>(emptyList())
    private var currentSongIndex by mutableIntStateOf(-1)
    private var isPlayingState by mutableStateOf(false)
    private var isShuffleState by mutableStateOf(false)
    private var repeatModeState by mutableIntStateOf(Player.REPEAT_MODE_OFF)
    private var isPlayingFromQueue = false
    private var playerVolume by mutableFloatStateOf(1f)
    
    // Detailed playlist view states
    private var activePlaylist by mutableStateOf<Playlist?>(null)
    private var playlistSongs by mutableStateOf<List<Song>>(emptyList())

    // UI Expansion State
    private var isPlayerExpanded by mutableStateOf(false)
    private var playbackProgress by mutableLongStateOf(0L)
    private var songDuration by mutableLongStateOf(0L)

    private var progressJob: Job? = null
    private var hasStoragePermission by mutableStateOf(false)

    private var isDarkTheme by mutableStateOf(true)
    private var showSettingsDialog by mutableStateOf(false)
    private var sleepTimerMinutes by mutableIntStateOf(0)
    private var sleepTimerRemainingSeconds by mutableIntStateOf(0)
    private var sleepTimerJob: Job? = null

    // Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        hasStoragePermission = readGranted
        if (readGranted) {
            loadMusicData()
        } else {
            Toast.makeText(this, "Masapul ti permiso ti storage tapno mapatugtog ti kanta.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = MusicRepository(applicationContext)
        
        checkPermissions()

        val prefs = getSharedPreferences("kansion_prefs", MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean("dark_theme", true)
        
        setContent {
            KansionTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!hasStoragePermission) {
                        PermissionScreen(onGrantPermissionClick = { askForPermissions() })
                    } else {
                        val currentSong = if (queue.isNotEmpty() && currentSongIndex >= 0 && currentSongIndex < queue.size) {
                            queue[currentSongIndex]
                        } else {
                            null
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            MainScreen(
                                songs = songs,
                                playlists = playlists,
                                queue = queue,
                                currentSongIndex = currentSongIndex,
                                isPlaying = isPlayingState,
                                activePlaylist = activePlaylist,
                                playlistSongs = playlistSongs,
                                onSongPlay = { song -> playSongNow(song) },
                                onQueueSelect = { index -> selectQueueIndex(index) },
                                onRemoveFromQueue = { index -> removeFromQueue(index) },
                                onClearQueue = { clearQueue() },
                                onCreatePlaylist = { name -> createPlaylist(name) },
                                onDeletePlaylist = { id -> deletePlaylist(id) },
                                onPlaylistClick = { playlist -> loadPlaylistSongs(playlist) },
                                onRemoveSongFromPlaylist = { playlistId, song -> removeSongFromPlaylist(playlistId, song) },
                                onBackToPlaylists = { activePlaylist = null },
                                onSortChange = { order -> loadSongsSorted(order) },
                                onAddSongToPlaylist = { playlistId, song -> addSongToPlaylist(playlistId, song) },
                                onAddSongToQueue = { song -> addSongToQueue(song) },
                                onPlayPauseToggle = { togglePlayPause() },
                                onExpandPlayer = { isPlayerExpanded = true },
                                onQueueMove = { from, to -> moveQueueItem(from, to) },
                                onNext = { playNext() },
                                onPrevious = { playPrevious() },
                                onSettingsClick = { showSettingsDialog = true }
                            )

                            AnimatedVisibility(
                                visible = isPlayerExpanded && currentSong != null,
                                enter = slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                ),
                                exit = slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                )
                            ) {
                                NowPlayingScreen(
                                    song = currentSong!!,
                                    isPlaying = isPlayingState,
                                    progress = playbackProgress,
                                    duration = songDuration,
                                    isShuffle = isShuffleState,
                                    repeatMode = repeatModeState,
                                    playlists = playlists,
                                    playbackSource = when {
                                        isPlayingFromQueue -> "Ur-uray"
                                        activePlaylist != null -> "Listaan: ${activePlaylist?.name}"
                                        else -> "Kankansion"
                                    },
                                    onPlayPauseToggle = { togglePlayPause() },
                                    onNext = { playNext() },
                                    onPrevious = { playPrevious() },
                                    onSeek = { seekTo(it) },
                                    onShuffleToggle = { toggleShuffle() },
                                    onRepeatToggle = { toggleRepeat() },
                                    onAddSongToPlaylist = { playlistId, song -> addSongToPlaylist(playlistId, song) },
                                    onCollapse = { isPlayerExpanded = false },
                                    volume = playerVolume,
                                    onVolumeChange = { setVolume(it) }
                                )
                            }

                            if (showSettingsDialog) {
                                AlertDialog(
                                    onDismissRequest = { showSettingsDialog = false },
                                    title = {
                                        Text(
                                            text = "Dagiti Setting",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryGold
                                        )
                                    },
                                    text = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Dark Mode Toggle
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = "Rupan Kansion (App Theme)", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                                                    Text(
                                                        text = if (isDarkTheme) "Dark Mode (Ita)" else "Light Mode (Ibaba)",
                                                        color = TextSecondary,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                                Switch(
                                                    checked = isDarkTheme,
                                                    onCheckedChange = { toggleDarkTheme() },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = PrimaryGold,
                                                        checkedTrackColor = PrimaryGold.copy(alpha = 0.5f)
                                                    )
                                                )
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                            // Sleep Timer
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(text = "Oras ti Turog (Sleep Timer)", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    listOf(0, 15, 30, 45, 60).forEach { mins ->
                                                        val selected = sleepTimerMinutes == mins
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (selected) PrimaryGold else MaterialTheme.colorScheme.surfaceVariant)
                                                                .clickable { startSleepTimer(mins) }
                                                                .padding(vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = if (mins == 0) "Off" else "${mins}m",
                                                                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (sleepTimerMinutes > 0) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    val mins = sleepTimerRemainingSeconds / 60
                                                    val secs = sleepTimerRemainingSeconds % 60
                                                    Text(
                                                        text = String.format("Mabati nga oras: %02d:%02d", mins, secs),
                                                        color = PrimaryGold,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                            // Rescan Library
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        rescanLibrary()
                                                        showSettingsDialog = false
                                                    }
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PrimaryGold)
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column {
                                                    Text(text = "Sukiren manen dagiti kansion (Rescan)", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                                                    Text(text = "Sapulen dagiti baro a kanta iti storage", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                            // Reset App Data
                                            var showResetConfirm by remember { mutableStateOf(false) }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showResetConfirm = true }
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column {
                                                    Text(text = "Pukawen ti Data (Reset Data)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                                                    Text(text = "Pukawen amin a listaan ken uray", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }

                                            if (showResetConfirm) {
                                                AlertDialog(
                                                    onDismissRequest = { showResetConfirm = false },
                                                    title = { Text(text = "Siguradoka?", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                                                    text = { Text(text = "Pukawen amin a playlist ken queue? Saanmon a maisubli daytoy.", color = MaterialTheme.colorScheme.onSurface) },
                                                    confirmButton = {
                                                        Button(
                                                            onClick = {
                                                                resetAppData()
                                                                showResetConfirm = false
                                                                showSettingsDialog = false
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                        ) {
                                                            Text(text = "Pukawen")
                                                        }
                                                    },
                                                    dismissButton = {
                                                        TextButton(onClick = { showResetConfirm = false }) {
                                                            Text(text = "Ibaba", color = TextSecondary)
                                                        }
                                                    }
                                                )
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                            // About
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(text = "Maipanggep ken Kansion", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_logo),
                                                        contentDescription = "Kansion Logo",
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(text = "Kansion Offline Player v2.1", color = PrimaryGold, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                        Text(
                                                            text = "Maysa a napardas nga offline player para kadagiti kansionyo iti Ilokano.",
                                                            color = TextSecondary,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                val uriHandler = LocalUriHandler.current
                                                Text(
                                                    text = "Ti Developer (GitHub): @kentobi09",
                                                    color = PrimaryGold,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        textDecoration = TextDecoration.Underline
                                                    ),
                                                    modifier = Modifier.clickable {
                                                        uriHandler.openUri("https://github.com/kentobi09/")
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showSettingsDialog = false }) {
                                            Text(text = "Nalpas (Done)", color = PrimaryGold, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onStop() {
        super.onStop()
        mediaController?.let {
            it.removeListener(playerListener)
            MediaController.releaseFuture(controllerFuture!!)
            mediaController = null
        }
        stopProgressUpdates()
    }

    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        hasStoragePermission = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission) {
            loadMusicData()
        }
    }

    private fun askForPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissionLauncher.launch(permissions)
    }

    private fun loadMusicData() {
        lifecycleScope.launch {
            songs = repository.getAllSongs()
            playlists = repository.getPlaylists()
            
            // Restore saved queue from database if exists
            val saved = repository.getSavedQueue(songs)
            if (saved.isNotEmpty()) {
                queue = saved
                setupQueueInController()
            }
        }
    }

    private fun loadSongsSorted(order: SortOrder) {
        lifecycleScope.launch {
            songs = repository.getAllSongs(order)
        }
    }

    private fun initializeController() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get()
                mediaController = controller
                controller?.addListener(playerListener)
                if (controller != null && controller.mediaItemCount == 0 && queue.isNotEmpty()) {
                    setupQueueInController()
                }
                updateControllerStates()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateControllerStates()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val controller = mediaController ?: return
            if (playbackState == Player.STATE_ENDED) {
                if (!isPlayingFromQueue && controller.mediaItemCount > 0) {
                    controller.seekToDefaultPosition(0)
                    controller.play()
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val controller = mediaController ?: return
            val currentIndex = controller.currentMediaItemIndex
            if (isPlayingFromQueue && currentIndex > 0) {
                for (i in 0 until currentIndex) {
                    controller.removeMediaItem(0)
                }
                val updatedQueue = queue.toMutableList()
                if (currentIndex <= updatedQueue.size) {
                    repeat(currentIndex) {
                        if (updatedQueue.isNotEmpty()) {
                            updatedQueue.removeAt(0)
                        }
                    }
                    queue = updatedQueue
                    repository.saveQueue(queue)
                }
            }
            updateControllerStates()
        }
    }

    private fun updateControllerStates() {
        val controller = mediaController ?: return
        isPlayingState = controller.playWhenReady && controller.playbackState == Player.STATE_READY
        isShuffleState = controller.shuffleModeEnabled
        repeatModeState = controller.repeatMode
        playerVolume = controller.volume

        currentSongIndex = controller.currentMediaItemIndex
        songDuration = controller.duration.coerceAtLeast(0L)
        playbackProgress = controller.currentPosition.coerceAtLeast(0L)

        // Keep current state queue synced with controller queue
        val controllerItems = mutableListOf<Song>()
        for (i in 0 until controller.mediaItemCount) {
            val item = controller.getMediaItemAt(i)
            val songId = item.mediaId.toLongOrNull()
            val song = songs.find { it.id == songId }
            if (song != null) {
                controllerItems.add(song)
            }
        }
        if (controllerItems.isNotEmpty()) {
            queue = controllerItems
        }

        if (isPlayingState) {
            startProgressUpdates()
        } else {
            stopProgressUpdates()
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = lifecycleScope.launch {
            while (isActive) {
                mediaController?.let {
                    playbackProgress = it.currentPosition
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    // Playback control operations
    private fun playSongNow(song: Song) {
        val controller = mediaController ?: return
        
        val activePl = activePlaylist
        val songList = if (activePl != null) {
            playlistSongs
        } else if (songs.contains(song)) {
            songs
        } else {
            listOf(song)
        }
        val startIndex = songList.indexOf(song).coerceAtLeast(0)
        
        isPlayingFromQueue = false
        queue = songList
        repository.saveQueue(queue)
        
        controller.clearMediaItems()
        val mediaItems = songList.map {
            MediaItem.Builder()
                .setMediaId(it.id.toString())
                .setUri(it.uri)
                .build()
        }
        controller.setMediaItems(mediaItems, startIndex, 0)
        controller.setShuffleModeEnabled(isShuffleState)
        controller.prepare()
        controller.play()
    }

    private fun setupQueueInController() {
        val controller = mediaController ?: return
        if (queue.isEmpty()) return
        
        controller.clearMediaItems()
        val mediaItems = queue.map {
            MediaItem.Builder()
                .setMediaId(it.id.toString())
                .setUri(it.uri)
                .build()
        }
        controller.setMediaItems(mediaItems)
        controller.setShuffleModeEnabled(isShuffleState)
        controller.prepare()
    }

    private fun selectQueueIndex(index: Int) {
        val controller = mediaController ?: return
        if (index >= 0 && index < controller.mediaItemCount) {
            isPlayingFromQueue = true
            controller.seekTo(index, 0)
            controller.play()
        }
    }

    private fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        if (index >= 0 && index < controller.mediaItemCount) {
            controller.removeMediaItem(index)
            val updatedQueue = queue.toMutableList()
            if (index < updatedQueue.size) {
                updatedQueue.removeAt(index)
                queue = updatedQueue
                repository.saveQueue(queue)
            }
        }
    }

    private fun clearQueue() {
        val controller = mediaController ?: return
        controller.clearMediaItems()
        queue = emptyList()
        currentSongIndex = -1
        repository.saveQueue(queue)
    }

    private fun addSongToQueue(song: Song) {
        val controller = mediaController ?: return
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .build()
        controller.addMediaItem(mediaItem)
        val updatedQueue = queue.toMutableList().apply { add(song) }
        queue = updatedQueue
        repository.saveQueue(queue)
        Toast.makeText(this, "Nainayon ti kanta iti uray.", Toast.LENGTH_SHORT).show()
    }

    private fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_ENDED) {
                controller.seekTo(0)
            }
            controller.play()
        }
    }

    private fun playNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNext()
        } else if (controller.mediaItemCount > 0) {
            controller.seekToDefaultPosition(0)
            controller.play()
        }
    }

    private fun playPrevious() {
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPrevious()
        } else if (controller.mediaItemCount > 0) {
            controller.seekToDefaultPosition(controller.mediaItemCount - 1)
            controller.play()
        }
    }

    private fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        playbackProgress = position
    }

    private fun setVolume(volume: Float) {
        val controller = mediaController ?: return
        controller.volume = volume
        playerVolume = volume
    }

    private fun toggleShuffle() {
        val controller = mediaController ?: return
        val targetShuffle = !controller.shuffleModeEnabled
        controller.setShuffleModeEnabled(targetShuffle)
    }

    private fun toggleRepeat() {
        val controller = mediaController ?: return
        val newMode = if (controller.repeatMode == Player.REPEAT_MODE_ONE) {
            Player.REPEAT_MODE_OFF
        } else {
            Player.REPEAT_MODE_ONE
        }
        controller.setRepeatMode(newMode)
    }

    private fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val controller = mediaController ?: return
        if (fromIndex in 0 until controller.mediaItemCount && toIndex in 0 until controller.mediaItemCount) {
            controller.moveMediaItem(fromIndex, toIndex)
            val updatedQueue = queue.toMutableList()
            if (fromIndex in updatedQueue.indices && toIndex in updatedQueue.indices) {
                val movedItem = updatedQueue.removeAt(fromIndex)
                updatedQueue.add(toIndex, movedItem)
                queue = updatedQueue
                repository.saveQueue(queue)
            }
        }
    }

    // Playlists Methods
    private fun createPlaylist(name: String) {
        lifecycleScope.launch {
            val result = repository.createPlaylist(name)
            if (result != -1L) {
                playlists = repository.getPlaylists()
                Toast.makeText(this@MainActivity, "Naaramid ti listaan a $name.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Adda kasta a nagan ti listaan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePlaylist(playlistId: Long) {
        lifecycleScope.launch {
            repository.deletePlaylist(playlistId)
            playlists = repository.getPlaylists()
            if (activePlaylist?.id == playlistId) {
                activePlaylist = null
            }
            Toast.makeText(this@MainActivity, "Naipukaw ti listaan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPlaylistSongs(playlist: Playlist) {
        activePlaylist = playlist
        lifecycleScope.launch {
            playlistSongs = repository.getSongsForPlaylist(playlist.id, songs)
        }
    }

    private fun addSongToPlaylist(playlistId: Long, song: Song) {
        lifecycleScope.launch {
            val success = repository.addSongToPlaylist(playlistId, song.id)
            if (success) {
                playlists = repository.getPlaylists()
                activePlaylist?.let {
                    if (it.id == playlistId) {
                        loadPlaylistSongs(it)
                    }
                }
                Toast.makeText(this@MainActivity, "Nainayon ti kanta iti listaan.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Addan daytoy a kanta iti listaan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeSongFromPlaylist(playlistId: Long, song: Song) {
        lifecycleScope.launch {
            repository.removeSongFromPlaylist(playlistId, song.id)
            playlists = repository.getPlaylists()
            activePlaylist?.let {
                if (it.id == playlistId) {
                    loadPlaylistSongs(it)
                }
            }
            Toast.makeText(this@MainActivity, "Naikkat ti kanta iti listaan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleDarkTheme() {
        val target = !isDarkTheme
        isDarkTheme = target
        getSharedPreferences("kansion_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("dark_theme", target)
            .apply()
    }

    private fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        sleepTimerMinutes = minutes
        if (minutes == 0) {
            sleepTimerRemainingSeconds = 0
            return
        }
        sleepTimerRemainingSeconds = minutes * 60
        sleepTimerJob = lifecycleScope.launch {
            while (sleepTimerRemainingSeconds > 0) {
                delay(1000)
                sleepTimerRemainingSeconds--
            }
            mediaController?.pause()
            sleepTimerMinutes = 0
            sleepTimerRemainingSeconds = 0
            Toast.makeText(this@MainActivity, "Nalpas ti oras ti turog. Innalikaten ti patugtog.", Toast.LENGTH_LONG).show()
        }
    }

    private fun rescanLibrary() {
        loadMusicData()
        Toast.makeText(this, "Nalpas a nasukir dagiti kansion.", Toast.LENGTH_SHORT).show()
    }

    private fun resetAppData() {
        lifecycleScope.launch {
            repository.clearAllData()
            loadMusicData()
            Toast.makeText(this@MainActivity, "Na-reset amin a listaan ken uray.", Toast.LENGTH_SHORT).show()
        }
    }
}
