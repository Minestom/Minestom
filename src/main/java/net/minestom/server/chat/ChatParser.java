package net.minestom.server.chat;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class used to convert JSON string to proper chat message representation
 */
public class ChatParser {

    public static final char COLOR_CHAR = (char) 0xA7; // Represent the character '§'

    /**
     * Convert a simple colored message json (text/color) to a {@link ColoredText}
     *
     * @param json the json containing the text and color
     * @return a {@link ColoredText} representing the text
     */
    public static ColoredText toColoredText(String json) {
        StringBuilder builder = new StringBuilder();

        final JsonObject object = JsonParser.parseString(json).getAsJsonObject();

        builder.append(parseText(object));

        final boolean hasExtra = object.has("extra");
        if (hasExtra) {
            JsonArray extraArray = object.get("extra").getAsJsonArray();
            for (JsonElement element : extraArray) {
                JsonObject extraObject = element.getAsJsonObject();
                builder.append(parseText(extraObject));
            }
        }

        return ColoredText.of(builder.toString());
    }

    /**
     * Get the format representing of a single text component (text + color key)
     *
     * @param textObject the text component to parse
     * @return the colored text format of the text component
     */
    private static String parseText(JsonObject textObject) {
        final boolean hasText = textObject.has("text");
        if (!hasText)
            return "";

        final boolean hasColor = textObject.has("color");

        StringBuilder builder = new StringBuilder();

        // Add color
        if (hasColor) {
            String colorString = textObject.get("color").getAsString();
            if (colorString.startsWith("#")) {
                // RGB format
                builder.append("{").append(colorString).append("}");
            } else {
                // Color simple name
                ChatColor color = ChatColor.fromName(colorString);
                builder.append(color);
            }
        }

        // Add text
        String text = textObject.get("text").getAsString();
        builder.append(text);

        return builder.toString();
    }
}
