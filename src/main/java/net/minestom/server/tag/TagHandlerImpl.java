package net.minestom.server.tag;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

final class TagHandlerImpl implements TagHandler {
    private static final boolean CACHE_ENABLE = PropertyUtils.getBoolean("minestom.tag-handler-cache", true);
    static final Serializers.Entry<Node, NBTCompound> NODE_SERIALIZER = new Serializers.Entry<>(NBTType.TAG_Compound, entries -> fromCompound(entries).root, Node::compound, true);

    private final AtomicInteger stamp = new AtomicInteger();
    private final Node root;

    TagHandlerImpl(Node root) {
        this.root = root;
    }

    TagHandlerImpl() {
        this.root = new Node();
    }

    static TagHandlerImpl fromCompound(NBTCompoundLike compoundLike) {
        final NBTCompound compound = compoundLike.toCompound();
        TagHandlerImpl handler = new TagHandlerImpl();
        TagNbtSeparator.separate(compound, entry -> handler.setTag(entry.tag(), entry.value()));
        handler.root.compound = compound;
        return handler;
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        VarHandle.fullFence();
        return root.getTag(tag);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        if (tag.isView()) {
            final boolean present = value != null;
            final Tag.PathEntry[] paths = tag.path;
            Node node = traversePathWrite(root, paths, present);
            if (node == null) return;
            node.updateContent(present ? (NBTCompound) tag.entry.write(value) : NBTCompound.EMPTY);
            if (!present && paths != null) recursiveClean(node, paths);
            VarHandle.fullFence();
            return;
        }

        final int initialStamp = stamp.get();
        final Node node = traversePathWrite(root, tag.path, value != null);
        if (node == null)
            return; // Tried to remove an absent tag. Do nothing
        if (tag.path != null) setTagWithPath(node, tag, value);
        else setTagNoPath(node, tag, value);
        // Tag handler mutation potentially failed (node map has been rehashed, changes may therefore not be visible)
        // The barrier also ensure propagation of cache invalidation
        final int finalStamp = stamp.get();
        if (initialStamp != finalStamp) setTag(tag, value);
    }

    private <T> void setTagWithPath(@NotNull Node node, @NotNull Tag<T> tag, @Nullable T value) {
        final int tagIndex = tag.index;
        StaticIntMap<Entry<?>> entries = node.entries;
        if (value != null) {
            Entry previous = entries.get(tagIndex);
            if (previous != null && previous.tag().shareValue(tag)) {
                previous.updateValue(tag.copyValue(value));
            } else {
                entries.put(tagIndex, valueToEntry(node, tag, value));
            }
            Node tmp = node;
            do {
                tmp.invalidate();
            } while ((tmp = tmp.parent) != null);
        } else {
            // Remove recursively
            if (entries.getAndRemove(tagIndex) == null) return;
            recursiveClean(node, tag.path);
        }
    }

    private void recursiveClean(Node node, Tag.PathEntry[] paths) {
        int i = paths.length;
        do {
            i--;
            node.invalidate();
            if (node.entries.isEmpty() && node.parent != null) {
                node.parent.entries.remove(paths[i].index());
            }
        } while ((node = node.parent) != null);
    }

    private <T> void setTagNoPath(@NotNull Node node, @NotNull Tag<T> tag, @Nullable T value) {
        final int tagIndex = tag.index;
        StaticIntMap<Entry<?>> entries = node.entries;
        if (value != null) {
            Entry previous = entries.get(tagIndex);
            if (previous != null && previous.tag().shareValue(tag)) {
                previous.updateValue(tag.copyValue(value));
            } else {
                entries.put(tagIndex, valueToEntry(node, tag, value));
            }
        } else {
            entries.remove(tagIndex);
        }
        node.invalidate();
    }

    private <T> Entry<?> valueToEntry(Node parent, Tag<T> tag, @NotNull T value) {
        if (value instanceof NBT nbt) {
            if (nbt instanceof NBTCompound compound) {
                final TagHandlerImpl handler = fromCompound(compound);
                return Entry.makePathEntry(tag, new Node(parent, handler.root.entries));
            } else {
                final var nbtEntry = TagNbtSeparator.separateSingle(tag.getKey(), nbt);
                return new Entry<>(nbtEntry.tag(), nbtEntry.value());
            }
        } else {
            return new Entry<>(tag, tag.copyValue(value));
        }
    }

    private Node traversePathWrite(Node root, Tag.PathEntry[] paths,
                                   boolean present) {
        if (paths == null) return root;
        Node local = root;
        for (Tag.PathEntry path : paths) {
            final int pathIndex = path.index();
            final Entry<?> entry = local.entries.get(pathIndex);
            if (entry != null && entry.tag.entry.isPath()) {
                // Existing path, continue navigating
                final Node tmp = (Node) entry.value;
                assert tmp.parent == local : "Path parent is invalid: " + tmp.parent + " != " + local;
                local = tmp;
            } else {
                if (!present) return null;
                // Empty path, create a new handler.
                // Slow path is taken if the entry comes from a Structure tag, requiring conversion from NBT
                Node tmp = local;
                local = new Node(tmp);
                if (entry != null && entry.nbt() instanceof NBTCompound compound) {
                    local.updateContent(compound);
                }
                tmp.entries.put(pathIndex, Entry.makePathEntry(path.name(), local));
            }
        }
        return local;
    }

