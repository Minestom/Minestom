package net.minestom.server.item.drop;

import net.minestom.server.inventory.AbstractInventory;
import org.jetbrains.annotations.NotNull;

/**
 * The item was dropped from an inventory
 */
public record DropReasonInventory(@NotNull AbstractInventory abstractInventory, @NotNull DropAmount dropAmount) implements DropReason { }
