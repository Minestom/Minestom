package net.minestom.server.entity;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Represents the game mode of a player.
 * <p>
 * Can be set with {@link Player#setGameMode(GameMode)}.
 */
public class GameMode {

    public static final GameMode SURVIVAL = new GameMode((byte) 0, player -> true);
    public static final GameMode CREATIVE = new GameMode((byte) 1, player -> false);
    public static final GameMode ADVENTURE = new GameMode((byte) 2, player -> true);
    public static final GameMode SPECTATOR = new GameMode((byte) 3, player -> false);

    private final byte id;
    private final Predicate<Player> canTakeDamagePredicate;

    private GameMode(byte id, Predicate<Player> canTakeDamagePredicate) {
        this.id = id;
        this.canTakeDamagePredicate = canTakeDamagePredicate;
    }

    public byte id() {
        return id;
    }

    public String getName() {
        return switch (id) {
            case 0 -> "SURVIVAL";
            case 1 -> "CREATIVE";
            case 2 -> "ADVENTURE";
            case 3 -> "SPECTATOR";
            default -> "UNKNOWN";
        };
    }

    public boolean canTakeDamage(Player player) {
        return canTakeDamagePredicate.test(player);
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

    public static GameMode fromName(String name) {
        return switch (name.toUpperCase()) {
            case "SURVIVAL" -> SURVIVAL;
            case "CREATIVE" -> CREATIVE;
            case "ADVENTURE" -> ADVENTURE;
            case "SPECTATOR" -> SPECTATOR;
            default -> throw new IllegalArgumentException("Unknown game mode: " + name);
        };
    }
}