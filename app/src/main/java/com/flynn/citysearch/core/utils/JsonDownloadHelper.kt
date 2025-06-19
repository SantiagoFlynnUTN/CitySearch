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
        fileName: String,
        apiCall: suspend () -> Response<ResponseBody>,
        context: Context
    ): File? = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, fileName)
        val tempFile = File(context.cacheDir, "tmp.json")

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
        countOnly: Boolean = false,
        file: File,
        crossinline onItem: suspend (T) -> Unit
    ): Int {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(T::class.java)

        var count = 0

        file.source().buffer().use { source ->
            JsonReader.of(source).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    handleJsonItem(reader, adapter, countOnly, onItem)
                    count++
                }
                reader.endArray()
            }
        }
        return count
    }

    suspend inline fun <reified T> handleJsonItem(
        reader: JsonReader,
        adapter: JsonAdapter<T>,
        countOnly: Boolean,
        crossinline onItem: suspend (T) -> Unit
    ) {
        if (countOnly) {
            reader.skipValue()
        } else {
            runCatching {
                adapter.fromJson(reader)
            }.onSuccess { item ->
                if (item != null) onItem(item)
            }.onFailure {
                reader.skipValue()
            }
            yield()
        }
    }
}