package net.minestom.server.tag;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.utils.PropertyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.lang.invoke.VarHandle;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

final class TagHandlerImpl implements TagHandler {
    private static final boolean CACHE_ENABLE = PropertyUtils.getBoolean("minestom.tag-handler-cache", true);

    private final TagHandlerImpl parent;
    private volatile SPMCMap entries;
    private Cache cache;

    TagHandlerImpl(TagHandlerImpl parent) {
        this.parent = parent;
        this.entries = new SPMCMap(this);
        this.cache = null;
    }

    TagHandlerImpl() {
        this(null);
    }

    static TagHandlerImpl fromCompound(NBTCompoundLike compoundLike) {
        final NBTCompound compound = compoundLike.toCompound();
        TagHandlerImpl handler = new TagHandlerImpl(null);
        TagNbtSeparator.separate(compound, entry -> handler.setTag(entry.tag(), entry.value()));
        return handler;
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return read(entries, tag, this::asCompound);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        TagHandlerImpl local = this;
        final Tag.PathEntry[] paths = tag.path;
        final boolean present = value != null;
        final int tagIndex = tag.index;
        final boolean isView = tag.isView();
        synchronized (this) {
            if (paths != null) {
                if ((local = traversePathWrite(this, paths, present)) == null)
                    return; // Tried to remove an absent tag. Do nothing
            }
            SPMCMap entries = local.entries;
            if (present) {
                if (!isView) {
                    Entry previous = entries.get(tagIndex);
                    if (previous != null && previous.tag().shareValue(tag)) {
                        previous.updateValue(value);
                    } else {
                        entries.put(tagIndex, valueToEntry(local, tag, value));
                    }
                } else {
                    local.updateContent((NBTCompound) tag.entry.write(value));
                    return;
                }
            } else {
                // Remove recursively
                if (!isView) {
                    if (entries.remove(tagIndex) == null) return;
                } else {
                    entries.clear();
                }
                if (paths != null) {
                    TagHandlerImpl tmp = local;
                    int i = paths.length;
                    do {
                        if (!tmp.entries.isEmpty()) break;
                        tmp = tmp.parent;
                        tmp.entries.remove(paths[--i].index());
                    } while (i > 0);
                }
            }
            entries.invalidate();
            assert !local.entries.rehashed;
        }
    }

    private <T> Entry<?> valueToEntry(TagHandlerImpl parent, Tag<T> tag, @NotNull T value) {
        if (value instanceof NBT nbt) {
            if (nbt instanceof NBTCompound compound) {
                var handler = new TagHandlerImpl(parent);
                handler.updateContent(compound);
                return new PathEntry(tag.getKey(), handler);
            } else {
                final var nbtEntry = TagNbtSeparator.separateSingle(tag.getKey(), nbt);
                return new TagEntry<>(nbtEntry.tag(), nbtEntry.value());
            }
        } else {
            final UnaryOperator<T> copy = tag.copy;
            if (copy != null) value = copy.apply(value);
            return new TagEntry<>(tag, value);
        }
    }

