package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TagSetter {
    <T> void setTag(@NotNull Tag<T> tag, @Nullable T value);
}
