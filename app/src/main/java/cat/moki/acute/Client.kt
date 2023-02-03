package cat.moki.acute

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.danikula.videocache.HttpProxyCacheServer
import com.google.gson.reflect.TypeToken
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type


interface IData {
    suspend fun albumList(
        type: String = "newest", size: Int = 10, offset: Int = 0,
        fromYear: Int? = null,
        toYear: Int? = null,
        genre: String? = null,
        musicFolderId: String? = null
    ): List<Album>

    suspend fun albumDetail(id: String): Album
}

class EmptyBodyException : Exception()
class RequestFailedException : Exception()


object Client {
    var networkMetered = mutableStateOf(false)

    enum class Type {
        Local,
        Online
    }

    fun store(context: Context, type: Type? = null): IData {
        val client = when (type) {
            Type.Online -> online(context = context)
            else -> when (networkMetered.value) {
                true -> local(context = context)
                false -> online(context = context)
            }
        }
        return client
    }

    private fun online(context: Context): IData {
        return OnlineClient(context = context)
    }

    private fun local(context: Context): IData {
        return LocalClient(context = context)
    }
}

class OnlineClient(val context: Context) : IData {
    override suspend fun albumList(
        type: String,
        size: Int,
        offset: Int,
        fromYear: Int?,
        toYear: Int?,
        genre: String?,
        musicFolderId: String?
    ): List<Album> {
        val data = NetClient
            .request().getAlbumList2(type, size, offset, fromYear, toYear, genre, musicFolderId)
        if (data.isSuccessful) {
            data.body()?.subsonicResponse?.albumList2?.album?.let {
                AppDatabase.getInstance(context).album().insertAll(*it.toTypedArray())
                return it
            }
            throw EmptyBodyException()
        }
        throw RequestFailedException()
    }

    override suspend fun albumDetail(id: String): Album {
        val data = NetClient.request().getAlbum(id)
        if (data.isSuccessful) {
            data.body()?.subsonicResponse?.album?.let {
                AppDatabase.getInstance(context).album().insertAll(it)
                it.song?.let { songs ->
                    AppDatabase.getInstance(context).song().insertAll(*songs.toTypedArray())
                }
                return it
            }
            throw EmptyBodyException()
        }
        throw RequestFailedException()
    }
}

class LocalClient(val context: Context) : IData {
    override suspend fun albumList(
        type: String,
        size: Int,
        offset: Int,
        fromYear: Int?,
        toYear: Int?,
        genre: String?,
        musicFolderId: String?
    ): List<Album> {
        return AppDatabase.getInstance(context).album().getAll(limit = size, offset = offset)
    }

    override suspend fun albumDetail(id: String): Album {
        val songs = AppDatabase.getInstance(context).song().getAlbum(id)
        val album = AppDatabase.getInstance(context).album().get(id)
        album.song = songs
        return album
    }
}

object NetClient {
    @SuppressLint("SuspiciousIndentation")
    private val okHttpClient = OkHttpClient().newBuilder().addInterceptor {
        val request: HttpUrl = authInterceptor(it.request().url.newBuilder()).build()
        it.proceed(it.request().newBuilder().url(request).build())

    }.build()
//    private var _proxy: HttpProxyCacheServer? = null
//    fun getProxy(context: Context): HttpProxyCacheServer? {
//        return if (_proxy == null) HttpProxyCacheServer(context.applicationContext).also {
//            _proxy = it
//        } else _proxy
//    }


    val BaseUrl = "a"
    private fun authInterceptor(builder: HttpUrl.Builder): HttpUrl.Builder {
        return builder.addQueryParameter("u", "a")
            .addQueryParameter("p", "a")
            .addQueryParameter("v", "1.12.0")
            .addQueryParameter("c", "acute")
            .addQueryParameter("f", "json")
    }

    private val client = Retrofit.Builder().apply {
        baseUrl(BaseUrl)
        client(okHttpClient)
        addConverterFactory(GsonConverterFactory.create())
    }.build().create(Api::class.java)

    fun request(): Api {
        return client
    }

    fun getCoverArtUrl(id: String, size: Int? = null): String {
        authInterceptor(BaseUrl.toHttpUrlOrNull()!!.newBuilder()).apply {

            addPathSegment("getCoverArt")
            addQueryParameter("id", id)
            if (size != null) addQueryParameter("size", size.toString())
            return build().toString()
        }
    }

    fun getStreamUrl(id: String): String {
        authInterceptor(BaseUrl.toHttpUrlOrNull()!!.newBuilder()).apply {
            addPathSegment("stream")
            addQueryParameter("id", id)
            addQueryParameter("maxBitRate", "192")
            return AcuteApplication.proxy.getProxyUrl(build().toString())
        }
    }

}

class ResConverter : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val envelopedType = TypeToken.getParameterized(Res::class.java, type).type
        val delegate: Converter<ResponseBody, Res> =
            retrofit.nextResponseBodyConverter(this, envelopedType, annotations)
        return Converter<ResponseBody, SubsonicResponse> {
            delegate.convert(it)!!.subsonicResponse
        }
    }
}

interface Api {
    @GET("ping.view")
    suspend fun ping(): Response<Res>

    @GET("getAlbumList")
    suspend fun getAlbumList(
        @Query("type") type: String = "newest",
        @Query("size") size: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("fromYear") fromYear: Int? = null,
        @Query("toYear") toYear: Int? = null,
        @Query("genre") genre: String? = null,
        @Query("musicFolderId") musicFolderId: String? = null
    ): Response<Res>

    @GET("getAlbumList2")
    suspend fun getAlbumList2(
        @Query("type") type: String = "newest",
        @Query("size") size: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("fromYear") fromYear: Int? = null,
        @Query("toYear") toYear: Int? = null,
        @Query("genre") genre: String? = null,
        @Query("musicFolderId") musicFolderId: String? = null
    ): Response<Res>

    @GET("getAlbum")
    suspend fun getAlbum(
        @Query("id") id: String,
    ): Response<Res>
}

