package net.minestom.server.chat;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Thank for the minecraft-text library made by rbrick:
 * https://github.com/ctmclub/minecraft-text
 */
public class Chat {

    public static final char COLOR_CHAR = (char) 0xA7; // Represent the character '§'

    public static String toJsonString(Component component) {
        return GsonComponentSerializer.INSTANCE.serialize(component);
    }

    public static Component fromJsonString(String json) {
        return GsonComponentSerializer.INSTANCE.deserialize(json);
    }

    public static String toLegacyText(Component component) {
        return LegacyComponentSerializer.legacyLinking().serialize(component);
    }

    public static TextComponent toLegacyText(String text, char colorChar) {
        return LegacyComponentSerializer.legacyLinking().deserialize(text, colorChar);
    }

    public static TextComponent toLegacyText(String text) {
        return toLegacyText(text, COLOR_CHAR);
    }
}
