package net.minestom.server.instance;

public class SectionCache {

    private final Section section;
    private final int chunkX;
    private final int sectionY;
    private final int chunkZ;

    public SectionCache(Section section, int chunkX, int sectionY, int chunkZ) {
        this.section = section;
        this.chunkX = chunkX;
        this.sectionY = sectionY;
        this.chunkZ = chunkZ;
    }

    public Section section() {
        return section;
    }

    public int chunkX() {
        return chunkX;
    }

    public int sectionY() {
        return sectionY;
    }

    public int chunkZ() {
        return chunkZ;
    }
}
