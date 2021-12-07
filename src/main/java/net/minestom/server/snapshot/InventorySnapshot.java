package net.minestom.server.snapshot;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public non-sealed interface InventorySnapshot extends Snapshot, TagReadable {
    @NotNull InventoryType type();

    @NotNull Component title();

    @NotNull List<@NotNull ItemStack> content();
}
