package net.minestom.server.world.structures;

public class StructurePool {
    private final StructureLocation location;

    public StructurePool(StructureLocation location) {
        this.location = location;
    }

    public void addStructure(Structure structure, float probability) {
        // TODO: 2021. 12. 12. Implement
    }

    public StructureLocation getLocation() {
        return location;
    }
}
