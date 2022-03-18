package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

final class TagHandlerImpl implements TagHandler {
    private Entry<?>[] entries;

    @Override
    public synchronized <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        final int index = tag.index;
        Entry<?>[] entries = this.entries;
        if (entries == null || index >= entries.length) {
            return tag.createDefault();
        }
        var entry = entries[index];
        if (entry == null) return tag.createDefault();

        if (entry.tag != tag) {
            // Try to convert nbt
            NBTCompound nbt = entry.nbt;
            if (nbt == null) {
                var compound = new MutableNBTCompound();
                entry.tag.writeUnsafe(compound, entry.value);
                entry.nbt = nbt = compound.toCompound();
            }
            return tag.read(nbt);
        }

        //noinspection unchecked
        return (T) entry.value;
    }

    @Override
    public synchronized <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        final int index = tag.index;
        Entry<?>[] entries = this.entries;
        if (entries == null || index >= entries.length) {
            this.entries = entries = new Entry[index + 1];
        }
        entries[index] = new Entry<>(tag, value);
    }

    @Override
    public synchronized @NotNull TagReadable readableCopy() {
        return null;
    }

    @Override
    public synchronized void updateContent(@NotNull NBTCompoundLike compound) {
        this.entries = null;
        for (var entry : compound.asMapView().entrySet()) {
            final String key = entry.getKey();
            final NBT nbt = entry.getValue();
            setTag(Tag.NBT(key), nbt);
        }
    }

    @Override
    public synchronized @NotNull NBTCompound asCompound() {
        var entries = this.entries;
        if (entries == null) {
            return NBTCompound.EMPTY;
        }
        MutableNBTCompound compound = new MutableNBTCompound();
        for (var entry : entries) {
            if (entry == null) continue;
            var nbt = entry.nbt;
            if (nbt != null) {
                compound.putAll(nbt);
            } else {
                entry.tag.writeUnsafe(compound, entry.value);
            }
        }
        return compound.toCompound();
    }

    private static final class Entry<T> {
        final Tag<T> tag;
        final T value;
        NBTCompound nbt;

        Entry(Tag<T> tag, T value) {
            this.tag = tag;
            this.value = value;
        }
    }
}
