package cat.moki.acute

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_IDLE
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
//import com.google.android.exoplayer2.ExoPlayer
//import com.google.android.exoplayer2.Player.STATE_IDLE
//import com.google.android.exoplayer2.Player.STATE_READY
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds

data class PlayerControllerData(
    var song: Song? = null,
    var songId: Int = 0,
    var currentPosition: Long = 0,
    var bufferPosition: Long = 0,
    var isPlaying: Boolean = false,
    var isLoading: Boolean = false,
    var positionStart: Long = 0,
    var timeStart: Long = 0,
//    var playlist: List<Song> = listOf()
)

class PlayerControllerViewModel() : ViewModel() {
    private val _data = MutableStateFlow(PlayerControllerData())
    val player = mutableStateOf<Player?>(null, neverEqualPolicy())

    val state = _data.asStateFlow()
    var playlist = mutableListOf<Song>()

    var job: Job? = null

    fun addPlaylist(song: Song) {
        playlist.add(song)
    }

    fun setSongId(songId: Int) {
        _data.update {
            it.copy(
                songId = songId,
                song = playlist.getOrNull(songId),
            )
        }
        player.value?.currentPosition?.let { it1 ->
            _data.update {
                it.copy(
                    currentPosition = it1,
                )
            }
        }
    }

    private fun startProgress() {
        job = viewModelScope.launch {
            while (true) {
                player.value?.currentPosition?.let { it1 ->
                    _data.update {
                        it.copy(
                            currentPosition = it1,
                        )
                    }

                }
                delay(250.milliseconds)
            }
        }
    }

    fun setPlaying(playing: Boolean) {
        job?.cancel()
        if (playing) {
            startProgress()
        }
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlayerController(
    playerData: PlayerControllerViewModel,
) {
    val data = playerData.state.collectAsState()
    val player = playerData.player.value ?: return

    Row(modifier = Modifier.padding(top = 2.dp, bottom = 6.dp, start = 12.dp, end = 12.dp)) {
        Card(shape = RoundedCornerShape(8.dp)) {

            GlideImage(
                model = data.value.song?.let { NetClient.getCoverArtUrl(it.albumId) }
                    ?: kotlin.run { R.drawable.ic_baseline_library_music_24 },
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentDescription = "Localized description",
                contentScale = ContentScale.Crop,
            )

        }
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = data.value.song?.title ?: "",
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = data.value.song?.artist ?: "",
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(
            Modifier
                .weight(1f)
                .aspectRatio(1f)
        )
        val modifier = Modifier.fillMaxHeight()
        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                modifier = modifier.align(Alignment.Center),
                onClick = {
                    when (player.isPlaying) {
                        true -> player.pause()
                        false -> {
                            if (player.playbackState == STATE_IDLE) {
                                player.playWhenReady = true
                                player.prepare()
                            } else {
                                player.play()
                            }
                        }
                    }
                }

            ) {
                when (player.isPlaying) {
                    true -> Icon(Icons.Filled.Pause, contentDescription = "")
                    false -> Icon(Icons.Filled.PlayArrow, contentDescription = "")
                }
            }
  

        }

    }
}