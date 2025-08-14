package ru.netology.mediaplayer.repository


import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import ru.netology.mediaplayer.BuildConfig.BASE_URL
import ru.netology.mediaplayer.dto.Playlist
import ru.netology.mediaplayer.error.ApiError
import ru.netology.mediaplayer.error.NetworkError
import ru.netology.mediaplayer.error.UnknownError
import ru.netology.mediaplayer.model.TrackModelState
import java.io.IOException
import javax.inject.Inject


class TrackRepository @Inject constructor(
    private val client: OkHttpClient,
) : TrackRepositoryFun {

    private val _dataState = MutableStateFlow(TrackModelState())
    val dataState: StateFlow<TrackModelState>
        get() = _dataState

    private val gson = Gson()

    override suspend fun getPlayList(): Playlist {
        val request: Request = Request.Builder()
            .url("${BASE_URL}album.json")
            .build()
            _dataState.value = TrackModelState(loading = true)
        try {

            val response = client.newCall(request).await()
            if (!response.isSuccessful) throw ApiError(response.code, response.message)
            _dataState.value = TrackModelState(loading = false)
            return gson.fromJson(response.body.string(), Playlist::class.java)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError) {
            throw e
        } catch (e: Exception) {
            throw UnknownError
        }

    }

}


