package net.minestom.server.utils.nbt;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.UUIDUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.kyori.adventure.nbt.DoubleBinaryTag.doubleBinaryTag;
import static net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag;

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


    BinaryTagSerializer<Point> VECTOR3D = new BinaryTagSerializer<Point>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Point value) {
            return ListBinaryTag.listBinaryTag(BinaryTagTypes.DOUBLE, List.of(
                    doubleBinaryTag(value.x()), doubleBinaryTag(value.y()), doubleBinaryTag(value.z())));
        }

        @Override
        public @NotNull Point read(@NotNull BinaryTag tag) {
            if (!(tag instanceof ListBinaryTag listTag && listTag.elementType() == BinaryTagTypes.DOUBLE))
                return Vec.ZERO;
            return new Vec(listTag.getDouble(0), listTag.getDouble(1), listTag.getDouble(2));
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

    default BinaryTagSerializer<@Nullable T> optional() {
        return optional(null);
    }

    default BinaryTagSerializer<@UnknownNullability T> optional(@Nullable T defaultValue) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @UnknownNullability T value) {
                return value == null || value.equals(defaultValue) ? null : BinaryTagSerializer.this.write(context, value);
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
                return tag == null ? defaultValue : BinaryTagSerializer.this.read(context, tag);
            }
        };

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

    default BinaryTagSerializer<List<T>> list() {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull List<T> value) {
                ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
                for (T t : value) {
                    BinaryTag entry = BinaryTagSerializer.this.write(context, t);
                    if (entry != null) builder.add(entry);
                }
                return builder.build();
            }

            @Override
            public @NotNull List<T> read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof ListBinaryTag listBinaryTag)) return List.of();
                List<T> list = new ArrayList<>();
                for (BinaryTag element : listBinaryTag)
                    list.add(BinaryTagSerializer.this.read(context, element));
                return List.copyOf(list);
            }
        };
    }

    default BinaryTagSerializer<Set<T>> set() {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull Set<T> value) {
                ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
                for (T t : value) {
                    BinaryTag entry = BinaryTagSerializer.this.write(context, t);
                    if (entry != null) builder.add(entry);
                }
                return builder.build();
            }

            @Override
            public @NotNull Set<T> read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof ListBinaryTag listBinaryTag)) return Set.of();
                Set<T> list = new HashSet<>();
                for (BinaryTag element : listBinaryTag)
                    list.add(BinaryTagSerializer.this.read(context, element));
                return Set.copyOf(list);
            }
        };
    }

    default <V> BinaryTagSerializer<Map<T, V>> mapValue(@NotNull BinaryTagSerializer<V> valueType) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull Map<T, V> value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (Map.Entry<T, V> entry : value.entrySet()) {
                    var rawKey = BinaryTagSerializer.this.write(context, entry.getKey());
                    if (!(rawKey instanceof StringBinaryTag keyTag)) {
                        throw new IllegalArgumentException("Map key must be a string, got " + rawKey);
                    }
                    BinaryTag val = valueType.write(context, entry.getValue());
                    if (val != null) builder.put(keyTag.value(), val);
                }
                return builder.build();
            }

            @Override
            public @NotNull Map<T, V> read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound)) return Map.of();
                Map<T, V> map = new HashMap<>();
                for (Map.Entry<String, ? extends BinaryTag> entry : compound) {
                    T key = BinaryTagSerializer.this.read(context, stringBinaryTag(entry.getKey()));
                    V value = valueType.read(context, entry.getValue());
                    map.put(key, value);
                }
                return Map.copyOf(map);
            }
        };
    }

    default <R> BinaryTagSerializer<R> unionType(@NotNull Function<T, BinaryTagSerializer<R>> serializers, @NotNull Function<R, T> keyFunc) {
        return unionType("type", serializers, keyFunc);
    }

    default <R> BinaryTagSerializer<R> unionType(@NotNull String keyField, @NotNull Function<T, BinaryTagSerializer<R>> serializers, @NotNull Function<R, T> keyFunc) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                final T key = keyFunc.apply(value);
                Check.notNull(key, "unknown key: {0}", key);
                var serializer = serializers.apply(key);

                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                builder.put((CompoundBinaryTag) serializer.write(context, value));
                builder.put(keyField, BinaryTagSerializer.this.write(context, key));
                return builder.build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound)) return null;

                final T key = BinaryTagSerializer.this.read(context, compound.get(keyField));
                Check.notNull(key, "unknown key: {0}", key);

                return serializers.apply(key).read(context, compound);
            }
        };
    }
}
