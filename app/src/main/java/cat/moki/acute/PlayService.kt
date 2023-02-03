package cat.moki.acute

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import androidx.media3.ui.PlayerNotificationManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.common.util.concurrent.ListenableFuture


class PlayService : MediaSessionService() {
//    private lateinit var mediaBrowser: MediaBrowserServiceCompat

    private val CHANNEL_MINI_PLAYER = "CHANNEL_MINI_PLAYER"
    private val PLAYER_NOTIFICATION_ID = 1
    private lateinit var messageHandler: Handler

    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var player: ExoPlayer

    //    private lateinit var connector: MediaSessionConnector
    private val playlist: MutableList<Song> = mutableListOf()
    private var playerPlaylist: MutableList<MediaItem> = mutableListOf()
    private var mediaSession: MediaSession? = null;
    private var songId = 0
    private val song: Song?
        get() = playlist.getOrNull(songId)

    public final val ADD_TRACK = "ADD_TRACK"

    //    private lateinit var album: Album;
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getPlayer(): Player {
            return player
        }

        val song
            get() = playlist.getOrNull(songId)

        val songID
            get() = songId


        fun addPlaylistItem(song: Song) {
            Log.d("add item:", song.title)
            playlist.add(song)
//            playerPlaylist.add(MediaItem.fromUri(Client.getStreamUrl(song.id)))
            if (playlist.size == 1) {
                Log.d("should init: ", playlist().size.toString())
                initSong(song)
            }
            MediaItem.Builder()
                .setUri(NetClient.getStreamUrl(song.id))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setTitle(song.title)
                        .setArtworkUri(Uri.parse(NetClient.getStreamUrl(song.albumId)))
                        .build()
                )
            player.addMediaItem(MediaItem.fromUri(NetClient.getStreamUrl(song.id)))
            Log.d("add to list", "playerPlaylist " + playerPlaylist.size.toString())
        }

        fun playlist(): List<Song> {
            return playlist
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("service", "oncreate")
        player = ExoPlayer.Builder(this).build()

        mediaSession =
            MediaSession.Builder(this, player).setCallback(object : MediaSession.Callback {

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {

                    return super.onCustomCommand(session, controller, customCommand, args)
                }

                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: MutableList<MediaItem>
                ): ListenableFuture<MutableList<MediaItem>> {
                    player.addMediaItem(
                        MediaItem
                            .Builder()
                            .setUri(NetClient.getStreamUrl(mediaItems[0].mediaId))
                            .setMediaMetadata(mediaItems[0].mediaMetadata).build()
                    )

                    return super.onAddMediaItems(mediaSession, controller, mediaItems)
                }
            }).build()

    }

    fun initSong(song: Song) {
        Glide.with(this@PlayService).asBitmap()
            .load(NetClient.getCoverArtUrl(song.albumId))
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                }
            })


    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()

    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onUpdateNotification(session: MediaSession) {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

    }
}