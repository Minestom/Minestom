package net.minestom.server.item.drop;

import org.jetbrains.annotations.NotNull;

/**
 * The item was dropped from the hotbar (default key: q)
 */
public record DropReasonHotbar(int slot, @NotNull DropAmount dropAmount) implements DropReason { }
