package net.minestom.server.item.drop;

import org.jetbrains.annotations.NotNull;

/**
 * The item was dropped because the inventory was closed
 */
public record DropReasonClose() implements DropReason {

    // Closing the inventory always results in the full item stack being dropped.
    @Override
    public @NotNull DropAmount dropAmount() {
        return DropAmount.STACK;
    }
}