    @Override
    public <T> void updateTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        updateAndGetTag(tag, value);
    }

    @Override
    public <T> @UnknownNullability T updateAndGetTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        return updateTag0(tag, value, false);
    }

    @Override
    public <T> @UnknownNullability T getAndUpdateTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        return updateTag0(tag, value, true);
    }

    private <T> T updateTag0(@NotNull Tag<T> tag, @NotNull UnaryOperator<T> value, boolean returnPrevious) {
        final int tagIndex = tag.index;
        final int initialStamp = stamp.get();
        Node node = traversePathWrite(root, tag.path, true);
        // Mutate
        Entry previousEntry = node.entries.get(tagIndex);
        final Object previousValue = previousEntry != null ? previousEntry.value() : tag.createDefault();
        final T newValue = value.apply((T) previousValue);
        Entry newEntry = newValue != null ? valueToEntry(node, tag, newValue) : null;
        if (!node.entries.compareAndSet(tagIndex, previousEntry, newEntry)) {
            return updateTag0(tag, value, returnPrevious);
        }
        // Invalidate the node parents
        if (newValue == null && tag.path != null) recursiveClean(node, tag.path);
        // Verify visibility
        if (initialStamp != stamp.get()) return updateTag0(tag, value, returnPrevious);
        else return returnPrevious ? (T) previousValue : newValue;
    }

    @Override
    public @NotNull TagReadable readableCopy() {
        return root.copy(null);
    }

    @Override
    public @NotNull TagHandler copy() {
        return new TagHandlerImpl(root.copy(null));
    }

    @Override
    public void updateContent(@NotNull NBTCompoundLike compound) {
        this.root.updateContent(compound);
    }

    @Override
    public @NotNull NBTCompound asCompound() {
        VarHandle.fullFence();
        return root.compound();
    }

    private static <T> T read(Node node, Tag<T> tag) {
        if ((node = traversePathRead(node, tag)) == null)
            return tag.createDefault(); // Must be a path-able entry, but not present
        if (tag.isView()) return tag.read(node.compound());

        final StaticIntMap<Entry<?>> entries = node.entries;
        final Entry<?> entry = entries.get(tag.index);
        if (entry == null)
            return tag.createDefault(); // Not present
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

    private static Node traversePathRead(Node node, Tag<?> tag) {
        final Tag.PathEntry[] paths = tag.path;
        if (paths == null) return node;
        for (var path : paths) {
            final Entry<?> entry = node.entries.get(path.index());
            if (entry == null) return null;
            node = entry.toNode();
            if (node == null) return null;
        }
        return node;
    }

    final class Node implements TagReadable {
        final Node parent;
        final StaticIntMap<Entry<?>> entries;
        NBTCompound compound;

        public Node(Node parent, StaticIntMap<Entry<?>> entries) {
            this.parent = parent;
            this.entries = entries;
        }

        Node(Node parent) {
            this(parent, new StaticIntMap.Array<>(stamp));
        }

        Node() {
            this(null);
        }

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return read(this, tag);
        }

        void updateContent(@NotNull NBTCompoundLike compoundLike) {
            final NBTCompound compound = compoundLike.toCompound();
            final TagHandlerImpl converted = fromCompound(compound);
            this.entries.updateContent(converted.root.entries);
            this.compound = compound;
        }

        NBTCompound compound() {
            NBTCompound compound;
            if (!CACHE_ENABLE || (compound = this.compound) == null) {
                MutableNBTCompound tmp = new MutableNBTCompound();
                this.entries.forValues(entry -> tmp.put(entry.tag().getKey(), entry.nbt()));
                this.compound = compound = tmp.toCompound();
            }
            return compound;
        }

        Node copy(Node parent) {
            MutableNBTCompound tmp = new MutableNBTCompound();
            Node result = new Node(parent, new StaticIntMap.Array<>(stamp));
            StaticIntMap<Entry<?>> entries = result.entries;
            this.entries.forValues(entry -> {
                Tag tag = entry.tag;
                Object value = entry.value;
                NBT nbt;
                if (value instanceof Node node) {
                    Node copy = node.copy(result);
                    value = copy;
                    nbt = copy.compound;
                    assert nbt != null : "Node copy should also compute the compound";
                } else {
                    nbt = entry.nbt();
                }

                tmp.put(tag.getKey(), nbt);
                entries.put(tag.index, valueToEntry(result, tag, value));
            });

            result.compound = tmp.toCompound();
            return result;
        }

        void invalidate() {
            this.compound = null;
        }
    }

    private static final class Entry<T> {
        private final Tag<T> tag;
        T value;
        NBT nbt;

        Entry(Tag<T> tag, T value) {
            this.tag = tag;
            this.value = value;
        }

        static Entry<?> makePathEntry(String path, Node node) {
            return new Entry<>(Tag.tag(path, NODE_SERIALIZER), node);
        }

        static Entry<?> makePathEntry(Tag<?> tag, Node node) {
            return makePathEntry(tag.getKey(), node);
        }

        public Tag<T> tag() {
            return tag;
        }

        public T value() {
            return value;
        }

        public NBT nbt() {
            if (tag.entry.isPath()) return ((Node) value).compound();
            NBT nbt = this.nbt;
            if (nbt == null) this.nbt = nbt = tag.entry.write(value);
            return nbt;
        }

        public void updateValue(T value) {
            assert !tag.entry.isPath();
            this.value = value;
            this.nbt = null;
        }

        Node toNode() {
            if (tag.entry.isPath()) return (Node) value;
            if (nbt() instanceof NBTCompound compound) {
                // Slow path forcing a conversion of the structure to NBTCompound
                // TODO should the handler be cached inside the entry?
                return fromCompound(compound).root;
            }
            // Entry is not path-able
            return null;
        }
    }
}
