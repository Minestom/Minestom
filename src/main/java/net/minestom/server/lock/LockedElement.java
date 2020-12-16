package net.minestom.server.lock;

import org.jetbrains.annotations.NotNull;

public interface LockedElement<T> {

    @NotNull
    AcquirableElement<T> getAcquiredElement();

}
