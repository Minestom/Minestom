package net.minestom.server.chat;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.format.*;
import net.minestom.server.color.Color;
import net.minestom.server.color.DyeColor;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a color in a text. You can either use one of the pre-made colors
 * or make your own using RGB. {@link ChatColor#fromRGB(byte, byte, byte)}.
 * <p>
 * Immutable class.
 * @deprecated For chat colors, use {@link TextColor} or {@link NamedTextColor}. For styles, use {@link TextDecoration}.
 * For colors in other contexts, see {@link Color} or {@link DyeColor}.
 */
@Deprecated
public final class ChatColor implements StyleBuilderApplicable {

    // Special
    public static final ChatColor NO_COLOR = new ChatColor();
    public static final ChatColor RESET = new ChatColor("reset");
    public static final ChatColor BOLD = new ChatColor("bold");
    public static final ChatColor ITALIC = new ChatColor("italic");
    public static final ChatColor UNDERLINED = new ChatColor("underlined");
    public static final ChatColor STRIKETHROUGH = new ChatColor("strikethrough");
    public static final ChatColor OBFUSCATED = new ChatColor("obfuscated");

    // Color
    public static final ChatColor BLACK = fromRGB((byte) 0, (byte) 0, (byte) 0, 0, "black");
    public static final ChatColor DARK_BLUE = fromRGB((byte) 0, (byte) 0, (byte) 170, 1, "dark_blue");
    public static final ChatColor DARK_GREEN = fromRGB((byte) 0, (byte) 170, (byte) 0, 2, "dark_green");
    public static final ChatColor DARK_CYAN = fromRGB((byte) 0, (byte) 170, (byte) 170, 3, "dark_aqua");
    public static final ChatColor DARK_RED = fromRGB((byte) 170, (byte) 0, (byte) 0, 4, "dark_red");
    public static final ChatColor PURPLE = fromRGB((byte) 170, (byte) 0, (byte) 170, 5, "dark_purple");
    public static final ChatColor GOLD = fromRGB((byte) 255, (byte) 170, (byte) 0, 6, "gold");
    public static final ChatColor GRAY = fromRGB((byte) 170, (byte) 170, (byte) 170, 7, "gray");
    public static final ChatColor DARK_GRAY = fromRGB((byte) 85, (byte) 85, (byte) 85, 8, "dark_gray");
    public static final ChatColor BLUE = fromRGB((byte) 85, (byte) 85, (byte) 255, 9, "blue");
    public static final ChatColor BRIGHT_GREEN = fromRGB((byte) 85, (byte) 255, (byte) 85, 10, "green");
    public static final ChatColor CYAN = fromRGB((byte) 85, (byte) 255, (byte) 255, 11, "aqua");
    public static final ChatColor RED = fromRGB((byte) 255, (byte) 85, (byte) 85, 12, "red");
    public static final ChatColor PINK = fromRGB((byte) 255, (byte) 85, (byte) 255, 13, "light_purple");
    public static final ChatColor YELLOW = fromRGB((byte) 255, (byte) 255, (byte) 85, 14, "yellow");
    public static final ChatColor WHITE = fromRGB((byte) 255, (byte) 255, (byte) 255, 15, "white");

    private static final Int2ObjectMap<ChatColor> idColorMap = new Int2ObjectOpenHashMap<>();
    private static final Map<String, ChatColor> colorCode = new HashMap<>();
    private static final Char2ObjectMap<ChatColor> legacyColorCodesMap = new Char2ObjectOpenHashMap<>();

    static {
        idColorMap.put(0, BLACK);
        idColorMap.put(1, DARK_BLUE);
        idColorMap.put(2, DARK_GREEN);
        idColorMap.put(3, DARK_CYAN);
        idColorMap.put(4, DARK_RED);
        idColorMap.put(5, PURPLE);
        idColorMap.put(6, GOLD);
        idColorMap.put(7, GRAY);
        idColorMap.put(8, DARK_GRAY);
        idColorMap.put(9, BLUE);
        idColorMap.put(10, BRIGHT_GREEN);
        idColorMap.put(11, CYAN);
        idColorMap.put(12, RED);
        idColorMap.put(13, PINK);
        idColorMap.put(14, YELLOW);
        idColorMap.put(15, WHITE);

        colorCode.put("reset", RESET);
        colorCode.put("bold", BOLD);
        colorCode.put("italic", ITALIC);
        colorCode.put("underlined", UNDERLINED);
        colorCode.put("strikethrough", STRIKETHROUGH);
        colorCode.put("obfuscated", OBFUSCATED);

        colorCode.put("black", BLACK);
        colorCode.put("dark_blue", DARK_BLUE);
        colorCode.put("dark_green", DARK_GREEN);
        colorCode.put("dark_aqua", DARK_CYAN);
        colorCode.put("dark_red", DARK_RED);
        colorCode.put("dark_purple", PURPLE);
        colorCode.put("gold", GOLD);
        colorCode.put("gray", GRAY);
        colorCode.put("dark_gray", DARK_GRAY);
        colorCode.put("blue", BLUE);
        colorCode.put("green", BRIGHT_GREEN);
        colorCode.put("aqua", CYAN);
        colorCode.put("red", RED);
        colorCode.put("light_purple", PINK);
        colorCode.put("yellow", YELLOW);
        colorCode.put("white", WHITE);

        legacyColorCodesMap.put('k', OBFUSCATED);
        legacyColorCodesMap.put('l', BOLD);
        legacyColorCodesMap.put('m', STRIKETHROUGH);
        legacyColorCodesMap.put('n', UNDERLINED);
        legacyColorCodesMap.put('o', ITALIC);
        legacyColorCodesMap.put('r', RESET);

        legacyColorCodesMap.put('0', BLACK);
        legacyColorCodesMap.put('1', DARK_BLUE);
        legacyColorCodesMap.put('2', DARK_GREEN);
        legacyColorCodesMap.put('3', DARK_CYAN);
        legacyColorCodesMap.put('4', DARK_RED);
        legacyColorCodesMap.put('5', PURPLE);
        legacyColorCodesMap.put('6', GOLD);
        legacyColorCodesMap.put('7', GRAY);
        legacyColorCodesMap.put('8', DARK_GRAY);
        legacyColorCodesMap.put('9', BLUE);
        legacyColorCodesMap.put('a', BRIGHT_GREEN);
        legacyColorCodesMap.put('b', CYAN);
        legacyColorCodesMap.put('c', RED);
        legacyColorCodesMap.put('d', PINK);
        legacyColorCodesMap.put('e', YELLOW);
        legacyColorCodesMap.put('f', WHITE);
    }

    private boolean empty;
    private final byte red, green, blue;
    private final int id;

    private final String codeName;

    private final boolean special;

    private ChatColor(byte r, byte g, byte b, int id, @Nullable String codeName, boolean special) {
        this.empty = false;
        this.red = r;
        this.green = g;
        this.blue = b;
        this.id = id;
        this.codeName = codeName;
        this.special = special;
    }

    private ChatColor(byte r, byte g, byte b, int id, @Nullable String codeName) {
        this(r, g, b, id, codeName, false);
    }

    private ChatColor(String codeName) {
        this((byte) 0, (byte) 0, (byte) 0, 0, codeName, true);
    }

    private ChatColor() {
        this((byte) 0, (byte) 0, (byte) 0, 0, null, true);
        this.empty = true;
    }

    /**
     * Creates an RGB color.
     *
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @return a chat color with the specified RGB color
     */
    @NotNull
    public static ChatColor fromRGB(byte r, byte g, byte b) {
        return fromRGB(r, g, b, -1, null);
    }

    @NotNull
    private static ChatColor fromRGB(byte r, byte g, byte b, int id, String codeName) {
        return new ChatColor(r, g, b, id, codeName);
    }

    /**
     * Gets a color based on its name (eg: white, black, aqua, etc...).
     *
     * @param name the color name
     * @return the color associated with the name, {@link #NO_COLOR} if not found
     */
    @NotNull
    public static ChatColor fromName(@NotNull String name) {
        return colorCode.getOrDefault(name.toLowerCase(), NO_COLOR);
    }

    /**
     * Gets a color based on its numerical id (0;15).
     *
     * @param id the id of the color
     * @return the color associated with the id, {@link #NO_COLOR} if not found
     */
    @NotNull
    public static ChatColor fromId(int id) {
        return idColorMap.getOrDefault(id, NO_COLOR);
    }

    /**
     * Gets a color based on its legacy color code (eg: 1, 2, 3,... f).
     *
     * @param colorCode the color legacy code
     * @return the color associated with the code, {@link #NO_COLOR} if not found
     */
    @NotNull
    public static ChatColor fromLegacyColorCodes(char colorCode) {
        return legacyColorCodesMap.getOrDefault(colorCode, NO_COLOR);
    }

    /**
     * Gets a collection of all chat colors
     * @return a collection of all chat colors
     */
    @NotNull
    public static Collection<ChatColor> values() {
        return colorCode.values();
    }

    public boolean isEmpty() {
        return empty;
    }

    /**
     * Gets the red component of the color.
     *
     * @return the red component of the color
     */
    public byte getRed() {
        return red;
    }

    /**
     * Gets the green component of the color.
     *
     * @return the green component of the color
     */
    public byte getGreen() {
        return green;
    }

    /**
     * Gets the blue component of the color.
     *
     * @return the blue component of the color
     */
    public byte getBlue() {
        return blue;
    }

    /**
     * Gets if the color is special (eg: no color, bold, reset, etc...).
     *
     * @return true if the color is special, false otherwise
     */
    public boolean isSpecial() {
        return special;
    }

    /**
     * Gets the code name.
     *
     * @return the color code name, null if not any
     */
    @Nullable
    public String getCodeName() {
        return codeName;
    }

    /**
     * Gets the color id, only present if this color has been retrieved from {@link ChatColor} constants.
     * <p>
     * Should only be used for some special packets which require it.
     *
     * @return the color id
     * @throws IllegalStateException if the color is not from the class constants
     */
    public int getId() {
        Check.stateCondition(id == -1, "Please use one of the ChatColor constant instead");
        return id;
    }

    /**
     * Gets the Adventure text color from this chat color.
     * @return the text color
     */
    public @NotNull TextColor asTextColor() {
        return TextColor.color(red, blue, green);
    }

    public @NotNull Color asColor() {
        return new Color(red, green, blue);
    }

    @NotNull
    @Override
    public String toString() {
        if (empty)
            return "";

        final String header = "{#";
        final String footer = "}";

        String code;

        if (codeName != null) {
            // color or special code (white/red/reset/bold/etc...)
            code = codeName;
        } else {
            // RGB color (special code not set)
            final int color = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;

            code = Integer.toHexString(color);
        }

        return header + code + footer;
    }

    @Override
    @Contract(mutates = "param")
    public void styleApply(Style.@NotNull Builder style) {
        if (this.isEmpty()) {
            style.color(NamedTextColor.WHITE);
        } else if (Objects.equals(this.codeName, "reset")) {
            style.color(NamedTextColor.WHITE);

            for (TextDecoration value : TextDecoration.NAMES.values()) {
                style.decoration(value, TextDecoration.State.FALSE);
            }
        } else if (this.isSpecial() && this.codeName != null) {
            TextDecoration decoration = TextDecoration.NAMES.value(this.codeName);

            if (decoration != null) {
                style.decorate(decoration);
            }
        } else {
            style.color(TextColor.color(this.red, this.green, this.blue));
        }
    }
}
