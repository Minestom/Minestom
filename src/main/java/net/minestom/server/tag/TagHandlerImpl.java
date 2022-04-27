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
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.lang.invoke.VarHandle;
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
        if (tag.isView()) return tag.read(asCompound());
        return read(entries, tag);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        if (tag.isView()) {
            MutableNBTCompound viewCompound = new MutableNBTCompound();
            tag.write(viewCompound, value);
            updateContent(viewCompound);
        } else {
            final int tagIndex = tag.index;
            final Tag.PathEntry[] paths = tag.path;
            final boolean present = value != null;
            TagHandlerImpl local = this;
            synchronized (this) {
                if (paths != null) {
                    if ((local = traversePathWrite(this, paths, present)) == null)
                        return; // Tried to remove an absent tag. Do nothing
                }
                SPMCMap entries = local.entries;
                if (present) {
                    entries.put(tagIndex, valueToEntry(local, tag, value));
                } else {
                    // Remove recursively
                    if (entries.remove(tagIndex) == null) return;
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
                if (entry != null && entry.updatedNbt() instanceof NBTCompound compound) {
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
            if ((entries = traversePathRead(paths, entries)) == null)
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
        final Serializers.Entry<T, NBT> serializerEntry = tag.entry;
        return serializerEntry.nbtType().isAssignableFrom(nbt.getClass()) ?
                serializerEntry.read(nbt) : tag.createDefault();
    }

    private static Int2ObjectOpenHashMap<Entry<?>> traversePathRead(Tag.PathEntry[] paths,
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
            if (nbt == null) this.nbt = nbt = tag.entry.write(value);
            return nbt;
        }
    }

    private record PathEntry(Tag<TagHandlerImpl> tag,
                             TagHandlerImpl value) implements Entry<TagHandlerImpl> {
        PathEntry(String key, TagHandlerImpl value) {
            this(Tag.tag(key, Serializers.PATH), value);
        }

        @Override
        public NBTCompound updatedNbt() {
            return value.asCompound();
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
