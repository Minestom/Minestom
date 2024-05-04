package net.minestom.server.utils.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public final class URLUtils {

    private URLUtils() {

    }

    public static String getText(String url) throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
        //add headers to the connection, or check the status if desired..

        // handle error response code it occurs
        final int responseCode = connection.getResponseCode();
        final InputStream inputStream;
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        inputStream));

        StringBuilder response = new StringBuilder();
        String currentLine;

        while ((currentLine = in.readLine()) != null)
            response.append(currentLine);

        in.close();

        return response.toString();
    }
}
