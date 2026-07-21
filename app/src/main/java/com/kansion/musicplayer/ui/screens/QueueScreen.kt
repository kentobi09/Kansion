package com.kansion.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kansion.musicplayer.R
import com.kansion.musicplayer.Song
import com.kansion.musicplayer.ui.theme.PrimaryGold
import com.kansion.musicplayer.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope

@Composable
fun QueueScreen(
    queue: List<Song>,
    currentSongIndex: Int,
    onSongSelect: (Int) -> Unit,
    onRemoveSong: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onQueueMove: (Int, Int) -> Unit,
    isPlaybackActive: Boolean
) {
    val dragDropListState = rememberDragDropListState(onMove = onQueueMove)

    Column(modifier = Modifier.fillMaxSize()) {
        // Queue Header Action (Clear)
        if (queue.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${queue.size} a kansion iti ur-uray",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )
                
                TextButton(
                    onClick = onClearQueue,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.ClearAll, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Dalusan ti Uray")
                }
            }
        }

        if (queue.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Awan ti kanta iti ur-uray.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                state = dragDropListState.lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset -> dragDropListState.onDragStart(offset) },
                            onDragEnd = { dragDropListState.onDragInterrupted() },
                            onDragCancel = { dragDropListState.onDragInterrupted() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDropListState.onDrag(dragAmount)
                            }
                        )
                    },
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                itemsIndexed(queue, key = { index, song -> "${song.id}_$index" }) { index, song ->
                    val isPlaying = index == currentSongIndex
                    val isDragged = index == dragDropListState.currentIndexOfDraggedItem
                    val displacement = if (isDragged) dragDropListState.draggedDistance else 0f
                    
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = displacement
                            }
                            .zIndex(if (isDragged) 1f else 0f)
                    ) {
                        QueueItem(
                            song = song,
                            isPlaying = isPlaying,
                            isPlaybackActive = isPlaybackActive,
                            onClick = { onSongSelect(index) },
                            onRemove = { onRemoveSong(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueItem(
    song: Song,
    isPlaying: Boolean,
    isPlaybackActive: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play state icon
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
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = TextSecondary,
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

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove from Queue",
                tint = TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Custom Drag and Drop List State Implementation
@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (Int, Int) -> Unit
): DragDropListState {
    val coroutineScope = rememberCoroutineScope()
    return remember(lazyListState) {
        DragDropListState(
            lazyListState = lazyListState,
            coroutineScope = coroutineScope,
            onMove = onMove
        )
    }
}

class DragDropListState(
    val lazyListState: LazyListState,
    private val coroutineScope: CoroutineScope,
    private val onMove: (Int, Int) -> Unit
) {
    var draggedDistance by mutableFloatStateOf(0f)
    var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)
    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                offset.y.toInt() in item.offset..(item.offset + item.size)
            }?.also {
                initiallyDraggedElement = it
                currentIndexOfDraggedItem = it.index
            }
    }

    fun onDragInterrupted() {
        initiallyDraggedElement = null
        currentIndexOfDraggedItem = null
        draggedDistance = 0f
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y

        val initialDraggedElement = initiallyDraggedElement ?: return
        val startOffset = initialDraggedElement.offset + draggedDistance
        val endOffset = startOffset + initialDraggedElement.size

        val targetItem = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                if (item.offset == initialDraggedElement.offset) return@firstOrNull false
                
                val itemStart = item.offset
                val itemEnd = item.offset + item.size
                
                startOffset.toInt() in itemStart..itemEnd || endOffset.toInt() in itemStart..itemEnd
            }

        if (targetItem != null) {
            val currentIndex = currentIndexOfDraggedItem ?: return
            
            // Invoke callback to swap elements in parent state
            onMove(currentIndex, targetItem.index)
            
            currentIndexOfDraggedItem = targetItem.index
            draggedDistance += (initialDraggedElement.offset - targetItem.offset)
            initiallyDraggedElement = targetItem
        }
    }
}
