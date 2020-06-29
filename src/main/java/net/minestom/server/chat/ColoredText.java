package net.minestom.server.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ColoredText {

    private String message;

    private ColoredText(String message) {
        this.message = message;
    }

    public static ColoredText of(ChatColor color, String message) {
        return new ColoredText(color + message);
    }

    public static ColoredText of(String message) {
        return of(ChatColor.WHITE, message);
    }

    public static ColoredText ofFormat(String message) {
        return new ColoredText(message);
    }

    public static ColoredText ofLegacy(String message, char colorChar) {
        String legacy = toLegacy(message, colorChar);

        return ofFormat(legacy);
    }

    public ColoredText append(ChatColor color, String message) {
        this.message += color + message;
        return this;
    }

    public ColoredText append(String message) {
        return append(ChatColor.NO_COLOR, message);
    }

    public ColoredText appendFormat(String message) {
        this.message += message;
        return this;
    }

    private static String toLegacy(String message, char colorChar) {
        String result = "";

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == colorChar) {
                final boolean hasNextChar = i < message.length();
                if (hasNextChar) {
                    final char nextChar = message.charAt(i + 1);
                    final ChatColor color = ChatColor.fromLegacyColorCodes(nextChar);
                    if (color != ChatColor.NO_COLOR) {
                        String replacement = color.toString();
                        result += replacement;
                        i++; // Increment to ignore the color code
                    } else {
                        result += c;
                    }
                }
            } else {
                result += c;
            }
        }

        return result;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return getJsonObject().toString();
    }

    protected JsonObject getJsonObject() {
        List<JsonObject> components = getComponents();

        // No message, return empty object
        if (components.isEmpty()) {
            return new JsonObject();
        }

        // Get the first element and remove it
        JsonObject mainObject = components.remove(0);

        // Append all the components
        if (!components.isEmpty()) {
            JsonArray extraArray = new JsonArray();
            for (JsonObject component : components) {
                extraArray.add(component);
            }
            mainObject.add("extra", extraArray);
        }


        return mainObject;
    }

    /**
     * Get the list of objects composing the message
     *
     * @return the list of objects composing the message
     */
    protected List<JsonObject> getComponents() {
        final List<JsonObject> objects = new ArrayList<>();
        // No message, return empty list
        if (message.isEmpty())
            return objects;

        boolean inFormat = false;
        int formatStart = 0;
        int formatEnd = 0;

        String currentColor = "";

        for (int i = 0; i < message.length(); i++) {
            // Last char or null
            Character p = i == 0 ? null : message.charAt(i - 1);
            // Current char
            char c = message.charAt(i);
            if ((p == null || (p != '/')) && c == '{' && !inFormat) {

                formatEnd = formatEnd > 0 ? formatEnd + 1 : formatEnd;
                String rawMessage = message.substring(formatEnd, i);
                if (!rawMessage.isEmpty()) {
                    objects.add(getMessagePart(MessageType.RAW, rawMessage, currentColor));
                }

                inFormat = true;
                formatStart = i;
                continue;
            } else if ((p == null || (p != '/')) && c == '}' && inFormat) {
                // Represent the custom format between the brackets
                String formatString = message.substring(formatStart + 1, i);
                if (formatString.isEmpty())
                    continue;

                inFormat = false;
                formatStart = 0;
                formatEnd = i;

                // Color component
                if (formatString.startsWith("#")) {
                    // Remove the first # character to get code
                    String colorCode = formatString.substring(1);
                    ChatColor color = ChatColor.fromName(colorCode);
                    if (color == ChatColor.NO_COLOR) {
                        // Use rgb formatting (#ffffff)
                        currentColor = "#" + colorCode;
                    } else {
                        // Use color name formatting (white)
                        currentColor = colorCode;
                    }
                    continue;
                }
                // Translatable component
                if (formatString.startsWith("@")) {
                    final String translatableCode = formatString.substring(1);
                    final boolean hasArgs = translatableCode.contains(",");
                    if (!hasArgs) {
                        objects.add(getMessagePart(MessageType.TRANSLATABLE, translatableCode, currentColor));
                    } else {
                        // Arguments parsing
                        // ex: {@translatable.key,arg1,arg2,etc}
                        final String[] split = translatableCode.split(Pattern.quote(","));
                        final String finalTranslatableCode = split[0];
                        final String[] arguments = Arrays.copyOfRange(split, 1, split.length);

                        JsonObject translatableObject = getMessagePart(MessageType.TRANSLATABLE, finalTranslatableCode, currentColor);
                        if (arguments.length > 0) {
                            JsonArray argArray = new JsonArray();
                            for (String arg : arguments) {
                                argArray.add(getMessagePart(MessageType.RAW, arg, currentColor));
                            }
                            translatableObject.add("with", argArray);
                            objects.add(translatableObject);
                        }
                    }
                    continue;
                }
                // Keybind component
                if (formatString.startsWith("&")) {
                    String keybindCode = formatString.substring(1);
                    objects.add(getMessagePart(MessageType.KEYBIND, keybindCode, currentColor));
                    continue;
                }
            }
        }

        // Add the remaining of the message as a raw message when any
        if (formatEnd < message.length()) {
            String lastRawMessage = message.substring(formatEnd + 1);
            objects.add(getMessagePart(MessageType.RAW, lastRawMessage, currentColor));
        }

        return objects;
    }

    /**
     * Get the object representing a message (raw/keybind/translatable)
     *
     * @param messageType the message type
     * @param message     the message
     * @param color       the last color
     * @return a json object representing a message
     */
    private JsonObject getMessagePart(MessageType messageType, String message, String color) {
        JsonObject object = new JsonObject();
        switch (messageType) {
            case RAW:
                object.addProperty("text", message);
                break;
            case KEYBIND:
                object.addProperty("keybind", message);
                break;
            case TRANSLATABLE:
                object.addProperty("translate", message);
                break;
        }
        if (!color.isEmpty()) {
            object.addProperty("color", color);
        }
        return object;
    }

    private enum MessageType {
        RAW, KEYBIND, TRANSLATABLE
    }

    public ColoredText appendLegacy(String message, char colorChar) {
        String legacy = toLegacy(message, colorChar);
        return appendFormat(legacy);
    }

}
