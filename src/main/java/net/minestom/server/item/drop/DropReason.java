package net.minestom.server.item.drop;

import org.jetbrains.annotations.NotNull;

public interface DropReason {

    @NotNull DropAmount dropAmount();


    enum DropAmount {
        SINGLE,
        STACK
    }
}
