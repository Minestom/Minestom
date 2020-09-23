package net.minestom.server.entity;

public enum GameMode {

    SURVIVAL((byte) 0, true), CREATIVE((byte) 1, false), ADVENTURE((byte) 2, true), SPECTATOR((byte) 3, false);

    private final byte id;
    private boolean hardcore;
    private final boolean canTakeDamage;

    GameMode(byte id, boolean canTakeDamage) {
        this.id = id;
        this.canTakeDamage = canTakeDamage;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public byte getId() {
        return id;
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public boolean canTakeDamage() {
        return canTakeDamage;
    }
}
