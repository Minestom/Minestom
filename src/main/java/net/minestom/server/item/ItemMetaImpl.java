package net.minestom.server.item;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Objects;
import java.util.function.Consumer;

import static net.minestom.server.network.NetworkBuffer.BYTE;
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
    public @NotNull NBTCompound toNBT() {
        return tagHandler.asCompound();
    }

    @Override
    public @NotNull String toSNBT() {
        return toNBT().toSNBT();
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        final NBTCompound nbt = toNBT();
        if (nbt.isEmpty()) {
            writer.write(BYTE, (byte) 0);
            return;
        }
        writer.write(NBT, nbt);
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
