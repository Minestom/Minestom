package net.minestom.server.map;

public class MapHandle {

    private int mapId;

    protected MapHandle(int mapId) {
        this.mapId = mapId;
    }

    public int getMapId() {
        return mapId;
    }
}
