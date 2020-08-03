package net.minestom.server.chat;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.utils.validate.Check;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent a color in a text
 */
public class ChatColor {

    // Special
    public static final ChatColor NO_COLOR = new ChatColor();
    public static final ChatColor RESET = new ChatColor("reset");
    public static final ChatColor BOLD = new ChatColor("bold");
    public static final ChatColor ITALIC = new ChatColor("italic");
    public static final ChatColor UNDERLINED = new ChatColor("underlined");
    public static final ChatColor STRIKETHROUGH = new ChatColor("strikethrough");
    public static final ChatColor OBFUSCATED = new ChatColor("obfuscated");

    // Color
    public static final ChatColor BLACK = fromRGB(0, 0, 0, 0, "black");
    public static final ChatColor DARK_BLUE = fromRGB(0, 0, 170, 1, "dark_blue");
    public static final ChatColor DARK_GREEN = fromRGB(0, 170, 0, 2, "dark_green");
    public static final ChatColor DARK_CYAN = fromRGB(0, 170, 170, 3, "dark_cyan");
    public static final ChatColor DARK_RED = fromRGB(170, 0, 0, 4, "dark_red");
    public static final ChatColor PURPLE = fromRGB(170, 0, 170, 5, "purple");
    public static final ChatColor GOLD = fromRGB(255, 170, 0, 6, "gold");
    public static final ChatColor GRAY = fromRGB(170, 170, 170, 7, "gray");
    public static final ChatColor DARK_GRAY = fromRGB(85, 85, 85, 8, "dark_gray");
    public static final ChatColor BLUE = fromRGB(85, 85, 255, 9, "blue");
    public static final ChatColor BRIGHT_GREEN = fromRGB(85, 255, 85, 10, "green");
    public static final ChatColor CYAN = fromRGB(85, 255, 255, 11, "cyan");
    public static final ChatColor RED = fromRGB(255, 85, 85, 12, "red");
    public static final ChatColor PINK = fromRGB(255, 85, 255, 13, "pink");
    public static final ChatColor YELLOW = fromRGB(255, 255, 85, 14, "yellow");
    public static final ChatColor WHITE = fromRGB(255, 255, 255, 15, "white");

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
    private int red, green, blue;
    private int id;

    private String codeName;

    private boolean special;

    private ChatColor(int r, int g, int b, int id, String codeName) {
        this.empty = false;
        this.red = r;
        this.green = g;
        this.blue = b;
        this.id = id;
        this.codeName = codeName;
    }

    private ChatColor(String codeName) {
        this.codeName = codeName;
        this.special = true;
    }

    private ChatColor() {
        this.empty = true;
        this.special = true;
    }

    /**
     * Create an RGB color
     *
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @return a chat color with the specified RGB color
     */
    public static ChatColor fromRGB(int r, int g, int b) {
        return fromRGB(r, g, b, -1, null);
    }

    private static ChatColor fromRGB(int r, int g, int b, int id, String codeName) {
        return new ChatColor(r, g, b, id, codeName);
    }

    /**
     * Get a color based on its name (eg: white, black, aqua, etc...)
     *
     * @param name the color name
     * @return the color associated with the name, {@link #NO_COLOR} if not found
     */
    public static ChatColor fromName(String name) {
        return colorCode.getOrDefault(name.toLowerCase(), NO_COLOR);
    }

    /**
     * Get a color based on its numerical id (0;15)
     *
     * @param id the id of the color
     * @return the color associated with the id, {@link #NO_COLOR} if not found
     */
    public static ChatColor fromId(int id) {
        return idColorMap.getOrDefault(id, NO_COLOR);
    }

    /**
     * Get a color based on its legacy color code (eg: 1, 2, 3,... f)
     *
     * @param colorCode the color legacy code
     * @return the color associated with the code
     */
    public static ChatColor fromLegacyColorCodes(char colorCode) {
        return legacyColorCodesMap.getOrDefault(colorCode, NO_COLOR);
    }

    public boolean isEmpty() {
        return empty;
    }

    /**
     * Get the red component of the color
     *
     * @return the red component of the color
     */
    public int getRed() {
        return red;
    }

    /**
     * Get the green component of the color
     *
     * @return the green component of the color
     */
    public int getGreen() {
        return green;
    }

    /**
     * Get the blue component of the color
     *
     * @return the blue component of the color
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Get if the color is special (eg: no color, bold, reset, etc...)
     *
     * @return true if the color is special, false otherwise
     */
    public boolean isSpecial() {
        return special;
    }

    /**
     * Get the code name is the color is "special"
     *
     * @return the special code name
     */
    protected String getCodeName() {
        return codeName;
    }

    public int getId() {
        Check.stateCondition(id == -1, "Please use one of the ChatColor constant instead");
        return id;
    }

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
            // RGB color
            String redH = Integer.toHexString(red);
            if (redH.length() == 1)
                redH = "0" + redH;

            String greenH = Integer.toHexString(green);
            if (greenH.length() == 1)
                greenH = "0" + greenH;

            String blueH = Integer.toHexString(blue);
            if (blueH.length() == 1)
                blueH = "0" + blueH;

            code = redH + greenH + blueH;
        }

        return header + code + footer;
    }
}
