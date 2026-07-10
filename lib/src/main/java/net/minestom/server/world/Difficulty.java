package net.minestom.server.world;

/**
 * Those are all the difficulties which can be displayed in the player menu.
 * <p>
 * Sets with {@link net.minestom.server.MinecraftServer#setDifficulty(Difficulty)}.
 */
public enum Difficulty {

    PEACEFUL((byte) 0), EASY((byte) 1), NORMAL((byte) 2), HARD((byte) 3);

    private final byte id;

    Difficulty(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }
}
