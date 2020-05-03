package net.minestom.server.chat;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.text.serializer.plain.PlainComponentSerializer;

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

    public static TextComponent legacyText(String text, char colorChar) {
        return LegacyComponentSerializer.legacyLinking().deserialize(text, colorChar);
    }

    public static TextComponent legacyText(String text) {
        return legacyText(text, COLOR_CHAR);
    }

    public static String legacyTextString(String text) {
        // TODO: Find out where this is used and ensure this is correct
        return GsonComponentSerializer.INSTANCE.serialize(legacyText(text, COLOR_CHAR));
    }

    public static String uncoloredLegacyText(String text) {
        // TODO: Find out where this is used and ensure this is correct
        // TODO: Improve this, I'm not sure the old method is correct
        return PlainComponentSerializer.INSTANCE.serialize(legacyText(text));
    }

}
