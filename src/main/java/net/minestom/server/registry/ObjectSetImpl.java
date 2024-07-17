package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

sealed interface ObjectSetImpl extends ObjectSet permits ObjectSetImpl.Empty, ObjectSetImpl.Entries, ObjectSetImpl.Tag {

    record Empty() implements ObjectSetImpl {
        static final Empty INSTANCE = new Empty();

        @Override
        public boolean contains(@NotNull Key namespace) {
            return false;
        }
    }

    record Entries(@NotNull List<Key> entries) implements ObjectSetImpl {

        public Entries {
            entries = List.copyOf(entries);
        }

        @Override
        public boolean contains(@NotNull Key key) {
            return entries.contains(key);
        }
    }

    final class Tag implements ObjectSetImpl {
        private final net.minestom.server.gamedata.tags.Tag.BasicType tagType;
        private final String name;
        private volatile Set<Key> value = null;

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

        public Set<Key> value() {
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
        public boolean contains(@NotNull Key key) {
            return value().contains(key);
        }
    }

    record NetworkType(
            @NotNull net.minestom.server.gamedata.tags.Tag.BasicType tagType
    ) implements NetworkBuffer.Type<ObjectSet> {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ObjectSet value) {
            throw new UnsupportedOperationException("todo");
        }

        @Override
        public ObjectSet read(@NotNull NetworkBuffer buffer) {
            throw new UnsupportedOperationException("todo");
        }
    }

    record CodecImpl(
            @NotNull net.minestom.server.gamedata.tags.Tag.BasicType tagType
    ) implements Codec<ObjectSet> {
        private static final Codec<Entries> ENTRIES_CODEC = Codec.KEY.list()
                .transform(Entries::new, Entries::entries);

        @Override
        public @NotNull Result<ObjectSet> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Entries> entriesResult = ENTRIES_CODEC.decode(coder, value);
            if (entriesResult instanceof Result.Ok(Entries entries)) {
                //noinspection unchecked
                return new Result.Ok<>((ObjectSet) entries);
            }

            final Result<String> stringResult = coder.getString(value);
            if (!(stringResult instanceof Result.Ok(String string))) {
                return stringResult.cast();
            }

            // Could be a tag or a block name depending if it starts with a #
            return new Result.Ok<>(string.startsWith("#")
                    ? new Tag(tagType(), string.substring(1))
                    : new Entries(List.of(Key.key(string))));
        }

        @Override
        public @NotNull Result encode(@NotNull Transcoder<D> coder, @Nullable ObjectSet value) {
            if (value == null) return new Result.Error<>("null");
            return new Result.Ok<>(switch (value) {
                case Empty empty -> coder.emptyList();
                case Entries entries -> {
                    if (entries.entries.size() == 1)
                        yield coder.createString(entries.entries.stream().findFirst().get().asString());
                    final Transcoder.ListBuilder<D> list = coder.createList(entries.entries.size());
                    for (Key entry : entries.entries)
                        list.add(coder.createString(entry.asString()));
                    yield list.build();
                }
                case Tag tag -> coder.createString("#" + tag.name());
            });
        }
    }

}
