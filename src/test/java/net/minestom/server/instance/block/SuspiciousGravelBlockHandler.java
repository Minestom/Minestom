package net.minestom.server.instance.block;

import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SuspiciousGravelBlockHandler implements BlockHandler {
    public static final SuspiciousGravelBlockHandler INSTANCE = new SuspiciousGravelBlockHandler(true);
    public static final SuspiciousGravelBlockHandler INSTANCE_NO_TAGS = new SuspiciousGravelBlockHandler(false);

    public static final Tag<String> LOOT_TABLE = Tag.String("LootTable");
    public static final Tag<ItemStack> ITEM = Tag.ItemStack("item");

    private final boolean hasTags;

    public SuspiciousGravelBlockHandler(boolean hasTags) {
        this.hasTags = hasTags;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NamespaceID.from("minecraft:suspicious_gravel");
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return hasTags ? List.of(LOOT_TABLE, ITEM) : List.of();
    }
}
