package net.minestom.server.map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class MapManager {

    private Int2ObjectMap<MapHandle> objectMap = new Int2ObjectOpenHashMap<>();

    public MapHandle getMap(int mapId) {
        return objectMap.computeIfAbsent(mapId, id -> new MapHandle(id));
    }

}
