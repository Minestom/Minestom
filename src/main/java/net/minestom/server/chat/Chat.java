package net.minestom.server.chat;

import net.kyori.text.Component;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

public class Chat {

    public static final char COLOR_CHAR = (char) 0xA7; // Represent the character '§'

    public static Component fromJsonString(String json) {
        return GsonComponentSerializer.INSTANCE.deserialize(json);
    }

    public static String toLegacyText(Component component) {
        return LegacyComponentSerializer.legacyLinking().serialize(component);
    }
}
