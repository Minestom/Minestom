package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Function;

final class TagSerializerImpl {
    static <T> TagSerializer<T> fromCompound(Function<NBTCompound, T> readFunc, Function<T, NBTCompound> writeFunc) {
        return new TagSerializer<>() {
            @Override
            public @Nullable T read(@NotNull TagReadable reader) {
                final NBTCompound compound = ((TagHandler) reader).asCompound();
                return readFunc.apply(compound);
            }

            @Override
            public void write(@NotNull TagWritable writer, @NotNull T value) {
                final NBTCompound compound = writeFunc.apply(value);
                TagNbtSeparator.separate(compound, entry -> writer.setTag(entry.tag(), entry.value()));
            }
        };
    }
}
