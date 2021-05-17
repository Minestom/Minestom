package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TagGetter {
    <T> @Nullable T getTag(@NotNull Tag<T> tag);

    boolean hasTag(@NotNull Tag<?> tag);
}
