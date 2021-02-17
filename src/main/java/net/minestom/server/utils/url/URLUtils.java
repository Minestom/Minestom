package net.minestom.server.utils.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class URLUtils {

    private URLUtils() {

    }


    public static String getText(String url) throws IOException {
        BufferedReader in = new BufferedReader(getReader(url));

        StringBuilder response = new StringBuilder();
        String currentLine;

        while ((currentLine = in.readLine()) != null)
            response.append(currentLine);

        in.close();

        return response.toString();
    }

    public static InputStreamReader getReader(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        // add headers to the connection, or check the status if desired..

        // handle error response code it occurs
        final int responseCode = connection.getResponseCode();
        final InputStream inputStream;
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        return new InputStreamReader(inputStream);
    }

}
