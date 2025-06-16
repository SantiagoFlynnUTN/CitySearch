package com.flynn.citysearch.core.utils

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.ResponseBody
import okio.buffer
import okio.source
import retrofit2.Response
import java.io.File
import java.io.IOException

object JsonDownloadHelper {

    /**
     * Gets the response as a body and copies it to a file in chunks defined by the buffer size.
     *
     * */
    suspend fun downloadToFile(
        apiCall: suspend () -> Response<ResponseBody>,
        context: Context
    ): File = withContext(Dispatchers.IO) {
        val response = apiCall()
        if (!response.isSuccessful) throw IOException("Download failed: ${response.code()}")

        val file = File(context.cacheDir, "cities.json")
        response.body()?.byteStream()?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Empty response body")
        file
    }

    suspend inline fun <reified T> readArrayInChunks(
        file: File,
        crossinline onItem: suspend (T) -> Unit
    ) {
        val adapter: JsonAdapter<T> = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build().adapter(T::class.java)

        file.source().buffer().use { source ->
            JsonReader.of(source).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    try {
                        val item = adapter.fromJson(reader)
                        if (item != null) {
                            onItem(item)
                        } else {
                            println("Skipped null item")
                        }
                    } catch (e: Exception) {
                        println("Parsing failed: ${e.message}")
                        reader.skipValue()
                    }
                    yield()
                }
                reader.endArray()
            }
        }
    }
}