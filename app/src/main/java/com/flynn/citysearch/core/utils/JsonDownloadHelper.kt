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
import java.security.MessageDigest

object JsonDownloadHelper {

    /**
     * Gets the response as a body and copies it to a file in chunks defined by the buffer size.
     *
     * */
    suspend fun downloadToFile(
        apiCall: suspend () -> Response<ResponseBody>,
        context: Context
    ): File? = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "cities.json")
        val tempFile = File(context.cacheDir, "cities_tmp.json")

        val response = apiCall()
        if (!response.isSuccessful) throw IOException("Download failed: ${response.code()}")
        val body = response.body() ?: throw IOException("Empty response body")

        body.byteStream().use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        handleDownloadedFile(file, tempFile)
    }

    private fun handleDownloadedFile(file: File, tempFile: File): File? {
        return if (file.exists()) {
            val oldHash = file.md5()
            val newHash = tempFile.md5()
            if (oldHash != newHash) {
                tempFile.copyTo(file, overwrite = true)
                println("File updated")
                file
            } else {
                println("File not updated")
                tempFile.delete()
                null
            }
        } else {
            tempFile.copyTo(file)
            file
        }
    }

    private fun File.md5(): String {
        val digest = MessageDigest.getInstance("MD5")
        inputStream().use { fis ->
            val buffer = ByteArray(1024 * 4)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
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