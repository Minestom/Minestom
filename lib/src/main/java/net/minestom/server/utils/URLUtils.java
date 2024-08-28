package net.minestom.server.utils;

import org.jetbrains.annotations.Blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class URLUtils {

    private URLUtils() {
    }

    @Blocking
    public static String getText(String url) throws IOException {
        final InputStream inputStream = getInputStream(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder response = new StringBuilder();
        String currentLine;
        while ((currentLine = in.readLine()) != null) response.append(currentLine);
        in.close();

        return response.toString();
    }

    private static InputStream getInputStream(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        //add headers to the connection, or check the status if desired..

        // handle error response code it occurs
        final int responseCode = connection.getResponseCode();
        final InputStream inputStream;
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }
        return inputStream;
    }
}