    @Override
    public synchronized <T> void updateTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        setTag(tag, value.apply(getTag(tag)));
    }

    @Override
    public synchronized <T> @UnknownNullability T updateAndGetTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        final T next = value.apply(getTag(tag));
        setTag(tag, next);
        return next;
    }

    @Override
    public synchronized <T> @UnknownNullability T getAndUpdateTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        final T prev = getTag(tag);
        setTag(tag, value.apply(prev));
        return prev;
    }

    @Override
    public @NotNull TagReadable readableCopy() {
        return updatedCache();
    }

    @Override
    public @NotNull TagHandler copy() {
        return fromCompound(asCompound());
    }

    @Override
    public void updateContent(@NotNull NBTCompoundLike compound) {
        final TagHandlerImpl converted = fromCompound(compound);
        synchronized (this) {
            this.cache = converted.cache;
            this.entries = new SPMCMap(this, converted.entries);
        }
    }

    @Override
    public @NotNull NBTCompound asCompound() {
        return updatedCache().compound;
    }

    private static TagHandlerImpl traversePathWrite(TagHandlerImpl root, Tag.PathEntry[] paths,
                                                    boolean present) {
        TagHandlerImpl local = root;
        for (Tag.PathEntry path : paths) {
            final int pathIndex = path.index();
            final Entry<?> entry = local.entries.get(pathIndex);
            if (entry instanceof PathEntry pathEntry) {
                // Existing path, continue navigating
                assert pathEntry.value.parent == local : "Path parent is invalid: " + pathEntry.value.parent + " != " + local;
                local = pathEntry.value;
            } else {
                if (!present) return null;
                // Empty path, create a new handler.
                // Slow path is taken if the entry comes from a Structure tag, requiring conversion from NBT
                TagHandlerImpl tmp = local;
                local = new TagHandlerImpl(tmp);
                if (entry != null && entry.nbt() instanceof NBTCompound compound) {
                    local.updateContent(compound);
                }
                tmp.entries.put(pathIndex, new PathEntry(path.name(), local));
            }
        }
        return local;
    }

    private synchronized Cache updatedCache() {
        Cache cache;
        if (!CACHE_ENABLE || (cache = this.cache) == null) {
            final SPMCMap entries = this.entries;
            if (!entries.isEmpty()) {
                MutableNBTCompound tmp = new MutableNBTCompound();
                for (Entry<?> entry : entries.values()) {
                    if (entry != null) tmp.put(entry.tag().getKey(), entry.nbt());
                }
                cache = new Cache(entries.clone(), tmp.toCompound());
            } else cache = Cache.EMPTY;
            this.cache = cache;
        }
        return cache;
    }

    private static <T> T read(Int2ObjectOpenHashMap<Entry<?>> entries, Tag<T> tag,
                              Supplier<NBTCompound> rootCompoundSupplier) {
        final Tag.PathEntry[] paths = tag.path;
        TagHandlerImpl pathHandler = null;
        if (paths != null) {
            if ((pathHandler = traversePathRead(paths, entries)) == null)
                return tag.createDefault(); // Must be a path-able entry, but not present
            entries = pathHandler.entries;
        }

        if (tag.isView()) {
            return tag.read(pathHandler != null ?
                    pathHandler.asCompound() : rootCompoundSupplier.get());
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
        final NBT nbt = entry.nbt();
        final Serializers.Entry<T, NBT> serializerEntry = tag.entry;
        final NBTType<NBT> type = serializerEntry.nbtType();
        return type == null || type == nbt.getID() ? serializerEntry.read(nbt) : tag.createDefault();
    }

    private static TagHandlerImpl traversePathRead(Tag.PathEntry[] paths,
                                                   Int2ObjectOpenHashMap<Entry<?>> entries) {
        assert paths != null && paths.length > 0;
        TagHandlerImpl result = null;
        for (var path : paths) {
            final Entry<?> entry;
            if ((entry = entries.get(path.index())) == null)
                return null;
            if (entry instanceof PathEntry pathEntry) {
                result = pathEntry.value;
            } else if (entry.nbt() instanceof NBTCompound compound) {
                // Slow path forcing a conversion of the structure to NBTCompound
                // TODO should the handler be cached inside the entry?
                result = fromCompound(compound);
            } else {
                // Entry is not path-able
                return null;
            }
            assert result != null;
            entries = result.entries;
        }
        assert result != null;
        return result;
    }

    private record Cache(Int2ObjectOpenHashMap<Entry<?>> entries, NBTCompound compound) implements TagReadable {
        static final Cache EMPTY = new Cache(new Int2ObjectOpenHashMap<>(), NBTCompound.EMPTY);

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return read(entries, tag, () -> compound);
        }
    }

    private sealed interface Entry<T>
            permits TagEntry, PathEntry {
        Tag<T> tag();

        T value();

        NBT nbt();

        void updateValue(T value);
    }

    private static final class TagEntry<T> implements Entry<T> {
        private final Tag<T> tag;
        volatile T value;
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
        public NBT nbt() {
            NBT nbt = this.nbt;
            if (nbt == null) this.nbt = nbt = tag.entry.write(value);
            return nbt;
        }

        @Override
        public void updateValue(T value) {
            this.value = value;
            this.nbt = null;
        }
    }

    private record PathEntry(Tag<TagHandlerImpl> tag,
                             TagHandlerImpl value) implements Entry<TagHandlerImpl> {
        PathEntry(String key, TagHandlerImpl value) {
            this(Tag.tag(key, Serializers.PATH), value);
        }

        @Override
        public NBTCompound nbt() {
            return value.asCompound();
        }

        @Override
        public void updateValue(TagHandlerImpl value) {
            throw new UnsupportedOperationException("Cannot update a path entry");
        }
    }

    static final class SPMCMap extends Int2ObjectOpenHashMap<Entry<?>> {
        final TagHandlerImpl handler;
        volatile boolean rehashed;

        SPMCMap(TagHandlerImpl handler) {
            super();
            this.handler = handler;
            assertState();
        }

        SPMCMap(TagHandlerImpl handler, Int2ObjectMap<TagHandlerImpl.Entry<?>> m) {
            super(m.size(), DEFAULT_LOAD_FACTOR);
            this.handler = handler;
            assertState();
            putAll(m);
        }

        @Override
        protected void rehash(int newSize) {
            assertState();
            this.handler.entries = new SPMCMap(handler, this);
            this.rehashed = true;
        }

        @Override
        public SPMCMap clone() {
            return (SPMCMap) super.clone();
        }

        void invalidate() {
            if (!CACHE_ENABLE) return;
            TagHandlerImpl tmp = handler;
            do {
                tmp.cache = null;
            } while ((tmp = tmp.parent) != null);
            VarHandle.fullFence();
        }

        private void assertState() {
            assert !rehashed;
            assert handler != null;
        }
    }
}
