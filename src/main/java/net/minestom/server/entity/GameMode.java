package net.minestom.server.entity;

public enum GameMode {

    SURVIVAL((byte) 0), CREATIVE((byte) 1), ADVENTURE((byte) 2), SPECTATOR((byte) 3);

    private byte id;
    private boolean hardcore;

    GameMode(byte id) {
        this.id = id;
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
}
