package net.minestom.server.tag;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.function.UnaryOperator;

final class TagHandlerImpl implements TagHandler {
    private volatile SPMCMap entries;
    private Cache cache;

    TagHandlerImpl(SPMCMap entries, Cache cache) {
        this.entries = entries;
        this.cache = cache;
    }

    TagHandlerImpl() {
        this.entries = new SPMCMap();
        this.cache = null;
    }

    static TagHandlerImpl fromCompound(NBTCompoundLike compound) {
        TagHandlerImpl handler = new TagHandlerImpl();
        for (var entry : compound) {
            final Tag<NBT> tag = Tag.NBT(entry.getKey());
            final NBT nbt = entry.getValue();
            handler.setTag(tag, nbt);
        }
        return handler;
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        if (tag.isView()) return tag.read(asCompound());
        return read(entries, tag);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        if (tag.isView()) {
            MutableNBTCompound viewCompound = new MutableNBTCompound();
            tag.writeUnsafe(viewCompound, value);
            updateContent(viewCompound);
        } else {
            if (value instanceof NBT nbt) {
                synchronized (this) {
                    write(tag, null);
                    TagNbtSeparator.separate(tag.getKey(), nbt,
                            entry -> write(entry.tag(), entry.value()));
                }
            } else {
                if (value != null) {
                    final UnaryOperator<T> copy = tag.copy;
                    if (copy != null) value = copy.apply(value);
                }
                write(tag, value);
            }
        }
    }

    @Override
    public @NotNull TagReadable readableCopy() {
        return updatedCache();
    }

    @Override
    public synchronized @NotNull TagHandler copy() {
        return new TagHandlerImpl(entries.clone(), cache);
    }

    @Override
    public void updateContent(@NotNull NBTCompoundLike compound) {
        final TagHandlerImpl converted = fromCompound(compound);
        synchronized (this) {
            this.cache = converted.cache;
            this.entries = converted.entries;
        }
    }

    @Override
    public @NotNull NBTCompound asCompound() {
        return updatedCache().compound;
    }

    private synchronized <T> void write(@NotNull Tag<T> tag, @Nullable T value) {
        int tagIndex = tag.index;
        TagHandlerImpl local = this;

        final Tag.PathEntry[] paths = tag.path;
        TagHandlerImpl[] pathHandlers = null;
        if (paths != null) {
            final int length = paths.length;
            pathHandlers = new TagHandlerImpl[length];
            for (int i = 0; i < length; i++) {
                final Tag.PathEntry path = paths[i];
                final int pathIndex = path.index();
                final Entry<?> entry = local.entries.get(pathIndex);
                if (entry instanceof PathEntry pathEntry) {
                    // Existing path, continue navigating
                    local = pathEntry.value;
                } else {
                    if (value == null) return;
                    // Empty path, create a new handler.
                    // Slow path is taken if the entry comes from a Structure tag, requiring conversion from NBT
                    TagHandlerImpl tmp = local;
                    local = entry != null && entry.updatedNbt() instanceof NBTCompound compound ?
                            fromCompound(compound) : new TagHandlerImpl();
                    tmp.entries.put(pathIndex, new PathEntry(path.name(), local));
                }
                pathHandlers[i] = local;
            }
            // Handle removal if the tag was present (recursively)
            if (value == null) {
                // Remove entry
                if (pathHandlers[length - 1].entries.remove(tagIndex) == null) return;
                // Clear empty parents
                for (int i = length - 1; i >= 0; i--) {
                    final TagHandlerImpl handler = pathHandlers[i];
                    if (!handler.entries.isEmpty()) break;
                    final int pathIndex = paths[i].index();
                    if (i == 0) {
                        // Remove the root handler
                        local = this;
                        tagIndex = pathIndex;
                    } else {
                        TagHandlerImpl parent = pathHandlers[i - 1];
                        parent.entries.remove(pathIndex);
                    }
                }
            }
        }
        // Normal tag
        if (value != null) local.entries.put(tagIndex, new TagEntry<>(tag, value));
        else local.entries.remove(tagIndex);
        this.cache = null;
        if (pathHandlers != null) {
            for (var handler : pathHandlers) handler.cache = null;
        }
    }

