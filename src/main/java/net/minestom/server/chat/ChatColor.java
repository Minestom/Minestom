package net.minestom.server.chat;

import java.util.HashMap;
import java.util.Map;

public class ChatColor {

    public static final ChatColor NO_COLOR = new ChatColor();

    public static final ChatColor BLACK = new ChatColor("black", 0);
    public static final ChatColor DARK_BLUE = new ChatColor("dark_blue", 1);
    public static final ChatColor DARK_GREEN = new ChatColor("dark_green", 2);
    public static final ChatColor DARK_CYAN = new ChatColor("dark_cyan", 3);
    public static final ChatColor DARK_RED = new ChatColor("dark_red", 4);
    public static final ChatColor PURPLE = new ChatColor("dark_purple", 5);
    public static final ChatColor GOLD = new ChatColor("gold", 6);
    public static final ChatColor GRAY = new ChatColor("gray", 7);
    public static final ChatColor DARK_GRAY = new ChatColor("dark_gray", 8);
    public static final ChatColor BLUE = new ChatColor("blue", 9);
    public static final ChatColor BRIGHT_GREEN = new ChatColor("green", 10);
    public static final ChatColor CYAN = new ChatColor("aqua", 11);
    public static final ChatColor RED = new ChatColor("red", 12);
    public static final ChatColor PINK = new ChatColor("light_purple", 13);
    public static final ChatColor YELLOW = new ChatColor("yellow", 14);
    public static final ChatColor WHITE = new ChatColor("white", 15);

    /*public static final ChatColor BLACK = fromRGB(0, 0, 0);
    public static final ChatColor DARK_BLUE = fromRGB(0, 0, 170);
    public static final ChatColor DARK_GREEN = fromRGB(0, 170, 0);
    public static final ChatColor DARK_CYAN = fromRGB(0, 170, 170);
    public static final ChatColor DARK_RED = fromRGB(170, 0, 0);
    public static final ChatColor PURPLE = fromRGB(170, 0, 170);
    public static final ChatColor GOLD = fromRGB(255, 170, 0);
    public static final ChatColor GRAY = fromRGB(170, 170, 170);
    public static final ChatColor DARK_GRAY = fromRGB(85, 85, 85);
    public static final ChatColor BLUE = fromRGB(85, 85, 255);
    public static final ChatColor BRIGHT_GREEN = fromRGB(85, 255, 85);
    public static final ChatColor CYAN = fromRGB(85, 255, 255);
    public static final ChatColor RED = fromRGB(255, 85, 85);
    public static final ChatColor PINK = fromRGB(255, 85, 255);
    public static final ChatColor YELLOW = fromRGB(255, 255, 85);
    public static final ChatColor WHITE = fromRGB(255, 255, 255);*/
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
    // 1.15
    private String name;

    // 1.15
    private ChatColor(String name, int id) {
        this.name = name;
        this.id = id;
    }

    // 1.16
    private ChatColor(int r, int g, int b) {
        this.empty = false;
        this.red = r;
        this.green = g;
        this.blue = b;
    }

    private ChatColor() {
        this.empty = true;
    }

    public static ChatColor fromRGB(int r, int g, int b) {
        return new ChatColor(r, g, b);
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
        return id;
    }

    // 1.15
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        // 1.15
        if (name != null)
            return "{#" + name + "}";
        // 1.16
        if (empty)
            return "";

        String redH = Integer.toHexString(red);
        String greenH = Integer.toHexString(green);
        String blueH = Integer.toHexString(blue);
        return "{#" + redH + greenH + blueH + "}";
    }
}
