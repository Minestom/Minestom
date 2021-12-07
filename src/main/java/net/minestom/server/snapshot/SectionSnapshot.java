package net.minestom.server.snapshot;

import net.minestom.server.instance.block.Block;
import net.minestom.server.world.biomes.Biome;

public interface SectionSnapshot extends Snapshot, Block.Getter, Biome.Getter {
    int index();
}