    private synchronized Cache updatedCache() {
        Cache cache = this.cache;
        if (cache == null) {
            final var entries = this.entries;
            if (!entries.isEmpty()) {
                MutableNBTCompound tmp = new MutableNBTCompound();
                for (Entry<?> entry : entries.values()) {
                    if (entry != null) tmp.put(entry.tag().getKey(), entry.updatedNbt());
                }
                cache = new Cache(entries.clone(), tmp.toCompound());
            } else cache = Cache.EMPTY;
            this.cache = cache;
        }
        return cache;
    }

    private static <T> T read(Int2ObjectOpenHashMap<Entry<?>> entries, Tag<T> tag) {
        final Tag.PathEntry[] paths = tag.path;
        if (paths != null) {
            // Must be a path-able entry
            if ((entries = traversePath(paths, entries)) == null)
                return tag.createDefault();
        }
        final Entry<?> entry;
        if ((entry = entries.get(tag.index)) == null) {
            return tag.createDefault();
        }
        if (entry.tag().shareValue(tag)) {
            // The tag used to write the entry is compatible with the one used to get
            // return the value directly
            //noinspection unchecked
            return (T) entry.value();
        }
        // Value must be parsed from nbt if the tag is different
        final NBT nbt = entry.updatedNbt();
        try {
            return tag.entry.read().apply(nbt);
        } catch (ClassCastException e) {
            return tag.createDefault();
        }
    }

    private static Int2ObjectOpenHashMap<Entry<?>> traversePath(Tag.PathEntry[] paths,
                                                                Int2ObjectOpenHashMap<Entry<?>> entries) {
        for (var path : paths) {
            final Entry<?> entry;
            if ((entry = entries.get(path.index())) == null)
                return null;
            if (entry instanceof PathEntry pathEntry) {
                entries = pathEntry.value.entries;
            } else if (entry.updatedNbt() instanceof NBTCompound compound) {
                // Slow path forcing a conversion of the structure to NBTCompound
                // TODO should the handler be cached inside the entry?
                TagHandlerImpl tmp = fromCompound(compound);
                entries = tmp.entries;
            } else {
                // Entry is not path-able
                return null;
            }
        }
        return entries;
    }

    private record Cache(Int2ObjectOpenHashMap<Entry<?>> entries, NBTCompound compound) implements TagReadable {
        static final Cache EMPTY = new Cache(new Int2ObjectOpenHashMap<>(), NBTCompound.EMPTY);

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            if (tag.isView()) return tag.read(compound);
            return read(entries, tag);
        }
    }

    private sealed interface Entry<T>
            permits TagEntry, PathEntry {
        Tag<T> tag();

        T value();

        NBT updatedNbt();
    }

    private static final class TagEntry<T> implements Entry<T> {
        private final Tag<T> tag;
        private final T value;
        volatile NBT nbt;

        TagEntry(Tag<T> tag, T value) {
            this.tag = tag;
            this.value = value;
        }

        @Override
        public Tag<T> tag() {
            return tag;
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public NBT updatedNbt() {
            NBT nbt = this.nbt;
            if (nbt == null) this.nbt = nbt = tag.entry.write().apply(value);
            return nbt;
        }
    }

    private record PathEntry(Tag<TagHandlerImpl> tag,
                             TagHandlerImpl value) implements Entry<TagHandlerImpl> {
        PathEntry(String key, TagHandlerImpl value) {
            this(Tag.tag(key, Serializers.PATH), value);
        }

        @Override
        public NBT updatedNbt() {
            return value.asCompound();
        }
    }

    final class SPMCMap extends Int2ObjectOpenHashMap<Entry<?>> {
        boolean rehashed;

        SPMCMap() {
        }

        SPMCMap(Int2ObjectMap<TagHandlerImpl.Entry<?>> m) {
            super(m);
        }

        @Override
        public TagHandlerImpl.Entry<?> put(int k, TagHandlerImpl.Entry<?> entry) {
            assertState();
            return super.put(k, entry);
        }

        @Override
        public boolean remove(int k, Object v) {
            assertState();
            return super.remove(k, v);
        }

        @Override
        protected void rehash(int newSize) {
            assertState();
            TagHandlerImpl.this.entries = new SPMCMap(this);
            this.rehashed = true;
        }

        @Override
        public SPMCMap clone() {
            return (SPMCMap) super.clone();
        }

        private void assertState() {
            assert !rehashed;
        }
    }
}
