package net.minestom.server.instance.chunksystem.impl;

public class ChunkClaimTree {
    private final IntervalTree<IntervalTree<Entry>> tree = new IntervalTree<>();
    
    public void insert(int minX, int minZ, int maxX, int maxZ) {
    }
    
    public record Entry() {
    }
}
