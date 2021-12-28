package net.minestom.server.tags;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public final class GameEventGameTags {

    public static final @NotNull GameTag<@NotNull GameEvent> VIBRATIONS = get("vibrations");
    public static final @NotNull GameTag<@NotNull GameEvent> IGNORE_VIBRATIONS_SNEAKING = get("ignore_vibrations_sneaking");

    private static GameTag<GameEvent> get(final String name) {
        return MinecraftServer.getTagManager().get(GameTagType.GAME_EVENTS, "minecraft:" + name);
    }

    private GameEventGameTags() {
    }
}
