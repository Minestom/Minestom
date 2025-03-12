package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.block.BlockChangeEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SuspiciousGravelBlockHandler implements BlockHandler {
    public static final SuspiciousGravelBlockHandler INSTANCE = new SuspiciousGravelBlockHandler(true);
    public static final SuspiciousGravelBlockHandler INSTANCE_NO_TAGS = new SuspiciousGravelBlockHandler(false);

    public static AtomicReference<Block> blockAtomicReference = new AtomicReference<>();

    public static final Tag<String> LOOT_TABLE = Tag.String("LootTable");
    public static final Tag<ItemStack> ITEM = Tag.ItemStack("item");

    private final boolean hasTags;

    public SuspiciousGravelBlockHandler(boolean hasTags) {
        this.hasTags = hasTags;
    }

    @Override
    public Block onNeighborUpdate(@NotNull Block neighbor, @NotNull Point neighborPosition, @NotNull Instance instance, @NotNull BlockFace fromFace) {
        Pos pos = new Pos(neighborPosition.x(), neighborPosition.y(), neighborPosition.z());
        Block currentBlock = instance.getBlock(pos.relative(fromFace));
        blockAtomicReference.set(currentBlock);
        return neighbor;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return hasTags ? List.of(LOOT_TABLE, ITEM) : List.of();
    }

    @Override
    public @NotNull Block getBlock() {
        return Block.SUSPICIOUS_GRAVEL;
    }
}
