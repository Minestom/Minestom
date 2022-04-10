package net.minestom.server.item;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Consumer;

record ItemMetaImpl(TagHandler tagHandler) implements ItemMeta {
    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return tagHandler.getTag(tag);
    }

    @Override
    public @NotNull ItemMeta with(@NotNull Consumer<ItemMeta.@NotNull Builder> builderConsumer) {
        Builder builder = new Builder(tagHandler.copy());
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Override
    public @NotNull NBTCompound toNBT() {
        return tagHandler.asCompound();
    }

    @Override
    public @NotNull String toSNBT() {
        return toNBT().toSNBT();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        final NBTCompound nbt = toNBT();
        if (nbt.isEmpty()) {
            writer.writeByte((byte) 0);
            return;
        }
        BinaryWriter w = new BinaryWriter();
        w.writeNBT("", nbt);
        var cachedBuffer = w.getBuffer();
        writer.write(cachedBuffer.flip());
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
            return new ItemMetaImpl(tagHandler.copy());
        }
    }
}
