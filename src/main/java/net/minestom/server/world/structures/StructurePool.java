package net.minestom.server.world.structures;

public class StructurePool {
    private final StructureType type;

    public StructurePool(StructureType type) {
        this.type = type;
    }

    public void addStructure(Structure structure, float probability) {
        // TODO: 2021. 12. 12. Implement
    }

    public StructureType getType() {
        return type;
    }
}
