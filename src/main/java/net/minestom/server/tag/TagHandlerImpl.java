package net.minestom.server.tag;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.ServerFlag;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.function.UnaryOperator;

final class TagHandlerImpl implements TagHandler {
    static final Serializers.Entry<Node, CompoundBinaryTag> NODE_SERIALIZER = new Serializers.Entry<>(BinaryTagTypes.COMPOUND, entries -> fromCompound(entries).root, Node::compound, true);

    private final Node root;
    private volatile Node copy;

    TagHandlerImpl(Node root) {
        this.root = root;
    }

    TagHandlerImpl() {
        this.root = new Node();
    }

    static TagHandlerImpl fromCompound(CompoundBinaryTag compound) {
        TagHandlerImpl handler = new TagHandlerImpl();
        TagNbtSeparator.separate(compound, entry -> handler.setTag(entry.tag(), entry.value()));
        handler.root.compound = compound;
        return handler;
    }

    @Override
    public <T> @UnknownNullability T getTag(Tag<T> tag) {
        VarHandle.fullFence();
        return root.getTag(tag);
    }

    @Override
    public <T> void setTag(Tag<T> tag, @Nullable T value) {
        // Handle view tags
        if (tag.isView()) {
            synchronized (this) {
                Node syncNode = traversePathWrite(root, tag, value != null);
                if (syncNode != null) {
                    syncNode.updateContent(value != null ? (CompoundBinaryTag) tag.entry.write(value) : CompoundBinaryTag.empty());
                    syncNode.invalidate();
                }
            }
            return;
        }
        // Normal tag
        final int tagIndex = tag.index;
        VarHandle.fullFence();
        Node node = traversePathWrite(root, tag, value != null);
        if (node == null)
            return; // Tried to remove an absent tag. Do nothing
        StaticIntMap<Entry<?>> entries = node.entries;
        if (value != null) {
            Entry previous = entries.get(tagIndex);
            if (previous != null && previous.tag.shareValue(tag)) {
                previous.updateValue(tag.copyValue(value));
            } else {
                synchronized (this) {
                    node = traversePathWrite(root, tag, true);
                    node.entries.put(tagIndex, valueToEntry(node, tag, value));
                }
            }
        } else {
            synchronized (this) {
                node = traversePathWrite(root, tag, false);
                if (node == null) return;
                node.entries.remove(tagIndex);
            }
        }
        node.invalidate();
    }

    @Override
    public <T> @Nullable T getAndSetTag(Tag<T> tag, @Nullable T value) {
        return updateTag0(tag, t -> value, true);
    }

    @Override
    public <T> void updateTag(Tag<T> tag, UnaryOperator<@UnknownNullability T> value) {
        updateTag0(tag, value, false);
    }

    @Override
    public <T> @UnknownNullability T updateAndGetTag(Tag<T> tag, UnaryOperator<@UnknownNullability T> value) {
        return updateTag0(tag, value, false);
    }

    @Override
    public <T> @UnknownNullability T getAndUpdateTag(Tag<T> tag, UnaryOperator<@UnknownNullability T> value) {
        return updateTag0(tag, value, true);
    }

    private synchronized <T> T updateTag0(Tag<T> tag, UnaryOperator<T> value, boolean returnPrevious) {
        final Node node = traversePathWrite(root, tag, true);
        if (tag.isView()) {
            final T previousValue = tag.read(node.compound());
            final T newValue = value.apply(previousValue);
            node.updateContent((CompoundBinaryTag) tag.entry.write(newValue));
            node.invalidate();
            return returnPrevious ? previousValue : newValue;
        }

        final int tagIndex = tag.index;
        StaticIntMap<Entry<?>> entries = node.entries;

        final Entry previousEntry = entries.get(tagIndex);
        final T previousValue;
        if (previousEntry != null) {
            final Object previousTmp = previousEntry.value;
            if (previousTmp instanceof Node n) {
                final CompoundBinaryTag compound = CompoundBinaryTag.from(Map.of(tag.getKey(), n.compound()));
                previousValue = tag.read(compound);
            } else {
                previousValue = (T) previousTmp;
            }
        } else {
            previousValue = tag.createDefault();
        }
        final T newValue = value.apply(previousValue);
        if (newValue != null) entries.put(tagIndex, valueToEntry(node, tag, newValue));
        else entries.remove(tagIndex);

        node.invalidate();
        return returnPrevious ? previousValue : newValue;
    }

    @Override
    public TagReadable readableCopy() {
        Node copy = this.copy;
        if (copy == null) {
            synchronized (this) {
                this.copy = copy = root.copy(null);
            }
        }
        return copy;
    }

    @Override
    public synchronized TagHandler copy() {
        return new TagHandlerImpl(root.copy(null));
    }

    @Override
    public synchronized void updateContent(CompoundBinaryTag compound) {
        this.root.updateContent(compound);
    }

    @Override
    public CompoundBinaryTag asCompound() {
        VarHandle.fullFence();
        return root.compound();
    }

    private static Node traversePathRead(Node node, Tag<?> tag) {
        final Tag.PathEntry[] paths = tag.path;
        if (paths == null) return node;
        for (var path : paths) {
            final Entry<?> entry = node.entries.get(path.index());
            if (entry == null || (node = entry.toNode()) == null)
                return null;
        }
        return node;
    }

