package cat.moki.acute

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import cat.moki.acute.ui.theme.AcuteTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.session.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.common.util.concurrent.MoreExecutors

class Home : ComponentActivity() {
    private val library: LibraryViewModel by viewModels()

    private var mediaPlayer: MutableState<Player?> = mutableStateOf(null)
    private val playerData: PlayerControllerViewModel by viewModels()


    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlayService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                val controller = controllerFuture.get()
                playerData.player.value = controller
                controller.addListener(object : Listener {
                    override fun onEvents(player: Player, events: Player.Events) {
                        super.onEvents(player, events)
                        playerData.player.value = controller
                        playerData.setSongId(player.currentMediaItemIndex)
                        playerData.setPlaying(player.isPlaying)

                    }

                })
            },
            MoreExecutors.directExecutor()

        )

    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)


        setContent {
            LaunchedEffect(Client.networkMetered.value) {
                library.reset()
            }
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()
            val navController = rememberNavController()
            val currentRoute = navController
                .currentBackStackEntryFlow
                .collectAsState(initial = navController.currentBackStackEntry)
            AcuteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            when (val route = currentRoute.value?.destination?.route) {
                                "library" -> {
                                    TopAppBar(
                                        title = { Text(text = "Library") },
                                        scrollBehavior = scrollBehavior,
                                        colors = TopAppBarDefaults.largeTopAppBarColors(),
                                    )
                                }
                                "album/{albumID}" -> {
                                    TopAppBar(
                                        title = {
                                            Text(
                                                text = library.getAlbumByID(
                                                    currentRoute.value?.arguments?.getString(
                                                        "albumID"
                                                    )!!
                                                )?.name!!,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        })
                                }
                                "playlist" -> {
                                    TopAppBar(
                                        title = {
                                            Text(
                                                text = "Playlist",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        })
                                }
                                else -> {}
                            }
                        },
                        bottomBar = {
                            BottomAppBar() {
                                PlayerController(playerData)
                            }
                        }
                    ) {
                        Box(modifier = Modifier.padding(it)) {
                            NavHost(navController = navController, startDestination = items[0]) {
                                navigation(startDestination = "library", route = items[0]) {
                                    composable("playlist") {
                                        playerData.player.value?.let { it1 ->
                                            PlayList(
                                                it1,
                                                playerData.playlist,
                                                currentPlayingIndex = playerData.state.collectAsState().value.songId
                                            )
                                        }
                                    }
                                    composable("library") {
                                        Library(library = library, onNavToAlbum = { albumID ->
                                            navController.navigate("album/${albumID}")
                                        })
                                    }
                                    composable(
                                        "album/{albumID}",
                                    ) { backStackEntry ->
                                        val albumDetail = rememberSaveable(
                                        ) {
                                            AlbumDetailData(
                                                library.getAlbumByID(
                                                    backStackEntry.arguments?.getString(
                                                        "albumID"
                                                    )!!
                                                )!!
                                            )
                                        }
                                        val context = LocalContext.current
                                        LaunchedEffect(true) {
                                            albumDetail.getSongList(context)
                                        }
                                        AlbumDetailComponent(
                                            albumDetail = albumDetail,
                                            addSong = { song ->
                                                playerData.player.value?.addMediaItem(
                                                    MediaItem.Builder()
                                                        .setMediaId(song.id)
                                                        .setMediaMetadata(
                                                            MediaMetadata.Builder()
                                                                .setArtworkUri(
                                                                    Uri.parse(
                                                                        NetClient.getCoverArtUrl(
                                                                            song.albumId
                                                                        )
                                                                    )
                                                                ).build()
                                                        )
                                                        .build()
                                                )
                                                playerData.player.value?.let {
                                                    it.playWhenReady = true
                                                    it.prepare()
                                                } ?: Log.d("player", "null")
                                                playerData.addPlaylist(song)
                                            }
                                        )
                                    }
                                }
                                composable(items[1]) {
                                    Box() {
                                        Text(text = "aaaa")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Library(library: LibraryViewModel, onNavToAlbum: (String) -> Unit) {

    if (library.viewBy == ViewBy.grid) {
        AlbumPreviewGrid(library = library, onNavToAlbum = onNavToAlbum)
    }
    if (library.viewBy == ViewBy.list) {
        AlbumPreviewList(library = library, onNavToAlbum = onNavToAlbum)
    }

}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AcuteTheme {
        Greeting("Android")
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayList(player: Player, playlist: List<Song>, currentPlayingIndex: Int = 0) {
    LazyColumn() {
        itemsIndexed(playlist) { index, song ->
            Card(
                modifier = Modifier
                    .background(Color.White),
                shape = RoundedCornerShape(0)
            ) {
                Box(
                    modifier = Modifier
                        .height(72.dp)
                        .background(Color.White)
                ) {
                    ListItem(
                        modifier = Modifier
                            .height(72.dp),
                        colors = if (index == currentPlayingIndex) ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        ) else ListItemDefaults.colors(),
                        leadingContent = {
                            Card(shape = RoundedCornerShape(8.dp)) {
                                GlideImage(
                                    model = song.coverArt?.let { NetClient.getCoverArtUrl(id = it) },
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f),
                                ) {
                                    it
                                        .error(R.drawable.ic_baseline_library_music_24)
                                        .placeholder(R.drawable.ic_baseline_downloading_24)
                                }
                            }
                        },
                        headlineText = {
                            Text(
                                text = song.title,
                                fontSize = 20.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingText = {
                            Text(
                                text = song.artist,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                    )
                }
            }
        }
    }
}