package net.minestom.server.entity;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the game mode of a player.
 * <p>
 * Can be set with {@link Player#setGameMode(GameMode)}.
 */
public enum GameMode {
    SURVIVAL((byte) 0, true),
    CREATIVE((byte) 1, false),
    ADVENTURE((byte) 2, true),
    SPECTATOR((byte) 3, false);

    private final byte id;
    private final boolean canTakeDamage;

    GameMode(byte id, boolean canTakeDamage) {
        this.id = id;
        this.canTakeDamage = canTakeDamage;
    }

    public byte id() {
        return id;
    }

    public boolean canTakeDamage() {
        return canTakeDamage;
    }

    public static @NotNull GameMode fromId(int id) {
        return switch (id) {
            case 0 -> SURVIVAL;
            case 1 -> CREATIVE;
            case 2 -> ADVENTURE;
            case 3 -> SPECTATOR;
            default -> throw new IllegalArgumentException("Unknown game mode id: " + id);
        };
    }
}
