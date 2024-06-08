package net.minestom.server.utils.nbt;

import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.UniqueIdUtils;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
            public @NotNull BinaryTag write(@NotNull T value) {
                return serializer().write(value);
            }

            @Override
            public @NotNull T read(@NotNull BinaryTag tag) {
                return serializer().read(tag);
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
            public @NotNull BinaryTag write(@NotNull T value) {
                return serializer().write(value);
            }

            @Override
            public @NotNull T read(@NotNull BinaryTag tag) {
                return serializer().read(tag);
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
            public @NotNull BinaryTag write(@NotNull T value) {
                return value;
            }

            @Override
            public @NotNull T read(@NotNull BinaryTag tag) {
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
        return new BinaryTagSerializer<E>() {
            @Override
            public @NotNull BinaryTag write(@NotNull E value) {
                return StringBinaryTag.stringBinaryTag(value.name().toLowerCase(Locale.ROOT));
            }

            @Override
            public @NotNull E read(@NotNull BinaryTag tag) {
                return switch (tag) {
                    case IntBinaryTag intBinaryTag -> values[intBinaryTag.value()];
                    case StringBinaryTag string -> nameMap.getOrDefault(string.value().toLowerCase(Locale.ROOT), values[0]);
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

    BinaryTagSerializer<String> STRING = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull String value) {
            return StringBinaryTag.stringBinaryTag(value);
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

    static <T extends ProtocolObject> @NotNull BinaryTagSerializer<DynamicRegistry.Key<T>> registryKey(@NotNull Function<Registries, DynamicRegistry<T>> registrySelector) {
        //todo need to pass Registries as context here somehow.
        return STRING.map(
                s -> {
                    final DynamicRegistry<T> registry = registrySelector.apply(MinecraftServer.process());
                    final DynamicRegistry.Key<T> key = DynamicRegistry.Key.of(s);
                    Check.argCondition(registry.get(key) == null, "Key is not registered: {0} > {1}", registry, s);
                    return key;
                },
                DynamicRegistry.Key::name
        );
    }

    @NotNull BinaryTag write(@NotNull T value);
    @NotNull T read(@NotNull BinaryTag tag);

    default <S> BinaryTagSerializer<S> map(@NotNull Function<T, S> to, @NotNull Function<S, T> from) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull S value) {
                return BinaryTagSerializer.this.write(from.apply(value));
            }

            @Override
            public @NotNull S read(@NotNull BinaryTag tag) {
                return to.apply(BinaryTagSerializer.this.read(tag));
            }
        };
    }

    default BinaryTagSerializer<List<T>> list() {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull List<T> value) {
                ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
                for (T t : value) builder.add(BinaryTagSerializer.this.write(t));
                return builder.build();
            }

            @Override
            public @NotNull List<T> read(@NotNull BinaryTag tag) {
                if (!(tag instanceof ListBinaryTag listBinaryTag)) return List.of();
                List<T> list = new ArrayList<>();
                for (BinaryTag element : listBinaryTag)
                    list.add(BinaryTagSerializer.this.read(element));
                return List.copyOf(list);
            }
        };
    }
}
