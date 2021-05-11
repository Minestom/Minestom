package net.minestom.demo.generator;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;

import java.util.HashMap;
import java.util.Map;

public class Structure {

    private final Map<BlockPosition, Block> blocks = new HashMap<>();

    public void build(ChunkBatch batch, BlockPosition pos) {
        blocks.forEach((bPos, block) -> {
            if (bPos.getX() + pos.getX() >= Chunk.CHUNK_SIZE_X || bPos.getX() + pos.getX() < 0)
                return;
            if (bPos.getZ() + pos.getZ() >= Chunk.CHUNK_SIZE_Z || bPos.getZ() + pos.getZ() < 0)
                return;
            batch.setBlock(bPos.clone().add(pos), block);
        });
    }

    public void addBlock(Block block, int localX, int localY, int localZ) {
        blocks.put(new BlockPosition(localX, localY, localZ), block);
    }

}
