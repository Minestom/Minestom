package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.lang.invoke.VarHandle;
import java.util.Arrays;

final class TagHandlerImpl implements TagHandler {
    private Entry<?>[] entries = new Entry[0];
    private Cache cache;

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        VarHandle.acquireFence();
        return read(entries, tag);
    }

    @Override
    public synchronized <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        VarHandle.acquireFence();
        final int index = tag.index;
        Entry<?>[] entries = this.entries;
        final Entry<T> entry = value != null ? new Entry<>(tag, value) : null;
        if (index >= entries.length) {
            if (value == null)
                return; // no need to create/remove an entry
            this.entries = entries = Arrays.copyOf(entries, index + 1);
        }
        entries[index] = entry;
        this.cache = null;
        VarHandle.releaseFence();
    }

    @Override
    public @NotNull TagReadable readableCopy() {
        return updatedCache();
    }

    @Override
    public void updateContent(@NotNull NBTCompoundLike compound) {
        Entry<?>[] entries = new Entry[0];
        for (var entry : compound) {
            final String key = entry.getKey();
            final NBT nbt = entry.getValue();
            final Tag<NBT> tag = Tag.NBT(key);
            final int index = tag.index;
            if (index >= entries.length) {
                entries = Arrays.copyOf(entries, index + 1);
            }
            entries[index] = new Entry<>(tag, nbt);
        }
        this.entries = entries;
        this.cache = null;
        VarHandle.releaseFence();
    }

    @Override
    public @NotNull NBTCompound asCompound() {
        return updatedCache().compound;
    }

    private Cache updatedCache() {
        VarHandle.acquireFence();
        Cache cache = this.cache;
        if (cache == null) {
            Entry<?>[] entries = this.entries;
            if (entries.length > 0) {
                entries = entries.clone();
                MutableNBTCompound tmp = new MutableNBTCompound();
                for (Entry<?> entry : entries) {
                    if (entry == null) continue;
                    final Tag<?> tag = entry.tag;
                    tag.writeUnsafe(tmp, entry.value);
                }
                cache = !tmp.isEmpty() ? new Cache(entries, tmp.toCompound()) : Cache.EMPTY;
            } else {
                cache = Cache.EMPTY;
            }
            this.cache = cache;
            VarHandle.releaseFence();
        }
        return cache;
    }

    private static final class Entry<T> {
        final Tag<T> tag;
        final T value;
        volatile NBT nbt;

        Entry(Tag<T> tag, T value) {
            this.tag = tag;
            this.value = value;
        }
    }

    private record Cache(Entry<?>[] entries, NBTCompound compound) implements TagReadable {
        static final Cache EMPTY = new Cache(new Entry[0], NBTCompound.EMPTY);

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return read(entries, tag);
        }
    }

    private static <T> T read(Entry<?>[] entries, Tag<T> tag) {
        final int index = tag.index;
        final Entry<?> entry;
        if (index >= entries.length || (entry = entries[index]) == null) {
            return tag.createDefault();
        }
        final Tag entryTag = entry.tag;
        if (entryTag == tag) {
            // Tag is the same, return the value
            //noinspection unchecked
            return (T) entry.value;
        }
        // Value must be parsed from nbt if the tag is different
        NBT nbt = entry.nbt;
        if (nbt == null) entry.nbt = nbt = entryTag.convertToNbt(entry.value);
        return tag.convertToValue(nbt);
    }
}
