package net.minestom.server.utils.url

import java.io.IOException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

object URLUtils {
    @JvmStatic
    @Throws(IOException::class)
    fun getText(url: String?): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        //add headers to the connection, or check the status if desired..

        // handle error response code it occurs
        val responseCode = connection.responseCode
        val inputStream: InputStream
        inputStream = if (200 <= responseCode && responseCode <= 299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val `in` = BufferedReader(
            InputStreamReader(
                inputStream
            )
        )
        val response = StringBuilder()
        var currentLine: String?
        while (`in`.readLine().also { currentLine = it } != null) response.append(currentLine)
        `in`.close()
        return response.toString()
    }
}