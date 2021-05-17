package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public interface TagReader {
    <T> @Nullable T getTag(@NotNull Tag<T> tag);

    boolean hasTag(@NotNull Tag<?> tag);

    static @NotNull TagReader fromCompound(@NotNull NBTCompound compound) {
        return new TagReader() {
            @Override
            public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
                return tag.read(compound);
            }

            @Override
            public boolean hasTag(@NotNull Tag<?> tag) {
                return compound.containsKey(tag.getKey());
            }
        };
    }
}
