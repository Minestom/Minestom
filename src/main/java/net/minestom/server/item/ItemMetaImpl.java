package net.minestom.server.item;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Objects;
import java.util.function.Consumer;

record ItemMetaImpl(TagReadable tagReadable, NBTCompound nbt) implements ItemMeta {
    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return tagReadable.getTag(tag);
    }

    @Override
    public @NotNull ItemMeta with(@NotNull Consumer<ItemMeta.@NotNull Builder> builderConsumer) {
        Builder builder = new Builder(TagHandler.fromCompound(nbt));
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Override
    public @NotNull NBTCompound toNBT() {
        return nbt;
    }

    @Override
    public @NotNull String toSNBT() {
        return nbt.toSNBT();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        if (nbt.isEmpty()) {
            writer.writeByte((byte) 0);
            return;
        }
        BinaryWriter w = new BinaryWriter();
        w.writeNBT("", nbt);
        var cachedBuffer = w.getBuffer();
        writer.write(cachedBuffer.flip());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemMetaImpl itemMeta)) return false;
        return nbt.equals(itemMeta.nbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nbt);
    }

    record Builder(TagHandler tagHandler) implements ItemMeta.Builder {
        @Override
        public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
            this.tagHandler.setTag(tag, value);
        }

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return tagHandler.getTag(tag);
        }

        @Override
        public @NotNull ItemMeta build() {
            return new ItemMetaImpl(tagHandler.readableCopy(), tagHandler.asCompound());
        }
    }
}
