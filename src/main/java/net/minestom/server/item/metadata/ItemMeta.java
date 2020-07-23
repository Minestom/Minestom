package net.minestom.server.item.metadata;

public abstract class ItemMeta {

    public abstract boolean hasNbt();

    public abstract boolean isSimilar(ItemMeta itemMeta);

    public abstract ItemMeta clone();

}
