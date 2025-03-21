package net.minestom.server.utils.nbt;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>API Note: This class and associated types are currently considered an internal api. It is likely there will be
 * significant changes in the future, and there will not be backwards compatibility for this. Use at your own risk.</p>
 */
@ApiStatus.Internal
public interface BinaryTagSerializer<T> {

    static <T extends BinaryTag> @NotNull BinaryTagSerializer<T> coerced(@NotNull BinaryTagType<T> type) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
                return value;
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (tag.type() == type) {
                    //noinspection unchecked
                    return (T) tag;
                }

                if (tag instanceof StringBinaryTag string) {
                    try {
                        tag = TagStringIOExt.readTag(string.value());
                        if (tag.type() == type) {
                            //noinspection unchecked
                            return (T) tag;
                        }
                    } catch (IOException e) {
                        // Ignored, we'll throw a more useful exception below
                    }
                }

                throw new IllegalArgumentException("Expected " + type + " but got " + tag);
            }
        };
    }

    BinaryTagSerializer<CompoundBinaryTag> COMPOUND = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull CompoundBinaryTag value) {
            return value;
        }

        @Override
        public @NotNull CompoundBinaryTag read(@NotNull BinaryTag tag) {
            return tag instanceof CompoundBinaryTag compoundBinaryTag ? compoundBinaryTag : CompoundBinaryTag.empty();
        }
    };
    BinaryTagSerializer<CompoundBinaryTag> COMPOUND_COERCED = coerced(BinaryTagTypes.COMPOUND);

    BinaryTagSerializer<Component> NBT_COMPONENT = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Component value) {
            return NbtComponentSerializer.nbt().serialize(value);
        }

        @Override
        public @NotNull Component read(@NotNull BinaryTag tag) {
            return NbtComponentSerializer.nbt().deserialize(tag);
        }
    };

    static <T> @NotNull BinaryTagSerializer<T> registryTaggedUnion(
            @NotNull Function<Registries, DynamicRegistry<BinaryTagSerializer<? extends T>>> registrySelector,
            @NotNull Function<T, BinaryTagSerializer<? extends T>> serializerGetter,
            @NotNull String key
    ) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
                final Registries registries = Objects.requireNonNull(context.registries(), "No registries in context");
                final DynamicRegistry<BinaryTagSerializer<? extends T>> registry = registrySelector.apply(registries);

                //noinspection unchecked
                final BinaryTagSerializer<T> serializer = (BinaryTagSerializer<T>) serializerGetter.apply(value);
                final DynamicRegistry.Key<BinaryTagSerializer<? extends T>> type = registry.getKey(serializer);
                Check.notNull(type, "Unregistered serializer for: {0}", value);
                if (context.forClient() && registry.getPack(type) != DataPack.MINECRAFT_CORE)
                    return null;

                final BinaryTag result = serializer.write(context, value);
                if (result == null) return null;
                if (!(result instanceof CompoundBinaryTag resultCompound))
                    throw new IllegalArgumentException("Expected compound tag for tagged union");

                return CompoundBinaryTag.builder().put(resultCompound).putString(key, type.name()).build();
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
                final Registries registries = Objects.requireNonNull(context.registries(), "No registries in context");
                final DynamicRegistry<BinaryTagSerializer<? extends T>> registry = registrySelector.apply(registries);

                if (!(tag instanceof CompoundBinaryTag compound))
                    throw new IllegalArgumentException("Expected compound tag for tagged union");

                final String type = compound.getString(key);
                Check.argCondition(type.isEmpty(), "Missing {0} field: {1}", key, tag);
                //noinspection unchecked
                final BinaryTagSerializer<T> serializer = (BinaryTagSerializer<T>) registry.get(Key.key(type));
                Check.notNull(serializer, "Unregistered serializer for: {0}", type);

                return serializer.read(context, tag);
            }
        };
    }

    interface Context {
        Context EMPTY = new Context() {
            @Override
            public @Nullable Registries registries() {
                return null;
            }

            @Override
            public boolean forClient() {
                return false;
            }
        };

        @Nullable Registries registries();

        boolean forClient();
    }

    record ContextWithRegistries(@NotNull Registries registries, boolean forClient) implements Context {

        public ContextWithRegistries(@NotNull Registries registries) {
            this(registries, false);
        }
    }

    default @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
        return write(value);
    }
    default @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
        return read(tag);
    }

    default @NotNull BinaryTag write(@NotNull T value) {
        return write(Context.EMPTY, value);
    }

    default @NotNull T read(@NotNull BinaryTag tag) {
        return read(Context.EMPTY, tag);
    }

    default <S> BinaryTagSerializer<S> map(@NotNull Function<T, S> to, @NotNull Function<S, T> from) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull S value) {
                return BinaryTagSerializer.this.write(context, from.apply(value));
            }

            @Override
            public @NotNull S read(@NotNull Context context, @NotNull BinaryTag tag) {
                return to.apply(BinaryTagSerializer.this.read(context, tag));
            }
        };
    }
}
