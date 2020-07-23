package net.minestom.server.item.metadata;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class MapMeta implements ItemMeta {

    private int mapId;

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    @Override
    public boolean hasNbt() {
        return mapId != 0;
    }

    @Override
    public boolean isSimilar(ItemMeta itemMeta) {
        return itemMeta instanceof MapMeta && ((MapMeta) itemMeta).getMapId() == mapId;
    }

    @Override
    public void read(NBTCompound compound) {
        if (compound.containsKey("map")) {
            this.mapId = compound.getInt("map");
        }
    }

    @Override
    public void write(NBTCompound compound) {
        if (mapId != 0) {
            compound.setInt("map", mapId);
        }
    }

    @Override
    public ItemMeta clone() {
        MapMeta mapMeta = new MapMeta();
        mapMeta.setMapId(mapId);
        return mapMeta;
    }
}
