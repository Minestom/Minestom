package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record SeededContainerLoot(@NotNull String lootTable, long seed) {

    public static final BinaryTagSerializer<SeededContainerLoot> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new SeededContainerLoot(tag.getString("loot_table"), tag.getLong("seed")),
            loot -> CompoundBinaryTag.builder()
                    .putString("loot_table", loot.lootTable)
                    .putLong("seed", loot.seed)
                    .build()
    );
}
