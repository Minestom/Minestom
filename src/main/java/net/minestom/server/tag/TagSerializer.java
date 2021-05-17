package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TagSerializer<T> {

    @Nullable T read(@NotNull TagReader reader);

    void write(@NotNull TagWriter writer, @NotNull T value);
}
