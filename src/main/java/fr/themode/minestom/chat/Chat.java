package fr.themode.minestom.chat;

import club.thectm.minecraft.text.LegacyText;
import club.thectm.minecraft.text.TextObject;

/**
 * Thank for the minecraft-text library made by rbrick:
 * https://github.com/ctmclub/minecraft-text
 */
public class Chat {

    public static final char COLOR_CHAR = (char) 0xA7; // Represent the character '§'

    public static TextObject legacyText(String text, char colorChar) {
        return LegacyText.fromLegacy(text, colorChar);
    }

    public static TextObject legacyText(String text) {
        return legacyText(text, COLOR_CHAR);
    }

    public static String legacyTextString(String text, char colorChar) {
        return legacyText(text, colorChar).toJson().toString();
    }

    public static String legacyTextString(String text) {
        return legacyText(text).toJson().toString();
    }

    public static String uncoloredLegacyText(String text) {
        return text.replace(String.valueOf(COLOR_CHAR), "");
    }
}
