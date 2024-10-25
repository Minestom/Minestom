package net.minestom.server.registry;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag;

sealed interface ObjectSetImpl<T extends ProtocolObject> extends ObjectSet<T> permits ObjectSetImpl.Empty, ObjectSetImpl.Entries, ObjectSetImpl.Tag {

    record Empty<T extends ProtocolObject>() implements ObjectSetImpl<T> {
        static final Empty<?> INSTANCE = new Empty<>();

        @Override
        public boolean contains(@NotNull NamespaceID namespace) {
            return false;
        }
    }

    record Entries<T extends ProtocolObject>(@NotNull List<NamespaceID> entries) implements ObjectSetImpl<T> {

        public Entries {
            entries = List.copyOf(entries);
        }

        @Override
        public boolean contains(@NotNull NamespaceID namespace) {
            return entries.contains(namespace);
        }
    }

    final class Tag<T extends ProtocolObject> implements ObjectSetImpl<T> {
        private final net.minestom.server.gamedata.tags.Tag.BasicType tagType;
        private final String name;
        private volatile Set<NamespaceID> value = null;

        public Tag(@NotNull net.minestom.server.gamedata.tags.Tag.BasicType tagType, @NotNull String name) {
            this.tagType = tagType;
            this.name = name;
        }

        public Tag(@NotNull net.minestom.server.gamedata.tags.Tag tag) {
            this.tagType = null;
            this.name = tag.name();
            this.value = Set.copyOf(tag.getValues());
        }

        public @NotNull String name() {
            return name;
        }

        public Set<NamespaceID> value() {
            if (value == null) {
                synchronized (this) {
                    if (value == null) {
                        var group = MinecraftServer.getTagManager().getTag(tagType, name);
                        value = group == null ? Set.of() : Set.copyOf(group.getValues());
                    }
                }
            }
            return value;
        }

        @Override
        public boolean contains(@NotNull NamespaceID namespace) {
            return value().contains(namespace);
        }
    }

    record NetworkType<T extends ProtocolObject>(
            @NotNull net.minestom.server.gamedata.tags.Tag.BasicType tagType
    ) implements NetworkBuffer.Type<ObjectSet<T>> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ObjectSet<T> value) {
            throw new UnsupportedOperationException("todo");
        }

        @Override
        public ObjectSet<T> read(@NotNull NetworkBuffer buffer) {
            throw new UnsupportedOperationException("todo");
        }
    }

    record NbtType<T extends ProtocolObject>(
            @NotNull net.minestom.server.gamedata.tags.Tag.BasicType tagType
    ) implements BinaryTagSerializer<ObjectSet<T>> {

        @Override
        public @NotNull ObjectSet<T> read(@NotNull BinaryTag tag) {
            return switch (tag) {
                case null -> ObjectSet.empty();
                case ListBinaryTag list -> {
                    if (list.size() == 0) yield ObjectSet.empty();

                    final List<NamespaceID> entries = new ArrayList<>(list.size());
                    for (BinaryTag entryTag : list) {
                        if (!(entryTag instanceof StringBinaryTag stringTag))
                            throw new IllegalArgumentException("Invalid entry type: " + entryTag.type());
                        entries.add(NamespaceID.from(stringTag.value()));
                    }
                    yield new Entries<>(entries);
                }
                case StringBinaryTag string -> {
                    // Could be a tag or a block name depending if it starts with a #
                    final String value = string.value();
                    if (value.startsWith("#")) {
                        yield new Tag<>(tagType(), value.substring(1));
                    } else {
                        yield new Entries<>(List.of(NamespaceID.from(value)));
                    }
                }
                default -> throw new IllegalArgumentException("Invalid tag type: " + tag.type());
            };
        }

        @Override
        public @NotNull BinaryTag write(@NotNull ObjectSet<T> value) {
            return switch (value) {
                case Empty<T> empty -> ListBinaryTag.empty();
                case Entries<T> entries -> {
                    if (entries.entries.size() == 1)
                        yield stringBinaryTag(entries.entries.stream().findFirst().get().asString());
                    ListBinaryTag.Builder<StringBinaryTag> builder = ListBinaryTag.builder(BinaryTagTypes.STRING);
                    for (NamespaceID entry : entries.entries)
                        builder.add(stringBinaryTag(entry.asString()));
                    yield builder.build();
                }
                case Tag<T> tag -> stringBinaryTag("#" + tag.name());
            };
        }
    }

}
