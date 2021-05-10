package net.minestom.codegen.items;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;

public class ItemContainer implements Comparable<ItemContainer> {
    private int id;
    private NamespaceID name;
    private int stackSize;
    private Block block;

    public ItemContainer(int id, NamespaceID name, int stackSize, Block block) {
        this.id = id;
        this.name = name;
        this.stackSize = stackSize;
        this.block = block;
    }

    public int getId() {
        return id;
    }

    public NamespaceID getName() {
        return name;
    }

    public Block getBlock() {
        return block;
    }

    public int getStackSize() {
        return stackSize;
    }

    @Override
    public int compareTo(ItemContainer o) {
        return Integer.compare(id, o.id);
    }
}
