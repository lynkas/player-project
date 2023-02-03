package cat.moki.acute

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import cat.moki.acute.ui.theme.AcuteTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import java.util.*

class SinglePlayer : ComponentActivity() {
    private lateinit var album: Album
    private lateinit var song: Song
    private var mBound: Boolean = false
    private var player: MutableState<ExoPlayer?> = mutableStateOf(null)
    private lateinit var token: MediaSessionCompat.Token

    //    private lateinit var connector: MediaSessionConnector
    private lateinit var controller: MediaControllerCompat

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as PlayService.LocalBinder
//            player.value = binder.getPlayer()
//            token = binder.getToken()
            controller = MediaControllerCompat(this@SinglePlayer, token)
//            connector = MediaSessionConnector(session).apply {
//                setPlayer(player)
//            }
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        album = intent.getParcelableExtra("album")!!
        song = intent.getParcelableExtra("song")!!
//        player = ExoPlayer.Builder(this).build()
        setContent {
            AcuteTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerPage(album = album, song = song, player = player.value)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, PlayService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlayerPage(album: Album, song: Song, player: ExoPlayer?) {

    ConstraintLayout {
        val (cover, text, controlPanel) = createRefs()
        Box(modifier = Modifier
            .constrainAs(cover) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .fillMaxWidth()
            .padding(top = 64.dp)
        ) {
            Card(modifier = Modifier.align(Alignment.Center)) {
                GlideImage(
                    model = album.coverArt?.let { NetClient.getCoverArtUrl(it) },
                    contentDescription = "aaaa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                )
            }

        }
        Box(modifier = Modifier.constrainAs(text) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(cover.bottom)
            bottom.linkTo(controlPanel.top)
        }) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                album.title?.let {
//                    Text(
//                        text = it,
//                        fontSize = 20.sp,
//                        modifier = modifier.alpha(0.6f)
//                    )
//                }
                Text(
                    text = song.title,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 18.sp,
                    modifier = Modifier.alpha(0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

            }

        }
        Box(modifier = Modifier
            .constrainAs(controlPanel) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)

            }
            .height(256.dp)
            .padding(horizontal = 24.dp)
        ) {

//            if (player != null) {
//                AndroidView(factory = { context ->
//
//                    (LayoutInflater.from(context).inflate(
//                        R.layout.player_control_view,
//                        null,
//                        false
//                    ) as PlayerControlView).apply {
//                        this.player = player
//
//                    }
////                    PlayerControlView(
////                        context,
////
////                    ).apply {
////                        showTimeoutMs = 0
////                    }
//                })
//            }

//            Column() {
//                Slider(value = 0.10f, onValueChange = {})
//                Box(modifier = Modifier.fillMaxWidth()) {
//                    Row(
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(
//                            onClick = { /*TODO*/ }, modifier = Modifier.size(64.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.SkipPrevious,
//                                contentDescription = "", modifier = Modifier.size(64.dp)
//                            )
//                        }
//                        IconButton(onClick = { /*TODO*/ }, modifier = Modifier.size(96.dp)) {
//                            Icon(
//                                imageVector = Icons.Filled.PlayArrow,
//                                contentDescription = "",
//                                modifier = Modifier.size(96.dp)
//                            )
//
//                        }
//                        IconButton(
//                            onClick = { /*TODO*/ }, modifier = Modifier.size(64.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.SkipNext,
//                                contentDescription = "", modifier = Modifier.size(64.dp)
//                            )
//                        }
//
//                    }
//                }
//
//            }
        }
//        ControlPanel(modifier = Modifier.constrainAs(controlPanel) {
//            top.linkTo(text.bottom)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//            bottom.linkTo(parent.bottom)
//
//        })
//        Button(
//            onClick = { /* Do something */ },
//            // Assign reference "button" to the Button composable
//            // and constrain it to the top of the ConstraintLayout
//            modifier = Modifier.constrainAs(button) {
//                top.linkTo(parent.top, margin = 16.dp)
//            }
//        ) {
//            Text("Button")
//        }

        // Assign reference "text" to the Text composable
        // and constrain it to the bottom of the Button composable
//        Text("Text", Modifier.constrainAs(text) {
//            top.linkTo(button.bottom, margin = 16.dp)
//        })
    }

}

@Composable
fun ControlPanel(modifier: Modifier) {
    Box(modifier = modifier.height(400.dp)) {
        Slider(value = 0.10f, onValueChange = {})

    }
}
