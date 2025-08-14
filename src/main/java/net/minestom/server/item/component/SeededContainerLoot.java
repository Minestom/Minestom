package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

public record SeededContainerLoot(String lootTable, long seed) {
    public static final Codec<SeededContainerLoot> CODEC = StructCodec.struct(
            "loot_table", Codec.STRING, SeededContainerLoot::lootTable,
            "seed", Codec.LONG, SeededContainerLoot::seed,
            SeededContainerLoot::new);
}
