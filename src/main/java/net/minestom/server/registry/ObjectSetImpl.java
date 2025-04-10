package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

sealed interface ObjectSetImpl<T extends Keyed> extends ObjectSet<T> permits ObjectSetImpl.Empty, ObjectSetImpl.Entries, ObjectSetImpl.Tag {

    record Empty<T extends Keyed>() implements ObjectSetImpl<T> {
        static final Empty<?> INSTANCE = new Empty<>();

        @Override
        public boolean contains(@NotNull Key namespace) {
            return false;
        }
    }

    record Entries<T extends Keyed>(@NotNull List<Key> entries) implements ObjectSetImpl<T> {

        public Entries {
            entries = List.copyOf(entries);
        }

        @Override
        public boolean contains(@NotNull Key key) {
            return entries.contains(key);
        }
    }

    final class Tag<T extends Keyed> implements ObjectSetImpl<T> {
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

    record NetworkType<T extends Keyed>(
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

    record CodecImpl<T extends Keyed>(
            @NotNull net.minestom.server.gamedata.tags.Tag.BasicType tagType
    ) implements Codec<ObjectSet<T>> {
        private static final Codec<Entries<?>> ENTRIES_CODEC = Codec.KEY.list()
                .transform(Entries::new, Entries::entries);

        @Override
        public @NotNull <D> Result<ObjectSet<T>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<Entries<?>> entriesResult = ENTRIES_CODEC.decode(coder, value);
            if (entriesResult instanceof Result.Ok(Entries<?> entries)) {
                //noinspection unchecked
                return new Result.Ok<>((ObjectSet<T>) entries);
            }

            final Result<String> stringResult = coder.getString(value);
            if (!(stringResult instanceof Result.Ok(String string))) {
                return stringResult.cast();
            }

            // Could be a tag or a block name depending if it starts with a #
            return new Result.Ok<>(string.startsWith("#")
                    ? new Tag<>(tagType(), string.substring(1))
                    : new Entries<>(List.of(Key.key(string))));
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable ObjectSet<T> value) {
            if (value == null) return new Result.Error<>("null");
            return new Result.Ok<>(switch (value) {
                case Empty<T> empty -> coder.emptyList();
                case Entries<T> entries -> {
                    if (entries.entries.size() == 1)
                        yield coder.createString(entries.entries.stream().findFirst().get().asString());
                    final Transcoder.ListBuilder<D> list = coder.createList(entries.entries.size());
                    for (Key entry : entries.entries)
                        list.add(coder.createString(entry.asString()));
                    yield list.build();
                }
                case Tag<T> tag -> coder.createString("#" + tag.name());
            });
        }
    }

}
