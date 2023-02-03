package cat.moki.acute

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

data class Res(
    @SerializedName("subsonic-response")
    val subsonicResponse: SubsonicResponse
)

data class SubsonicResponse(
    val status: String,
    val version: String,
    val type: String,
    val albumList: AlbumList?,
    val albumList2: AlbumList2?,
    val album: Album?,
)


data class AlbumList(
    val album: List<Album>
)

data class AlbumList2(
    val album: List<Album>
)

object Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Entity
@Parcelize
data class Album(
    @PrimaryKey val id: String,
    val coverArt: String?,
    val artist: String,
    val created: Date,
    val title: String?,
    val album: String?,
    val parent: String?,
    val isDir: Boolean?,
    val name: String,
    val songCount: Int,
    val duration: Int,
    val year: Int?,
    @Ignore var song: List<Song>? = null,
) : Parcelable {
    constructor(
        id: String,
        coverArt: String?,
        artist: String,
        created: Date,
        title: String?,
        album: String?,
        parent: String?,
        isDir: Boolean?,
        name: String,
        songCount: Int,
        duration: Int,
        year: Int?,
    ) : this(
        id,
        coverArt,
        artist,
        created,
        title,
        album,
        parent,
        isDir,
        name,
        songCount,
        duration,
        year,
        null
    )
}

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg album: Album)

    @Delete
    fun delete(album: Album)

    @Query("DELETE FROM album where id=:id")
    fun delete(id: String)

    @Query("SELECT * FROM album")
    fun getAll(): List<Album>

    @Query("SELECT * FROM album where id=:id")
    fun get(id: String): Album

    @Query("SELECT * FROM album limit :limit offset :offset")
    fun getAll(limit: Int, offset: Int): List<Album>

}


@Entity
@Parcelize
data class Song(
    @PrimaryKey val id: String,
    val album: String,
    val albumId: String,
    val artist: String,
    val artistId: String,
    val bitRate: Int,
    val contentType: String,
    val coverArt: String?,
    val created: Date,
    val duration: Int,
    val isDir: Boolean,
    val isVideo: Boolean,
    val parent: String,
    val path: String,
    val size: Int,
    val suffix: String,
    val title: String,
    val track: Int,
    val discNumber: Int,
    val type: String,
    val year: Int,
) : Parcelable

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg song: Song)

    @Delete
    fun delete(song: Song)

    @Query("DELETE FROM song where id=:id")
    fun delete(id: String)

    @Query("SELECT * FROM song")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song where albumId=:id")
    fun getAlbum(id: String): List<Song>

    @Query("SELECT * FROM song where id=:id")
    fun get(id: String): Song

}

