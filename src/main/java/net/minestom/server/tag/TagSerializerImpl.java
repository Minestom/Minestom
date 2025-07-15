package net.minestom.server.tag;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

final class TagSerializerImpl {
    public static final TagSerializer<CompoundBinaryTag> COMPOUND = new TagSerializer<>() {
        @Override
        public CompoundBinaryTag read(TagReadable reader) {
            return ((TagHandler) reader).asCompound();
        }

        @Override
        public void write(TagWritable writer, CompoundBinaryTag value) {
            TagNbtSeparator.separate(value, entry -> writer.setTag(entry.tag(), entry.value()));
        }
    };

    static <T> TagSerializer<T> fromCompound(Function<CompoundBinaryTag, T> readFunc, Function<T, CompoundBinaryTag> writeFunc) {
        return new TagSerializer<>() {
            @Override
            public @Nullable T read(TagReadable reader) {
                final CompoundBinaryTag compound = COMPOUND.read(reader);
                return readFunc.apply(compound);
            }

            @Override
            public void write(TagWritable writer, T value) {
                final CompoundBinaryTag compound = writeFunc.apply(value);
                COMPOUND.write(writer, compound);
            }
        };
    }
}
