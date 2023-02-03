package cat.moki.acute

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import cat.moki.acute.ui.theme.AcuteTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.selects.select

enum class ViewBy {
    list, grid
}

class LibraryOOld : ComponentActivity() {
//    private val library: LibraryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var viewBy by rememberSaveable { mutableStateOf(ViewBy.list) }
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            AcuteTheme {

                Surface() {
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            LargeTopAppBar(
                                title = { Text(text = "Library") },
                                scrollBehavior = scrollBehavior,
                                navigationIcon = {
                                    IconButton(onClick = { /* doSomething() */ }) {
                                        Icon(
                                            imageVector = Icons.Filled.Menu,
                                            contentDescription = "Localized description"
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = {
                                        viewBy =
                                            if (viewBy == ViewBy.list) ViewBy.grid else ViewBy.list
                                    }) {
                                        Icon(
                                            imageVector = if (viewBy == ViewBy.list) Icons.Filled.GridView else Icons.Filled.List,
                                            contentDescription = "Localized description"
                                        )
                                    }
                                },
                            )
                        },
                    ) {
                        Box(modifier = Modifier.padding(it)) {

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumPreviewList(library: LibraryViewModel, onNavToAlbum: (String) -> Unit) {
    LazyColumn() {
        items(library.albumList) { album ->
            AlbumListItem(album = album, onNavToAlbum = onNavToAlbum)
        }
        item {
            BoxWithConstraints(
                Modifier.fillMaxWidth()
            ) {
                if (!library.loaded) {
                    val context = LocalContext.current
                    LaunchedEffect(true) {
                        library.get(context)
                    }
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ">_<",
                            fontSize = 40.sp,
                            modifier = Modifier.align(Alignment.BottomCenter),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun AlbumListItem(album: Album, onNavToAlbum: (String) -> Unit) {
    val context = LocalContext.current
    Card(onClick = {
        onNavToAlbum(album.id)
    }) {

        ListItem(
            modifier = Modifier.height(100.dp),
            leadingContent = {
                Card(shape = RoundedCornerShape(8.dp)) {
                    GlideImage(
                        model = album.coverArt?.let { NetClient.getCoverArtUrl(id = it) },
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
                    text = album.name,
                    fontSize = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingText = {
                Text(
                    text = album.artist,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumPreviewGrid(library: LibraryViewModel, onNavToAlbum: (String) -> Unit) {

    LazyVerticalStaggeredGrid(
        modifier = Modifier.padding(10.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
    ) {

        items(library.albumList) {
            AlbumPreview(it, onNavToAlbum)
        }

        item {
            BoxWithConstraints(
                Modifier
                    .width(150.dp)
                    .height(200.dp)
            ) {
                if (!library.loaded) {
                    val context = LocalContext.current
                    LaunchedEffect(true) {
                        library.get(context)
                    }
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .padding(top = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = ">_<",
                            fontSize = 80.sp,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumPreview(album: Album, onNavToAlbum: (String) -> Unit) {
    Card(onClick = {
        onNavToAlbum(album.id)
    }) {
        Column() {
            Card(shape = RoundedCornerShape(8.dp)) {
                GlideImage(
                    model = album.coverArt?.let { NetClient.getCoverArtUrl(id = it) },
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                ) {
                    it
                        .error(R.drawable.ic_baseline_library_music_24)
                        .placeholder(R.drawable.ic_baseline_downloading_24)
                }

            }

            Text(
                text = album.name,
                fontSize = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(6.dp, 4.dp, 6.dp, 0.dp)
            )
            Text(
                text = album.artist,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(6.dp, 4.dp, 6.dp, 16.dp)
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun Test() {

    Column {
        Text(
            text = "aaaaaaaaaaaaaaa",
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

