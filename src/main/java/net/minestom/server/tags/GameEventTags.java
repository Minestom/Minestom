package net.minestom.server.tags;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public final class GameEventTags {

    public static final @NotNull Tag<@NotNull GameEvent> VIBRATIONS = get("vibrations");
    public static final @NotNull Tag<@NotNull GameEvent> IGNORE_VIBRATIONS_SNEAKING = get("ignore_vibrations_sneaking");

    private static Tag<GameEvent> get(final String name) {
        return MinecraftServer.getTagManager().get(TagType.GAME_EVENTS, "minecraft:" + name);
    }

    private GameEventTags() {
    }
}
