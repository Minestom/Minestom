package net.minestom.server.chat;


import com.google.gson.*;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to convert JSON string to proper chat message representation.
 * @deprecated Use {@link GsonComponentSerializer}
 */
@Deprecated
public final class ChatParser {

    public static final char COLOR_CHAR = (char) 0xA7; // Represent the character '§'

    /**
     * Converts a simple colored message json (text/color) to a {@link ColoredText}.
     *
     * @param json the json containing the text and color
     * @return a {@link ColoredText} representing the text
     */
    @NotNull
    public static ColoredText toColoredText(@NotNull String json) {
        StringBuilder builder = new StringBuilder();

        try {
            final JsonElement element = JsonParser.parseString(json);

            if (element instanceof JsonObject) {
                final JsonObject object = element.getAsJsonObject();
                appendBuilder(builder, object);
            } else if (element instanceof JsonArray) {
                final JsonArray array = element.getAsJsonArray();
                for (JsonElement e : array) {
                    final JsonObject object = e.getAsJsonObject();
                    appendBuilder(builder, object);
                }
            }

            return ColoredText.of(builder.toString());
        } catch (JsonSyntaxException e) {
            // Not a json text
            return ColoredText.of(json);
        }
    }

    private static void appendBuilder(@NotNull StringBuilder builder, @NotNull JsonObject object) {
        builder.append(parseText(object));

        final boolean hasExtra = object.has("extra");
        if (hasExtra) {
            final JsonArray extraArray = object.get("extra").getAsJsonArray();
            for (JsonElement extraElement : extraArray) {
                final JsonObject extraObject = extraElement.getAsJsonObject();
                builder.append(parseText(extraObject));
            }
        }
    }

    /**
     * Gets the format representing of a single text component (text + color key).
     *
     * @param textObject the text component to parse
     * @return the colored text format of the text component
     */
    @NotNull
    private static String parseText(@NotNull JsonObject textObject) {
        final boolean hasText = textObject.has("text");
        if (!hasText)
            return "";

        StringBuilder builder = new StringBuilder();

        appendColor(textObject, builder);
        appendExtra(textObject, builder, "bold");
        appendExtra(textObject, builder, "italic");
        appendExtra(textObject, builder, "underlined");
        appendExtra(textObject, builder, "strikethrough");
        appendExtra(textObject, builder, "obfuscated");

        // Add text
        final String text = textObject.get("text").getAsString();
        builder.append(text);

        return builder.toString();
    }

    private static void appendColor(@NotNull JsonObject textObject, @NotNull StringBuilder builder) {
        if (textObject.has("color")) {
            final String colorString = textObject.get("color").getAsString();
            if (colorString.startsWith("#")) {
                // RGB format
                builder.append("{").append(colorString).append("}");
            } else {
                // Color simple name
                final ChatColor color = ChatColor.fromName(colorString);
                builder.append(color);
            }
        }
    }

    private static void appendExtra(@NotNull JsonObject textObject, @NotNull StringBuilder builder,
                                    @NotNull String name) {
        if (textObject.has(name)) {
            final boolean value = textObject.get(name).getAsBoolean();
            if (value) {
                builder.append("{#").append(name).append("}");
            }
        }
    }
}
