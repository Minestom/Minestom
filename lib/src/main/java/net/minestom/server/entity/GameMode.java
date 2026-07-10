package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;

import static net.minestom.server.network.NetworkBuffer.BYTE;

/**
 * Represents the game mode of a player.
 * <p>
 * Can be set with {@link Player#setGameMode(GameMode)}.
 */
public enum GameMode {
    SURVIVAL(false, false, false),
    CREATIVE(true, true, true),
    ADVENTURE(false, false, false),
    SPECTATOR(true, true, false);

    private final boolean allowFlying;
    private final boolean invulnerable;
    private final boolean instantBreak;

    GameMode(boolean allowFlying, boolean invulnerable, boolean instantBreak) {
        this.allowFlying = allowFlying;
        this.invulnerable = invulnerable;
        this.instantBreak = instantBreak;
    }

    public boolean allowFlying() {
        return allowFlying;
    }

    public boolean invulnerable() {
        return invulnerable;
    }

    public boolean instantBreak() {
        return instantBreak;
    }

    private static final GameMode[] VALUES = values();

    public static final NetworkBuffer.Type<GameMode> NETWORK_TYPE = BYTE.transform(
            id -> VALUES[id],
            gameMode -> (byte) gameMode.ordinal()
    );

    public static final NetworkBuffer.Type<GameMode> OPT_NETWORK_TYPE = BYTE.transform(
            id -> id != -1 ? VALUES[id] : null,
            gameMode -> gameMode != null ? (byte) gameMode.ordinal() : -1
    );
}
