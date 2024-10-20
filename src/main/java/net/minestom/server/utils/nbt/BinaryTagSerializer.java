package net.minestom.server.utils.nbt;

import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.UniqueIdUtils;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.kyori.adventure.nbt.DoubleBinaryTag.doubleBinaryTag;
import static net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag;

/**
 * <p>API Note: This class and associated types are currently considered an internal api. It is likely there will be
 * significant changes in the future, and there will not be backwards compatibility for this. Use at your own risk.</p>
 */
@ApiStatus.Internal
public interface BinaryTagSerializer<T> {

    static <T> @NotNull BinaryTagSerializer<T> recursive(@NotNull Function<BinaryTagSerializer<T>, BinaryTagSerializer<T>> self) {
        return new BinaryTagSerializer<>() {
            private BinaryTagSerializer<T> serializer = null;

            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
                return serializer().write(context, value);
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
                return serializer().read(context, tag);
            }

            private BinaryTagSerializer<T> serializer() {
                if (serializer == null) serializer = self.apply(this);
                return serializer;
            }
        };
    }

    static <T> @NotNull BinaryTagSerializer<T> lazy(@NotNull Supplier<BinaryTagSerializer<T>> self) {
        return new BinaryTagSerializer<>() {
            private BinaryTagSerializer<T> serializer = null;

            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull T value) {
                return serializer().write(context, value);
            }

            @Override
            public @NotNull T read(@NotNull Context context, @NotNull BinaryTag tag) {
                return serializer().read(context, tag);
            }

            private BinaryTagSerializer<T> serializer() {
                if (serializer == null) serializer = self.get();
                return serializer;
            }
        };
    }

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

    static <E extends Enum<E>> @NotNull BinaryTagSerializer<E> fromEnumStringable(@NotNull Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        final Map<String, E> nameMap = Arrays.stream(values).collect(Collectors.toMap(e -> e.name().toLowerCase(Locale.ROOT), Function.identity()));
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull E value) {
                return stringBinaryTag(value.name().toLowerCase(Locale.ROOT));
            }

            @Override
            public @NotNull E read(@NotNull BinaryTag tag) {
                return switch (tag) {
                    case IntBinaryTag intBinaryTag -> values[intBinaryTag.value()];
                    case StringBinaryTag string ->
                            nameMap.getOrDefault(string.value().toLowerCase(Locale.ROOT), values[0]);
                    default -> values[0];
                };
            }
        };
    }

    BinaryTagSerializer<Unit> UNIT = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Unit value) {
            return CompoundBinaryTag.empty();
        }

        @Override
        public @NotNull Unit read(@NotNull BinaryTag tag) {
            return Unit.INSTANCE;
        }
    };

    BinaryTagSerializer<Byte> BYTE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Byte value) {
            return ByteBinaryTag.byteBinaryTag(value);
        }

        @Override
        public @NotNull Byte read(@NotNull BinaryTag tag) {
            return tag instanceof ByteBinaryTag byteBinaryTag ? byteBinaryTag.value() : 0;
        }
    };

    BinaryTagSerializer<Boolean> BOOLEAN = BYTE.map(b -> b != 0, b -> (byte) (b ? 1 : 0));

    BinaryTagSerializer<Integer> INT = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Integer value) {
            return IntBinaryTag.intBinaryTag(value);
        }

        @Override
        public @NotNull Integer read(@NotNull BinaryTag tag) {
            return tag instanceof NumberBinaryTag numberTag ? numberTag.intValue() : 0;
        }
    };

    BinaryTagSerializer<Float> FLOAT = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Float value) {
            return FloatBinaryTag.floatBinaryTag(value);
        }

        @Override
        public @NotNull Float read(@NotNull BinaryTag tag) {
            return tag instanceof NumberBinaryTag numberTag ? numberTag.floatValue() : 0f;
        }
    };

    BinaryTagSerializer<String> STRING = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull String value) {
            return stringBinaryTag(value);
        }

        @Override
        public @NotNull String read(@NotNull BinaryTag tag) {
            return tag instanceof StringBinaryTag stringBinaryTag ? stringBinaryTag.value() : "";
        }
    };

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

    BinaryTagSerializer<Component> JSON_COMPONENT = STRING.map(
            s -> GsonComponentSerializer.gson().deserialize(s),
            c -> GsonComponentSerializer.gson().serialize(c)
    );
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
    BinaryTagSerializer<Style> NBT_COMPONENT_STYLE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Style value) {
            return NbtComponentSerializer.nbt().serializeStyle(value);
        }

        @Override
        public @NotNull Style read(@NotNull BinaryTag tag) {
            return NbtComponentSerializer.nbt().deserializeStyle(tag);
        }
    };
    BinaryTagSerializer<ItemStack> ITEM = COMPOUND.map(ItemStack::fromItemNBT, ItemStack::toItemNBT);

    BinaryTagSerializer<UUID> UUID = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(java.util.@NotNull UUID value) {
            return UniqueIdUtils.toNbt(value);
        }

        @Override
        public java.util.@NotNull UUID read(@NotNull BinaryTag tag) {
            if (!(tag instanceof IntArrayBinaryTag intArrayTag)) {
                throw new IllegalArgumentException("unexpected uuid type: " + tag.type());
            }
            return UniqueIdUtils.fromNbt(intArrayTag);
        }
    };

    BinaryTagSerializer<Point> BLOCK_POSITION = new BinaryTagSerializer<Point>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Point value) {
            return IntArrayBinaryTag.intArrayBinaryTag(value.blockX(), value.blockY(), value.blockZ());
        }

        @Override
        public @NotNull Point read(@NotNull BinaryTag tag) {
            if (!(tag instanceof IntArrayBinaryTag intArrayTag))
                return Vec.ZERO;
            int[] value = intArrayTag.value();
            return new Vec(value[0], value[1], value[2]);
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

    static <T extends ProtocolObject> @NotNull BinaryTagSerializer<DynamicRegistry.Key<T>> registryKey(@NotNull Function<Registries, DynamicRegistry<T>> registrySelector) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, DynamicRegistry.@NotNull Key<T> value) {
                return stringBinaryTag(value.name());
            }

            @Override
            public @NotNull DynamicRegistry.Key<T> read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof StringBinaryTag s))
                    throw new IllegalArgumentException("Expected string tag for registry key");
                final Registries registries = Objects.requireNonNull(context.registries(), "No registries in context");
                final DynamicRegistry<T> registry = registrySelector.apply(registries);
                final DynamicRegistry.Key<T> key = DynamicRegistry.Key.of(s.value());
                Check.argCondition(registry.get(key) == null, "Key is not registered: {0} > {1}", registry, s);
                return key;
            }
        };
    }

    static <P1, R> @NotNull BinaryTagSerializer<R> object(
            @NotNull String param1, @NotNull BinaryTagSerializer<P1> serializer1, @NotNull Function<R, P1> getter1,
            @NotNull Function<P1, R> constructor
    ) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                P1 p1 = getter1.apply(value);
                if (p1 != null) {
                    BinaryTag child = serializer1.write(context, p1);
                    if (child == null) return null;
                    builder.put(param1, child);
                }
                return builder.build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound)) return constructor.apply(null);
                return constructor.apply(serializer1.read(context, compound.get(param1)));
            }
        };
    }

    static <P1, P2, R> @NotNull BinaryTagSerializer<R> object(
            @NotNull String param1, @NotNull BinaryTagSerializer<P1> serializer1, @NotNull Function<R, P1> getter1,
            @NotNull String param2, @NotNull BinaryTagSerializer<P2> serializer2, @NotNull Function<R, P2> getter2,
            @NotNull BiFunction<P1, P2, R> constructor
    ) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                P1 p1 = getter1.apply(value);
                if (p1 != null) builder.put(param1, serializer1.write(context, p1));
                P2 p2 = getter2.apply(value);
                if (p2 != null) builder.put(param2, serializer2.write(context, p2));
                return builder.build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound)) return constructor.apply(null, null);
                return constructor.apply(
                        serializer1.read(context, compound.get(param1)),
                        serializer2.read(context, compound.get(param2))
                );
            }
        };
    }

    interface Function3<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

    static <P1, P2, P3, R> @NotNull BinaryTagSerializer<R> object(
            @NotNull String param1, @NotNull BinaryTagSerializer<P1> serializer1, @NotNull Function<R, P1> getter1,
            @NotNull String param2, @NotNull BinaryTagSerializer<P2> serializer2, @NotNull Function<R, P2> getter2,
            @NotNull String param3, @NotNull BinaryTagSerializer<P3> serializer3, @NotNull Function<R, P3> getter3,
            @NotNull Function3<P1, P2, P3, R> constructor
    ) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                P1 p1 = getter1.apply(value);
                if (p1 != null) builder.put(param1, serializer1.write(context, p1));
                P2 p2 = getter2.apply(value);
                if (p2 != null) builder.put(param2, serializer2.write(context, p2));
                P3 p3 = getter3.apply(value);
                if (p3 != null) builder.put(param3, serializer3.write(context, p3));
                return builder.build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound)) return constructor.apply(null, null, null);
                return constructor.apply(
                        serializer1.read(context, compound.get(param1)),
                        serializer2.read(context, compound.get(param2)),
                        serializer3.read(context, compound.get(param3))
                );
            }
        };
    }

    interface Function4<P1, P2, P3, P4, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    static <P1, P2, P3, P4, R> @NotNull BinaryTagSerializer<R> object(
            @NotNull String param1, @NotNull BinaryTagSerializer<P1> serializer1, @NotNull Function<R, P1> getter1,
            @NotNull String param2, @NotNull BinaryTagSerializer<P2> serializer2, @NotNull Function<R, P2> getter2,
            @NotNull String param3, @NotNull BinaryTagSerializer<P3> serializer3, @NotNull Function<R, P3> getter3,
            @NotNull String param4, @NotNull BinaryTagSerializer<P4> serializer4, @NotNull Function<R, P4> getter4,
            @NotNull Function4<P1, P2, P3, P4, R> constructor
    ) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                P1 p1 = getter1.apply(value);
                if (p1 != null) builder.put(param1, serializer1.write(context, p1));
                P2 p2 = getter2.apply(value);
                if (p2 != null) builder.put(param2, serializer2.write(context, p2));
                P3 p3 = getter3.apply(value);
                if (p3 != null) builder.put(param3, serializer3.write(context, p3));
                P4 p4 = getter4.apply(value);
                if (p4 != null) builder.put(param4, serializer4.write(context, p4));
                return builder.build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound))
                    return constructor.apply(null, null, null, null);
                return constructor.apply(
                        serializer1.read(context, compound.get(param1)),
                        serializer2.read(context, compound.get(param2)),
                        serializer3.read(context, compound.get(param3)),
                        serializer4.read(context, compound.get(param4))
                );
            }
        };
    }

    interface Function5<P1, P2, P3, P4, P5, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    static <P1, P2, P3, P4, P5, R> @NotNull BinaryTagSerializer<R> object(
            @NotNull String param1, @NotNull BinaryTagSerializer<P1> serializer1, @NotNull Function<R, P1> getter1,
            @NotNull String param2, @NotNull BinaryTagSerializer<P2> serializer2, @NotNull Function<R, P2> getter2,
            @NotNull String param3, @NotNull BinaryTagSerializer<P3> serializer3, @NotNull Function<R, P3> getter3,
            @NotNull String param4, @NotNull BinaryTagSerializer<P4> serializer4, @NotNull Function<R, P4> getter4,
            @NotNull String param5, @NotNull BinaryTagSerializer<P5> serializer5, @NotNull Function<R, P5> getter5,
            @NotNull Function5<P1, P2, P3, P4, P5, R> constructor
    ) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                P1 p1 = getter1.apply(value);
                if (p1 != null) builder.put(param1, serializer1.write(context, p1));
                P2 p2 = getter2.apply(value);
                if (p2 != null) builder.put(param2, serializer2.write(context, p2));
                P3 p3 = getter3.apply(value);
                if (p3 != null) builder.put(param3, serializer3.write(context, p3));
                P4 p4 = getter4.apply(value);
                if (p4 != null) builder.put(param4, serializer4.write(context, p4));
                P5 p5 = getter5.apply(value);
                if (p5 != null) builder.put(param5, serializer5.write(context, p5));
                return builder.build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound))
                    return constructor.apply(null, null, null, null, null);
                return constructor.apply(
                        serializer1.read(context, compound.get(param1)),
                        serializer2.read(context, compound.get(param2)),
                        serializer3.read(context, compound.get(param3)),
                        serializer4.read(context, compound.get(param4)),
                        serializer5.read(context, compound.get(param5))
                );
            }
        };
    }

    interface Function6<P1, P2, P3, P4, P5, P6, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
    }

    static <P1, P2, P3, P4, P5, P6, R> @NotNull BinaryTagSerializer<R> object(
            @NotNull String param1, @NotNull BinaryTagSerializer<P1> serializer1, @NotNull Function<R, P1> getter1,
            @NotNull String param2, @NotNull BinaryTagSerializer<P2> serializer2, @NotNull Function<R, P2> getter2,
            @NotNull String param3, @NotNull BinaryTagSerializer<P3> serializer3, @NotNull Function<R, P3> getter3,
            @NotNull String param4, @NotNull BinaryTagSerializer<P4> serializer4, @NotNull Function<R, P4> getter4,
            @NotNull String param5, @NotNull BinaryTagSerializer<P5> serializer5, @NotNull Function<R, P5> getter5,
            @NotNull String param6, @NotNull BinaryTagSerializer<P6> serializer6, @NotNull Function<R, P6> getter6,
            @NotNull Function6<P1, P2, P3, P4, P5, P6, R> constructor
    ) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                P1 p1 = getter1.apply(value);
                if (p1 != null) builder.put(param1, serializer1.write(context, p1));
                P2 p2 = getter2.apply(value);
                if (p2 != null) builder.put(param2, serializer2.write(context, p2));
                P3 p3 = getter3.apply(value);
                if (p3 != null) builder.put(param3, serializer3.write(context, p3));
                P4 p4 = getter4.apply(value);
                if (p4 != null) builder.put(param4, serializer4.write(context, p4));
                P5 p5 = getter5.apply(value);
                if (p5 != null) builder.put(param5, serializer5.write(context, p5));
                P6 p6 = getter6.apply(value);
                if (p6 != null) builder.put(param6, serializer6.write(context, p6));
                return builder.build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound))
                    return constructor.apply(null, null, null, null, null, null);
                return constructor.apply(
                        serializer1.read(context, compound.get(param1)),
                        serializer2.read(context, compound.get(param2)),
                        serializer3.read(context, compound.get(param3)),
                        serializer4.read(context, compound.get(param4)),
                        serializer5.read(context, compound.get(param5)),
                        serializer6.read(context, compound.get(param6))
                );
            }
        };
    }

    interface Function7<P1, P2, P3, P4, P5, P6, P7, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);
    }

    static <P1, P2, P3, P4, P5, P6, P7, R> @NotNull BinaryTagSerializer<R> object(
            @NotNull String param1, @NotNull BinaryTagSerializer<P1> serializer1, @NotNull Function<R, P1> getter1,
            @NotNull String param2, @NotNull BinaryTagSerializer<P2> serializer2, @NotNull Function<R, P2> getter2,
            @NotNull String param3, @NotNull BinaryTagSerializer<P3> serializer3, @NotNull Function<R, P3> getter3,
            @NotNull String param4, @NotNull BinaryTagSerializer<P4> serializer4, @NotNull Function<R, P4> getter4,
            @NotNull String param5, @NotNull BinaryTagSerializer<P5> serializer5, @NotNull Function<R, P5> getter5,
            @NotNull String param6, @NotNull BinaryTagSerializer<P6> serializer6, @NotNull Function<R, P6> getter6,
            @NotNull String param7, @NotNull BinaryTagSerializer<P7> serializer7, @NotNull Function<R, P7> getter7,
            @NotNull Function7<P1, P2, P3, P4, P5, P6, P7, R> constructor
    ) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull R value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                P1 p1 = getter1.apply(value);
                if (p1 != null) builder.put(param1, serializer1.write(context, p1));
                P2 p2 = getter2.apply(value);
                if (p2 != null) builder.put(param2, serializer2.write(context, p2));
                P3 p3 = getter3.apply(value);
                if (p3 != null) builder.put(param3, serializer3.write(context, p3));
                P4 p4 = getter4.apply(value);
                if (p4 != null) builder.put(param4, serializer4.write(context, p4));
                P5 p5 = getter5.apply(value);
                if (p5 != null) builder.put(param5, serializer5.write(context, p5));
                P6 p6 = getter6.apply(value);
                if (p6 != null) builder.put(param6, serializer6.write(context, p6));
                P7 p7 = getter7.apply(value);
                if (p7 != null) builder.put(param7, serializer7.write(context, p7));
                return builder.build();
            }

            @Override
            public @NotNull R read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound))
                    return constructor.apply(null, null, null, null, null, null, null);
                return constructor.apply(
                        serializer1.read(context, compound.get(param1)),
                        serializer2.read(context, compound.get(param2)),
                        serializer3.read(context, compound.get(param3)),
                        serializer4.read(context, compound.get(param4)),
                        serializer5.read(context, compound.get(param5)),
                        serializer6.read(context, compound.get(param6)),
                        serializer7.read(context, compound.get(param7))
                );
            }
        };
    }

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
                final BinaryTagSerializer<T> serializer = (BinaryTagSerializer<T>) registry.get(NamespaceID.from(type));
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
}
