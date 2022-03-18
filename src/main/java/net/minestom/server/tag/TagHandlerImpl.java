package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

final class TagHandlerImpl implements TagHandler {
    private static final VarHandle ENTRY_UPDATER = MethodHandles.arrayElementVarHandle(Entry[].class);

    private volatile Entry<?>[] entries;

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return read(entries, tag, true);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        final int index = tag.index;
        Entry<?>[] entries = this.entries;
        if (entries == null || index >= entries.length) {
            this.entries = entries = new Entry[index + 1];
        }
        ENTRY_UPDATER.setVolatile(entries, index, new Entry<>(tag, value));
    }

    @Override
    public @NotNull TagReadable readableCopy() {
        var entries = this.entries;
        if (entries == null) {
            return new Reader(null);
        }
        return new Reader(entries.clone());
    }

    @Override
    public void updateContent(@NotNull NBTCompoundLike compound) {
        Entry<?>[] entries = null;
        for (var entry : compound.asMapView().entrySet()) {
            final String key = entry.getKey();
            final NBT nbt = entry.getValue();
            final Tag<NBT> tag = Tag.NBT(key);
            final int index = tag.index;
            if (entries == null || index >= entries.length) {
                entries = new Entry[index + 1];
            }
            entries[index] = new Entry<>(tag, nbt);
        }
        this.entries = entries;
    }

    @Override
    public @NotNull NBTCompound asCompound() {
        var entries = this.entries;
        if (entries == null) {
            return NBTCompound.EMPTY;
        }
        entries = entries.clone();
        MutableNBTCompound compound = new MutableNBTCompound();
        for (var entry : entries) {
            if (entry == null) continue;
            final NBTCompound nbt = entry.nbt;
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
        volatile NBTCompound nbt;

        Entry(Tag<T> tag, T value) {
            this.tag = tag;
            this.value = value;
        }
    }

    private record Reader(Entry<?>[] entries) implements TagReadable {
        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return read(entries, tag, false);
        }
    }

    private static <T> T read(Entry<?>[] entries, Tag<T> tag, boolean volatileRead) {
        final int index;
        if (entries == null || (index = tag.index) >= entries.length) {
            return tag.createDefault();
        }
        Entry<?> entry = volatileRead ? (Entry<?>) ENTRY_UPDATER.getVolatile(entries, index) : entries[index];
        if (entry == null) return tag.createDefault();

        // Value must be parsed from nbt if the tag is different
        {
            final Tag<?> entryTag = entry.tag;
            if (entryTag != tag) {
                // Try to convert nbt
                NBTCompound nbt = entry.nbt;
                if (nbt == null) {
                    var compound = new MutableNBTCompound();
                    entryTag.writeUnsafe(compound, entry.value);
                    entry.nbt = nbt = compound.toCompound();
                }
                return tag.read(nbt);
            }
        }

        // Tag is the same, return the value
        //noinspection unchecked
        return (T) entry.value;
    }
}
