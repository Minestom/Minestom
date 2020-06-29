package net.minestom.server.chat;

import net.minestom.server.utils.validate.Check;

import java.util.HashMap;
import java.util.Map;

public class ChatColor {

    public static final ChatColor NO_COLOR = new ChatColor();

    public static final ChatColor BLACK = fromRGB(0, 0, 0, 0);
    public static final ChatColor DARK_BLUE = fromRGB(0, 0, 170, 1);
    public static final ChatColor DARK_GREEN = fromRGB(0, 170, 0, 2);
    public static final ChatColor DARK_CYAN = fromRGB(0, 170, 170, 3);
    public static final ChatColor DARK_RED = fromRGB(170, 0, 0, 4);
    public static final ChatColor PURPLE = fromRGB(170, 0, 170, 5);
    public static final ChatColor GOLD = fromRGB(255, 170, 0, 6);
    public static final ChatColor GRAY = fromRGB(170, 170, 170, 7);
    public static final ChatColor DARK_GRAY = fromRGB(85, 85, 85, 8);
    public static final ChatColor BLUE = fromRGB(85, 85, 255, 9);
    public static final ChatColor BRIGHT_GREEN = fromRGB(85, 255, 85, 10);
    public static final ChatColor CYAN = fromRGB(85, 255, 255, 11);
    public static final ChatColor RED = fromRGB(255, 85, 85, 12);
    public static final ChatColor PINK = fromRGB(255, 85, 255, 13);
    public static final ChatColor YELLOW = fromRGB(255, 255, 85, 14);
    public static final ChatColor WHITE = fromRGB(255, 255, 255, 15);
    private static Map<String, ChatColor> colorCode = new HashMap<>();
    private static Map<Character, ChatColor> legacyColorCodesMap = new HashMap<>();

    static {
        colorCode.put("black", BLACK);
        colorCode.put("dark_blue", DARK_BLUE);
        colorCode.put("dark_green", DARK_GREEN);
        colorCode.put("dark_cyan", DARK_CYAN);
        colorCode.put("dark_red", DARK_RED);
        colorCode.put("purple", PURPLE);
        colorCode.put("gold", GOLD);
        colorCode.put("gray", GRAY);
        colorCode.put("dark_gray", DARK_GRAY);
        colorCode.put("blue", BLUE);
        colorCode.put("bright_green", BRIGHT_GREEN);
        colorCode.put("cyan", CYAN);
        colorCode.put("red", RED);
        colorCode.put("pink", PINK);
        colorCode.put("yellow", YELLOW);
        colorCode.put("white", WHITE);

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

    private ChatColor(int r, int g, int b, int id) {
        this.empty = false;
        this.red = r;
        this.green = g;
        this.blue = b;
        this.id = id;
    }

    private ChatColor() {
        this.empty = true;
    }

    public static ChatColor fromRGB(int r, int g, int b) {
        return fromRGB(r, g, b, -1);
    }

    private static ChatColor fromRGB(int r, int g, int b, int id) {
        return new ChatColor(r, g, b, id);
    }

    public static ChatColor fromName(String name) {
        return colorCode.getOrDefault(name.toLowerCase(), NO_COLOR);
    }

    public static ChatColor fromLegacyColorCodes(char colorCode) {
        return legacyColorCodesMap.getOrDefault(colorCode, NO_COLOR);
    }

    public boolean isEmpty() {
        return empty;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getId() {
        Check.stateCondition(id == -1, "Please use one of the ChatColor constant instead");
        return id;
    }

    @Override
    public String toString() {
        if (empty)
            return "";

        String redH = Integer.toHexString(red);
        String greenH = Integer.toHexString(green);
        String blueH = Integer.toHexString(blue);
        return "{#" + redH + greenH + blueH + "}";
    }
}
