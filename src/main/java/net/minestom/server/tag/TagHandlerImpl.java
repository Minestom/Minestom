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
import java.util.function.UnaryOperator;

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
        // Convert value to fit the tag (e.g. list copies)
        if (value != null) {
            final UnaryOperator<T> copy = tag.copy;
            if (copy != null) value = copy.apply(value);
        }

        VarHandle.acquireFence();
        int tagIndex = tag.index;
        TagHandlerImpl local = this;
        Entry<?>[] entries = this.entries;

        var paths = tag.path;
        TagHandlerImpl[] pathHandlers = null;
        if (paths != null) {
            pathHandlers = new TagHandlerImpl[paths.size()];
            // Path-able tag
            int in = 0;
            for (var path : paths) {
                final int pathIndex = path.index();
                if (pathIndex >= entries.length) {
                    if (value == null) return;
                    local.entries = entries = Arrays.copyOf(entries, pathIndex + 1);
                }
                Entry<?> entry = entries[pathIndex];
                if (entry == null) {
                    if (value == null) return;
                    var updated = new TagHandlerImpl();
                    entries[pathIndex] = new Entry<>(Tag.tag(path.name(), null, null), updated);
                    local = updated;
                } else if (entry.value instanceof TagHandlerImpl handler) {
                    local = handler;
                } else {
                    throw new IllegalStateException("Cannot set a path-able tag on a non-path-able entry");
                }
                entries = local.entries;
                pathHandlers[in++] = local;
            }
            // Handle removal if the tag was present (recursively)
            if (value == null) {
                pathHandlers[pathHandlers.length - 1].entries[tagIndex] = null;
                boolean empty = false;
                removalLoop:
                for (int i = pathHandlers.length - 1; i >= 0; i--) {
                    TagHandlerImpl handler = pathHandlers[i];
                    Entry<?>[] entr = handler.entries;
                    // Verify if the handler is empty
                    empty = tagIndex >= entr.length;
                    if (!empty) {
                        empty = true;
                        for (var entry : entr) {
                            if (entry != null) {
                                empty = false;
                                continue removalLoop;
                            }
                        }
                    }
                    if (i > 0) {
                        TagHandlerImpl parent = pathHandlers[i - 1];
                        parent.entries[paths.get(i).index()] = null;
                    }
                }
                if (empty) {
                    // Remove the root handler
                    local = this;
                    entries = this.entries;
                    tagIndex = paths.get(0).index();
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
            VarHandle.releaseFence();
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
        if (tag.path != null) {
            // Must be a path-able entry
            var paths = tag.path;
            for (var path : paths) {
                final int pathIndex = path.index();
                if (pathIndex >= entries.length || (entry = entries[pathIndex]) == null) {
                    return tag.createDefault();
                }
                if (entry.value instanceof TagHandlerImpl handler) {
                    entries = handler.entries;
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
        if (entryTag == tag) {
            // Tag is the same, return the value
            //noinspection unchecked
            return (T) value;
        }
        // Value must be parsed from nbt if the tag is different
        NBT nbt = entry.nbt;
        if (nbt == null) entry.nbt = nbt = (NBT) entryTag.writeFunction.apply(value);
        try {
            return tag.readFunction.apply(nbt);
        } catch (ClassCastException e) {
            return tag.createDefault();
        }
    }
}
