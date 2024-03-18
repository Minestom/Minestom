package net.minestom.server.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import static net.minestom.server.network.NetworkBuffer.NBT;

record ItemMetaImpl(TagHandler tagHandler) implements ItemMeta {
    static final ItemMetaImpl EMPTY = new ItemMetaImpl(TagHandler.newHandler());

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
    public @NotNull CompoundBinaryTag toNBT() {
        return tagHandler.asCompound();
    }

    @Override
    public @NotNull String toSNBT() {
        try {
            return TagStringIO.get().asString(toNBT());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert to SNBT", e);
        }
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NBT, toNBT());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemMetaImpl itemMeta)) return false;
        return toNBT().equals(itemMeta.toNBT());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toNBT());
    }

    @Override
    public String toString() {
        return toSNBT();
    }

    record Builder(TagHandler tagHandler) implements ItemMeta.Builder {
        @Override
        public @NotNull ItemMetaImpl build() {
            return new ItemMetaImpl(tagHandler.copy());
        }
    }
}
