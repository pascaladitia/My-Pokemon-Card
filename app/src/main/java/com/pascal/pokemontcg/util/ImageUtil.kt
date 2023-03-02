package com.pascal.pokemontcg.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.pascal.pokemontcg.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


object ImageUtil {

    private const val FILENAME_FORMAT = "dd-MMM-yyyy"

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private fun createTempFile(context: Context, pokemonName: String): File {
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES + "/${context.getString(R.string.app_name)}/"
        ).mkdirs()

        val storageDir: File? = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES + "/${context.getString(R.string.app_name)}/"
        )

        MediaScannerConnection.scanFile(
            context, arrayOf(storageDir.toString()),
            null, null
        )

        return File.createTempFile("$timeStamp-$pokemonName-", ".jpg", storageDir)
    }

    fun saveToFile(
        imageUrl: String,
        pokemonName: String,
        context: Context,
        onLoadingListener: () -> Unit,
        onFinishedListener: (message: String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.IO) {
                    onLoadingListener()

                    val url = URL(imageUrl)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()

                    val input: InputStream = connection.inputStream
                    val myBitmap = BitmapFactory.decodeStream(input)
                    val stream = FileOutputStream(
                        createTempFile(context, pokemonName)
                    )

                    val outStream = ByteArrayOutputStream()
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outStream)
                    val byteArray = outStream.toByteArray()

                    stream.write(byteArray)
                    stream.close()

                    onFinishedListener("The image has been successfully downloaded.")
                }
            } catch (e: Exception) {
                onFinishedListener("An error occurred: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}