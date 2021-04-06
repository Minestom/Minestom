package net.minestom.server.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a text with one or multiple colors.
 * <p>
 * Used when the message can contain colors but not events like in {@link RichMessage}.
 * <p>
 * To create one, you simply call one of the static methods like {@link #of(ChatColor, String)},
 * you can then continue to append text with {@link #append(ChatColor, String)}.
 * @deprecated Use {@link Component#text(String, TextColor)}
 */
@Deprecated
public class ColoredText extends JsonMessage {

    private static final char SEPARATOR_START = '{';
    private static final char SEPARATOR_END = '}';
    private static final String COLOR_PREFIX = "#";
    private static final String TRANSLATABLE_PREFIX = "@";
    private static final String KEYBIND_PREFIX = "&";

    /**
     * The raw text StringBuilder.
     * Its a single StringBuilder instance for easier and faster concatenation.
     */
    private final StringBuilder message = new StringBuilder();

    /**
     * Creates a colored text.
     * <p>
     * You need to use one of the static methods instead of the constructor.
     *
     * @param message the content of the text
     * @see #of(String) to create a colored text
     */
    private ColoredText(@NotNull String message) {
        this.message.append(message);
        refreshUpdate();
    }

    /**
     * Creates a {@link ColoredText}.
     *
     * @param color   the text color
     * @param message the text message
     * @return the created {@link ColoredText}
     */
    public static ColoredText of(@NotNull ChatColor color, @NotNull String message) {
        return new ColoredText(color + message);
    }

    /**
     * Creates a {@link ColoredText}.
     *
     * @param message the text message
     * @return the created {@link ColoredText}
     */
    @NotNull
    public static ColoredText of(@NotNull String message) {
        return of(ChatColor.WHITE, message);
    }

    /**
     * Creates a {@link ColoredText} with a legacy text.
     *
     * @param message   the text message
     * @param colorChar the char used before the color code
     * @return the created {@link ColoredText}
     */
    @NotNull
    public static ColoredText ofLegacy(@NotNull String message, char colorChar) {
        final String legacy = legacyToFormat(message, colorChar);
        return of(legacy);
    }

    /**
     * Appends the text.
     *
     * @param color   the text color
     * @param message the text message
     * @return this {@link ColoredText}
     */
    @NotNull
    public ColoredText append(@NotNull ChatColor color, @NotNull String message) {
        this.message.append(color).append(message);
        refreshUpdate();
        return this;
    }

    /**
     * Appends the text.
     *
     * @param message the text message
     * @return this {@link ColoredText}
     */
    @NotNull
    public ColoredText append(@NotNull String message) {
        return append(ChatColor.NO_COLOR, message);
    }

    /**
     * Adds legacy text.
     *
     * @param message   the legacy text
     * @param colorChar the char used before the color code
     * @return this {@link ColoredText}
     */
    @NotNull
    public ColoredText appendLegacy(@NotNull String message, char colorChar) {
        final String legacy = legacyToFormat(message, colorChar);
        return of(legacy);
    }

    /**
     * Gets the raw text.
     *
     * @return the raw text
     * @see #toString() for the Json representation
     */
    @NotNull
    public String getMessage() {
        return message.toString();
    }

    /**
     * Gets the Json representation of this colored text.
     * <p>
     * Used to "compile" the message, retrieved with {@link #toString()}.
     *
     * @return the Json representation of the text
     */
    @NotNull
    @Override
    public JsonObject getJsonObject() {
        final List<JsonObject> components = getComponents();

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
     * Gets the list of objects composing the message.
     *
     * @return the list of objects composing the message
     */
    @NotNull
    protected List<JsonObject> getComponents() {
        final List<JsonObject> objects = new ArrayList<>();
        // No message, return empty list
        if (getMessage().isEmpty())
            return objects;

        boolean inFormat = false;
        int formatStart = 0;
        int formatEnd = 0;

        String currentColor = "";
        SpecialComponentContainer specialComponentContainer = new SpecialComponentContainer();
        for (int i = 0; i < message.length(); i++) {
            // Last char or null
            final Character p = i == 0 ? null : message.charAt(i - 1);
            // Current char
            final char c = message.charAt(i);
            if (c == SEPARATOR_START && !inFormat) {

                formatEnd = formatEnd > 0 ? formatEnd + 1 : formatEnd;
                final String rawMessage = message.substring(formatEnd, i);
                if (!rawMessage.isEmpty()) {
                    objects.add(getMessagePart(MessageType.RAW, rawMessage, currentColor, specialComponentContainer));
                }

                inFormat = true;
                formatStart = i;
                continue;
            } else if (c == SEPARATOR_END && inFormat) {
                // Represent the custom format between the brackets
                final String formatString = message.substring(formatStart + 1, i);
                if (formatString.isEmpty())
                    continue;

                inFormat = false;
                //formatStart = 0;
                formatEnd = i;

                // Color component
                if (formatString.startsWith(COLOR_PREFIX)) {
                    // Remove the first # character to get code
                    final String colorCode = formatString.substring(1);
                    final ChatColor color = ChatColor.fromName(colorCode);
                    if (color == ChatColor.NO_COLOR) {
                        // Use rgb formatting (#ffffff)
                        currentColor = COLOR_PREFIX + colorCode;
                    } else if (color.isSpecial()) {
                        // Check for special color (reset/bold/etc...)
                        if (color == ChatColor.RESET) {
                            // Remove all additional component
                            currentColor = "";
                            specialComponentContainer.reset();
                        } else if (color == ChatColor.BOLD) {
                            specialComponentContainer.bold = true;
                        } else if (color == ChatColor.ITALIC) {
                            specialComponentContainer.italic = true;
                        } else if (color == ChatColor.UNDERLINED) {
                            specialComponentContainer.underlined = true;
                        } else if (color == ChatColor.STRIKETHROUGH) {
                            specialComponentContainer.strikethrough = true;
                        } else if (color == ChatColor.OBFUSCATED) {
                            specialComponentContainer.obfuscated = true;
                        }
                    } else {
                        // Use color name formatting (white)
                        currentColor = colorCode;
                    }
                    continue;
                }
                // Translatable component
                if (formatString.startsWith(TRANSLATABLE_PREFIX)) {
                    final String translatableCode = formatString.substring(1);
                    final boolean hasArgs = translatableCode.contains(",");
                    if (!hasArgs) {
                        // Without argument
                        // ex: {@translatable.key}
                        objects.add(getMessagePart(MessageType.TRANSLATABLE, translatableCode, currentColor, specialComponentContainer));
                    } else {
                        // Arguments parsing
                        // ex: {@translatable.key,arg1,arg2,etc}
                        final String[] split = translatableCode.split(Pattern.quote(","));
                        final String finalTranslatableCode = split[0];
                        final String[] arguments = Arrays.copyOfRange(split, 1, split.length);

                        JsonObject translatableObject = getMessagePart(MessageType.TRANSLATABLE, finalTranslatableCode, currentColor, specialComponentContainer);
                        if (arguments.length > 0) {
                            JsonArray argArray = new JsonArray();
                            for (String arg : arguments) {
                                argArray.add(getMessagePart(MessageType.RAW, arg, currentColor, specialComponentContainer));
                            }
                            translatableObject.add("with", argArray);
                            objects.add(translatableObject);
                        }
                    }
                    continue;
                }
                // Keybind component
                if (formatString.startsWith(KEYBIND_PREFIX)) {
                    // ex: {&key.drop}
                    final String keybindCode = formatString.substring(1);
                    objects.add(getMessagePart(MessageType.KEYBIND, keybindCode, currentColor, specialComponentContainer));
                    continue;
                }

                // Was not a component, add the encapsulated string
                final String current = message.substring(formatStart, i + 1);
                objects.add(getMessagePart(MessageType.RAW, current, currentColor, specialComponentContainer));
            }
        }

        // Add the remaining of the message as a raw message when any
        if (formatEnd < message.length()) {
            final String lastRawMessage = message.substring(formatEnd + 1);
            // if 'objects' is empty, an empty message is required for the client to understand the final JSON
            if (!lastRawMessage.isEmpty() || objects.isEmpty()) {
                objects.add(getMessagePart(MessageType.RAW, lastRawMessage, currentColor, specialComponentContainer));
            }
        }

        return objects;
    }

    /**
     * Gets the object representing a message (raw/keybind/translatable).
     *
     * @param messageType the message type
     * @param message     the message
     * @param color       the last color
     * @return a json object representing a message
     */
    private JsonObject getMessagePart(@NotNull MessageType messageType, @NotNull String message, @NotNull String color,
                                      @NotNull SpecialComponentContainer specialComponentContainer) {
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


        object.addProperty("bold", getBoolean(specialComponentContainer.bold));
        object.addProperty("italic", getBoolean(specialComponentContainer.italic));
        object.addProperty("underlined", getBoolean(specialComponentContainer.underlined));
        object.addProperty("strikethrough", getBoolean(specialComponentContainer.strikethrough));
        object.addProperty("obfuscated", getBoolean(specialComponentContainer.obfuscated));

        return object;
    }

    @NotNull
    private String getBoolean(boolean value) {
        return value ? "true" : "false";
    }

    /**
     * Converts a legacy text to our format which can be used by {@link #of(String)} etc...
     * <p>
     * eg: "&fHey" -> "{#white}Hey"
     *
     * @param message   the legacy text
     * @param colorChar the char used before the color code
     * @return the converted legacy text
     */
    @NotNull
    private static String legacyToFormat(@NotNull String message, char colorChar) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            final char c = message.charAt(i);
            if (c == colorChar) {
                final char nextChar = message.charAt(i + 1);
                final ChatColor color = ChatColor.fromLegacyColorCodes(nextChar);
                if (color != ChatColor.NO_COLOR) {
                    final String replacement = color.toString();
                    result.append(replacement);
                    i++; // Increment to ignore the color code
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Represents an element which can change based on the client which receive the text.
     */
    private enum MessageType {
        RAW, KEYBIND, TRANSLATABLE
    }

    /**
     * Used to keep a "color" state in the text.
     */
    private static class SpecialComponentContainer {
        boolean bold = false;

        boolean italic = false;

        boolean underlined = false;

        boolean strikethrough = false;

        boolean obfuscated = false;

        private void reset() {
            this.bold = false;

            this.italic = false;

            this.underlined = false;

            this.strikethrough = false;

            this.obfuscated = false;
        }
    }

}
