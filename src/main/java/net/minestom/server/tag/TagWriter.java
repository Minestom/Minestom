package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public interface TagWriter {
    <T> void setTag(@NotNull Tag<T> tag, @Nullable T value);

    static @NotNull TagWriter fromCompound(@NotNull NBTCompound compound) {
        return new TagWriter() {
            @Override
            public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
                tag.write(compound, value);
            }
        };
    }
}