    @Contract("_, _, true -> !null")
    private Node traversePathWrite(Node root, Tag<?> tag,
                                   boolean present) {
        final Tag.PathEntry[] paths = tag.path;
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
                synchronized (this) {
                    var synEntry = local.entries.get(pathIndex);
                    if (synEntry != null && synEntry.tag.entry.isPath()) {
                        // Existing path, continue navigating
                        final Node tmp = (Node) synEntry.value;
                        assert tmp.parent == local : "Path parent is invalid: " + tmp.parent + " != " + local;
                        local = tmp;
                        continue;
                    }

                    // Empty path, create a new handler.
                    // Slow path is taken if the entry comes from a Structure tag, requiring conversion from NBT
                    Node tmp = local;
                    local = new Node(tmp);
                    if (synEntry != null && synEntry.updatedNbt() instanceof CompoundBinaryTag compound) {
                        local.updateContent(compound);
                    }
                    tmp.entries.put(pathIndex, Entry.makePathEntry(path.name(), local));
                }
            }
        }
        return local;
    }

    private <T> Entry<?> valueToEntry(Node parent, Tag<T> tag, T value) {
        if (value instanceof BinaryTag nbt) {
            if (nbt instanceof CompoundBinaryTag compound) {
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

    final class Node implements TagReadable {
        final Node parent;
        final StaticIntMap<Entry<?>> entries;
        CompoundBinaryTag compound;

        public Node(Node parent, StaticIntMap<Entry<?>> entries) {
            this.parent = parent;
            this.entries = entries;
        }

        Node(Node parent) {
            this(parent, new StaticIntMap.Array<>());
        }

        Node() {
            this(null);
        }

        @Override
        public <T> @UnknownNullability T getTag(Tag<T> tag) {
            final Node node = traversePathRead(this, tag);
            if (node == null)
                return tag.createDefault(); // Must be a path-able entry, but not present
            if (tag.isView()) return tag.read(node.compound());

            final StaticIntMap<Entry<?>> entries = node.entries;
            final Entry<?> entry = entries.get(tag.index);
            if (entry == null)
                return tag.createDefault(); // Not present
            if (entry.tag.shareValue(tag)) {
                // The tag used to write the entry is compatible with the one used to get
                // return the value directly
                //noinspection unchecked
                return (T) entry.value;
            }
            // Value must be parsed from nbt if the tag is different
            final BinaryTag nbt = entry.updatedNbt();
            final Serializers.Entry<T, BinaryTag> serializerEntry = tag.entry;
            final BinaryTagType<BinaryTag> type = serializerEntry.nbtType();
            return type == null || type.equals(nbt.type()) ? serializerEntry.read(nbt) : tag.createDefault();
        }

        void updateContent(CompoundBinaryTag compound) {
            final TagHandlerImpl converted = fromCompound(compound);
            this.entries.updateContent(converted.root.entries);
            this.compound = compound;
        }

        CompoundBinaryTag compound() {
            CompoundBinaryTag compound;
            if (!ServerFlag.TAG_HANDLER_CACHE_ENABLED || (compound = this.compound) == null) {
                CompoundBinaryTag.Builder tmp = CompoundBinaryTag.builder();
                this.entries.forValues(entry -> {
                    final Tag tag = entry.tag;
                    final BinaryTag nbt = entry.updatedNbt();
                    if (nbt != null && (!tag.entry.isPath() || (!ServerFlag.SERIALIZE_EMPTY_COMPOUND) && ((CompoundBinaryTag) nbt).size() > 0)) {
                        tmp.put(tag.getKey(), nbt);
                    }
                });
                this.compound = compound = tmp.build();
            }
            return compound;
        }

        @Contract("null -> !null")
        Node copy(Node parent) {
            CompoundBinaryTag.Builder tmp = CompoundBinaryTag.builder();
            Node result = new Node(parent, new StaticIntMap.Array<>());
            StaticIntMap<Entry<?>> entries = result.entries;
            this.entries.forValues(entry -> {
                Tag tag = entry.tag;
                Object value = entry.value;
                BinaryTag nbt;
                if (value instanceof Node node) {
                    Node copy = node.copy(result);
                    if (copy == null)
                        return; // Empty node
                    value = copy;
                    nbt = copy.compound;
                    assert nbt != null : "Node copy should also compute the compound";
                } else {
                    nbt = entry.updatedNbt();
                }

                if (nbt != null)
                    tmp.put(tag.getKey(), nbt);
                entries.put(tag.index, valueToEntry(result, tag, value));
            });
            var compound = tmp.build();
            if ((!ServerFlag.SERIALIZE_EMPTY_COMPOUND) && compound.size() == 0 && parent != null)
                return null; // Empty child node
            result.compound = compound;
            return result;
        }

        void invalidate() {
            Node tmp = this;
            do tmp.compound = null;
            while ((tmp = tmp.parent) != null);
            TagHandlerImpl.this.copy = null;
        }
    }

    private static final class Entry<T> {
        private final Tag<T> tag;
        T value;
        BinaryTag nbt;

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

        BinaryTag updatedNbt() {
            if (tag.entry.isPath()) return ((Node) value).compound();
            BinaryTag nbt = this.nbt;
            if (nbt == null) this.nbt = nbt = tag.entry.write(value);
            return nbt;
        }

        void updateValue(T value) {
            assert !tag.entry.isPath();
            this.value = value;
            this.nbt = null;
        }

        Node toNode() {
            if (tag.entry.isPath()) return (Node) value;
            if (updatedNbt() instanceof CompoundBinaryTag compound) {
                // Slow path forcing a conversion of the structure to NBTCompound
                // TODO should the handler be cached inside the entry?
                return fromCompound(compound).root;
            }
            // Entry is not path-able
            return null;
        }
    }
}
