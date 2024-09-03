package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

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

    public boolean canTakeDamage() {
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

    public static final NetworkBuffer.Type<GameMode> OPT_NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, GameMode value) {
            buffer.write(BYTE, value != null ? (byte) value.ordinal() : -1);
        }

        @Override
        public GameMode read(@NotNull NetworkBuffer buffer) {
            final byte id = buffer.read(BYTE);
            return id != -1 ? VALUES[id] : null;
        }
    };
}
