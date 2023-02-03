package cat.moki.acute

import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes.Margins
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.moki.acute.ui.theme.AcuteTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

class AlbumDetail : ComponentActivity() {
    private lateinit var album: Album
//    private val albumDetail: AlbumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        album = intent.getParcelableExtra("album")!!

        setContent {
        }
    }
}

@Composable
fun AlbumDetailComponent(
    scrollState: ScrollState = rememberScrollState(),
    albumDetail: AlbumDetailData,
    addSong: (Song) -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        AlbumDetailHeadPic(albumDetail.album, scrollState.value)
        AlbumDetailHeadInfo(albumDetail.album)
        SongList(albumDetail.album, albumDetail.songs, albumDetail.sameArtist, addSong)

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumDetailHeadPic(album: Album, scrollPosition: Int) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(12.dp, 0.dp)
            .graphicsLayer { translationY = scrollPosition / 2f }
    ) {
        GlideImage(
            model = NetClient.getCoverArtUrl(album.id),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
fun AlbumDetailHeadInfo(album: Album) {
    Card(
        shape = RoundedCornerShape(bottomEnd = 0.dp, bottomStart = 0.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {

        Column(
            Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = album.name, fontSize = 26.sp, lineHeight = 32.sp)
                    Text(text = album.artist, fontSize = 18.sp)
                    Row() {
                        Text(text = album.duration.formatSecond())
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${album.songCount} ${if (album.songCount == 1) "song" else "songs"}")

                    }
                }
            }

        }
    }
}


@Composable
fun SongList(album: Album, songs: List<Song>, sameArtist: Boolean, addSong: (Song) -> Unit) {
    for ((index, song) in songs.withIndex()) {
        SongListItem(album, song = song, sameArtist = sameArtist, addSong)
        if (index + 1 != songs.size) Divider(Modifier.padding(horizontal = 12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListItem(album: Album, song: Song, sameArtist: Boolean, addSong: (Song) -> Unit) {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(0.dp), onClick = {
        addSong(song)
    }) {
        ListItem(
            headlineText = {
                Text(
                    text = song.title,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingText = {
                if (!sameArtist)
                    Text(
                        text = song.artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
            },
            trailingContent = {
                Text(
                    text = "${song.duration.formatSecond()} ",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
}