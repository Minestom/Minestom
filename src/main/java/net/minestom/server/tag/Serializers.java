package net.minestom.server.tag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.item.ItemStack;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.UUID;
import java.util.function.Function;

/**
 * Basic serializers for {@link Tag tags}.
 */
final class Serializers {
    static final Entry<Byte, NBTByte> BYTE = new Entry<>(NBTType.TAG_Byte, NBTByte::getValue, NBT::Byte);
    static final Entry<Boolean, NBTByte> BOOLEAN = new Entry<>(NBTType.TAG_Byte, NBTByte::asBoolean, NBT::Boolean);
    static final Entry<Short, NBTShort> SHORT = new Entry<>(NBTType.TAG_Short, NBTShort::getValue, NBT::Short);
    static final Entry<Integer, NBTInt> INT = new Entry<>(NBTType.TAG_Int, NBTInt::getValue, NBT::Int);
    static final Entry<Long, NBTLong> LONG = new Entry<>(NBTType.TAG_Long, NBTLong::getValue, NBT::Long);
    static final Entry<Float, NBTFloat> FLOAT = new Entry<>(NBTType.TAG_Float, NBTFloat::getValue, NBT::Float);
    static final Entry<Double, NBTDouble> DOUBLE = new Entry<>(NBTType.TAG_Double, NBTDouble::getValue, NBT::Double);
    static final Entry<String, NBTString> STRING = new Entry<>(NBTType.TAG_String, NBTString::getValue, NBT::String);
    static final Entry<NBT, NBT> NBT_ENTRY = new Entry<>(null, Function.identity(), Function.identity());

    static final Entry<java.util.UUID, NBTIntArray> UUID = new Entry<>(NBTType.TAG_Int_Array, intArray -> intArrayToUuid(intArray.getValue().copyArray()),
            uuid -> NBT.IntArray(uuidToIntArray(uuid)));
    static final Entry<ItemStack, NBTCompound> ITEM = new Entry<>(NBTType.TAG_Compound, ItemStack::fromItemNBT, ItemStack::toItemNBT);
    static final Entry<Component, NBTString> COMPONENT = new Entry<>(NBTType.TAG_String, input -> GsonComponentSerializer.gson().deserialize(input.getValue()),
            component -> NBT.String(GsonComponentSerializer.gson().serialize(component)));

    static <T> Entry<T, NBTCompound> fromTagSerializer(TagSerializer<T> serializer) {
        return new Serializers.Entry<>(NBTType.TAG_Compound,
                (NBTCompound compound) -> {
                    if (compound.isEmpty()) return null;
                    return serializer.read(TagHandler.fromCompound(compound));
                },
                (value) -> {
                    if (value == null) return NBTCompound.EMPTY;
                    TagHandler handler = TagHandler.newHandler();
                    serializer.write(handler, value);
                    return handler.asCompound();
                });
    }

    record Entry<T, N extends NBT>(NBTType<N> nbtType, Function<N, T> reader, Function<T, N> writer, boolean isPath) {
        Entry(NBTType<N> nbtType, Function<N, T> reader, Function<T, N> writer) {
            this(nbtType, reader, writer, false);
        }

        T read(N nbt) {
            return reader.apply(nbt);
        }

        N write(T value) {
            return writer.apply(value);
        }
    }

    private static int[] uuidToIntArray(UUID uuid) {
        int[] array = new int[4];

        final long uuidMost = uuid.getMostSignificantBits();
        final long uuidLeast = uuid.getLeastSignificantBits();

        array[0] = (int) (uuidMost >> 32);
        array[1] = (int) uuidMost;

        array[2] = (int) (uuidLeast >> 32);
        array[3] = (int) uuidLeast;

        return array;
    }

    private static UUID intArrayToUuid(int[] array) {
        final long uuidMost = (long) array[0] << 32 | array[1] & 0xFFFFFFFFL;
        final long uuidLeast = (long) array[2] << 32 | array[3] & 0xFFFFFFFFL;

        return new UUID(uuidMost, uuidLeast);
    }
}
