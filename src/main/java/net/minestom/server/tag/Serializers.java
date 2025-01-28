package net.minestom.server.tag;

import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.ServerFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.UUIDUtils;

import java.util.function.Function;

/**
 * Basic serializers for {@link Tag tags}.
 */
final class Serializers {
    static final Entry<Byte, ByteBinaryTag> BYTE = new Entry<>(BinaryTagTypes.BYTE, ByteBinaryTag::value, ByteBinaryTag::byteBinaryTag);
    static final Entry<Boolean, ByteBinaryTag> BOOLEAN = new Entry<>(BinaryTagTypes.BYTE, b -> b.value() != 0, b -> b ? ByteBinaryTag.ONE : ByteBinaryTag.ZERO);
    static final Entry<Short, ShortBinaryTag> SHORT = new Entry<>(BinaryTagTypes.SHORT, ShortBinaryTag::value, ShortBinaryTag::shortBinaryTag);
    static final Entry<Integer, IntBinaryTag> INT = new Entry<>(BinaryTagTypes.INT, IntBinaryTag::value, IntBinaryTag::intBinaryTag);
    static final Entry<Long, LongBinaryTag> LONG = new Entry<>(BinaryTagTypes.LONG, LongBinaryTag::value, LongBinaryTag::longBinaryTag);
    static final Entry<Float, FloatBinaryTag> FLOAT = new Entry<>(BinaryTagTypes.FLOAT, FloatBinaryTag::value, FloatBinaryTag::floatBinaryTag);
    static final Entry<Double, DoubleBinaryTag> DOUBLE = new Entry<>(BinaryTagTypes.DOUBLE, DoubleBinaryTag::value, DoubleBinaryTag::doubleBinaryTag);
    static final Entry<String, StringBinaryTag> STRING = new Entry<>(BinaryTagTypes.STRING, StringBinaryTag::value, StringBinaryTag::stringBinaryTag);
    static final Entry<BinaryTag, BinaryTag> NBT_ENTRY = new Entry<>(null, Function.identity(), Function.identity());

    static final Entry<java.util.UUID, IntArrayBinaryTag> UUID = new Entry<>(BinaryTagTypes.INT_ARRAY, UUIDUtils::fromNbt, UUIDUtils::toNbt);
    static final Entry<ItemStack, CompoundBinaryTag> ITEM = new Entry<>(BinaryTagTypes.COMPOUND, ItemStack::fromItemNBT, ItemStack::toItemNBT);
    static final Entry<Component, StringBinaryTag> COMPONENT = new Entry<>(BinaryTagTypes.STRING, input -> GsonComponentSerializer.gson().deserialize(input.value()),
            component -> StringBinaryTag.stringBinaryTag(GsonComponentSerializer.gson().serialize(component)));

    static final Entry<Object, ByteBinaryTag> EMPTY = new Entry<>(BinaryTagTypes.BYTE, unused -> null, component -> null);

    static <T> Entry<T, CompoundBinaryTag> fromTagSerializer(TagSerializer<T> serializer) {
        return new Serializers.Entry<>(BinaryTagTypes.COMPOUND,
                (CompoundBinaryTag compound) -> {
                    if ((!ServerFlag.SERIALIZE_EMPTY_COMPOUND) && compound.size() == 0) return null;
                    return serializer.read(TagHandler.fromCompound(compound));
                },
                (value) -> {
                    if (value == null) return CompoundBinaryTag.empty();
                    TagHandler handler = TagHandler.newHandler();
                    serializer.write(handler, value);
                    return handler.asCompound();
                });
    }

    record Entry<T, N extends BinaryTag>(BinaryTagType<N> nbtType, Function<N, T> reader, Function<T, N> writer, boolean isPath) {
        Entry(BinaryTagType<N> nbtType, Function<N, T> reader, Function<T, N> writer) {
            this(nbtType, reader, writer, false);
        }

        T read(N nbt) {
            return reader.apply(nbt);
        }

        N write(T value) {
            return writer.apply(value);
        }
    }
}
