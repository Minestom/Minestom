package net.minestom.server.chat;

import net.kyori.text.Component;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

public class Chat {

    public static Component fromJsonString(String json) {
        return GsonComponentSerializer.INSTANCE.deserialize(json);
    }

    public static String toLegacyText(Component component) {
        return LegacyComponentSerializer.legacyLinking().serialize(component);
    }
}
