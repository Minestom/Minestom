package net.minestom.server.item.metadata;

public class MapMeta extends ItemMeta {

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
    public ItemMeta clone() {
        MapMeta mapMeta = new MapMeta();
        mapMeta.setMapId(mapId);
        return mapMeta;
    }
}
