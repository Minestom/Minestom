package net.minestom.server.tag;

import net.minestom.server.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.Arrays;
import java.util.function.UnaryOperator;

final class TagHandlerImpl implements TagHandler {
    private volatile Entry<?>[] entries = new Entry[0];
    private Cache cache;

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return read(entries, tag);
    }

    @Override
    public synchronized <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        // Convert value to fit the tag (e.g. list copies)
        if (value != null) {
            final UnaryOperator<T> copy = tag.copy;
            if (copy != null) value = copy.apply(value);
        }

        int tagIndex = tag.index;
        TagHandlerImpl local = this;
        Entry<?>[] entries = this.entries;
        final Entry<?>[] localEntries = entries;

        final var paths = tag.path;
        TagHandlerImpl[] pathHandlers = null;
        if (paths != null) {
            final int length = paths.length;
            pathHandlers = new TagHandlerImpl[length];
            for (int i = 0; i < length; i++) {
                final Tag.PathEntry path = paths[i];
                final int pathIndex = path.index();
                if (pathIndex >= entries.length) {
                    if (value == null) return;
                    local.entries = entries = Arrays.copyOf(entries, pathIndex + 1);
                }
                final Entry<?> entry = entries[pathIndex];
                if (entry == null) {
                    if (value == null) return;
                    // Empty path, create a new handler
                    local = new TagHandlerImpl();
                    entries[pathIndex] = new Entry(Tag.tag(path.name(), Serializers.VOID), local);
                } else if (entry.value instanceof TagHandlerImpl handler) {
                    // Existing path, continue navigating
                    local = handler;
                } else throw new IllegalStateException("Cannot set a path-able tag on a non-path-able entry");
                entries = local.entries;
                pathHandlers[i] = local;
            }
            // Handle removal if the tag was present (recursively)
            if (value == null) {
                pathHandlers[length - 1].entries[tagIndex] = null;
                boolean empty = false;
                for (int i = length - 1; i >= 0; i--) {
                    TagHandlerImpl handler = pathHandlers[i];
                    Entry<?>[] entr = handler.entries;
                    // Verify if the handler is empty
                    empty = tagIndex >= entr.length || ArrayUtils.isEmpty(entr);
                    if (empty && i > 0) {
                        TagHandlerImpl parent = pathHandlers[i - 1];
                        parent.entries[paths[i].index()] = null;
                    }
                }
                if (empty) {
                    // Remove the root handler
                    local = this;
                    entries = localEntries;
                    tagIndex = paths[0].index();
                }
            }
        }
        // Normal tag
        if (tagIndex >= entries.length) {
            if (value == null) return;
            local.entries = entries = Arrays.copyOf(entries, tagIndex + 1);
        }
        entries[tagIndex] = value != null ? new Entry<>(tag, value) : null;
        this.cache = null;
        if (pathHandlers != null) {
            for (var handler : pathHandlers) handler.cache = null;
        }
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
        synchronized (this) {
            this.cache = null;
            this.entries = entries;
        }
    }

    @Override
    public @NotNull NBTCompound asCompound() {
        return updatedCache().compound;
    }

    private synchronized Cache updatedCache() {
        Cache cache = this.cache;
        if (cache == null) {
            Entry<?>[] entries = this.entries;
            if (entries.length > 0) {
                entries = entries.clone();
                MutableNBTCompound tmp = new MutableNBTCompound();
                for (Entry<?> entry : entries) {
                    if (entry == null) continue;
                    final Tag<?> tag = entry.tag;
                    final Object value = entry.value;
                    if (value instanceof TagHandler handler) {
                        // Path-able entry
                        tmp.put(tag.getKey(), handler.asCompound());
                    } else {
                        tag.writeUnsafe(tmp, value);
                    }
                }
                cache = !tmp.isEmpty() ? new Cache(entries, tmp.toCompound()) : Cache.EMPTY;
            } else {
                cache = Cache.EMPTY;
            }
            this.cache = cache;
        }
        return cache;
    }

    private static final class Entry<T> {
        final Tag<T> tag;
        final T value; // TagHandler type for path-able tags
        volatile NBT nbt;

        Entry(Tag<T> tag, T value) {
            this.tag = tag;
            this.value = value;
        }

        NBT updatedNbt() {
            NBT nbt = this.nbt;
            if (nbt == null) this.nbt = nbt = tag.entry.write().apply(value);
            return nbt;
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
        Entry<?> entry;
        final var paths = tag.path;
        if (paths != null) {
            // Must be a path-able entry
            for (var path : paths) {
                final int pathIndex = path.index();
                if (pathIndex >= entries.length || (entry = entries[pathIndex]) == null) {
                    return tag.createDefault();
                }
                if (entry.value instanceof TagHandlerImpl handler) {
                    entries = handler.entries;
                } else if (entry.updatedNbt() instanceof NBTCompound compound) {
                    var tmp = new TagHandlerImpl();
                    tmp.updateContent(compound);
                    entries = tmp.entries;
                }
            }
        }
        if (index >= entries.length || (entry = entries[index]) == null) {
            return tag.createDefault();
        }
        final Object value = entry.value;
        if (value instanceof TagHandlerImpl)
            throw new IllegalStateException("Cannot read path-able tag " + tag.getKey());
        final Tag entryTag = entry.tag;
        if (entryTag.shareValue(tag)) {
            // Tag is the same, return the value
            //noinspection unchecked
            return (T) value;
        }
        // Value must be parsed from nbt if the tag is different
        final NBT nbt = entry.updatedNbt();
        try {
            return tag.entry.read().apply(nbt);
        } catch (ClassCastException e) {
            return tag.createDefault();
        }
    }
}
