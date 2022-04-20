package net.minestom.server.tag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Utils;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.function.Function;

/**
 * Basic serializers for {@link Tag tags}.
 */
final class Serializers {
    static final Entry<TagHandlerImpl, NBTCompound> PATH = new Entry<>(NBTCompound.class, TagHandlerImpl::fromCompound, TagHandlerImpl::asCompound);

    static final Entry<Byte, NBTByte> BYTE = new Entry<>(NBTByte.class, NBTByte::getValue, NBT::Byte);
    static final Entry<Boolean, NBTByte> BOOLEAN = new Entry<>(NBTByte.class, NBTByte::asBoolean, NBT::Boolean);
    static final Entry<Short, NBTShort> SHORT = new Entry<>(NBTShort.class, NBTShort::getValue, NBT::Short);
    static final Entry<Integer, NBTInt> INT = new Entry<>(NBTInt.class, NBTInt::getValue, NBT::Int);
    static final Entry<Long, NBTLong> LONG = new Entry<>(NBTLong.class, NBTLong::getValue, NBT::Long);
    static final Entry<Float, NBTFloat> FLOAT = new Entry<>(NBTFloat.class, NBTFloat::getValue, NBT::Float);
    static final Entry<Double, NBTDouble> DOUBLE = new Entry<>(NBTDouble.class, NBTDouble::getValue, NBT::Double);
    static final Entry<String, NBTString> STRING = new Entry<>(NBTString.class, NBTString::getValue, NBT::String);
    static final Entry<NBT, NBT> NBT_ENTRY = new Entry<>(NBT.class, Function.identity(), Function.identity());

    static final Entry<java.util.UUID, NBTIntArray> UUID = new Entry<>(NBTIntArray.class, intArray -> Utils.intArrayToUuid(intArray.getValue().copyArray()),
            uuid -> NBT.IntArray(Utils.uuidToIntArray(uuid)));
    static final Entry<ItemStack, NBTCompound> ITEM = new Entry<>(NBTCompound.class, ItemStack::fromItemNBT, ItemStack::toItemNBT);
    static final Entry<Component, NBTString> COMPONENT = new Entry<>(NBTString.class, input -> GsonComponentSerializer.gson().deserialize(input.getValue()),
            component -> NBT.String(GsonComponentSerializer.gson().serialize(component)));

    static <T> Entry<T, NBTCompound> fromTagSerializer(TagSerializer<T> serializer) {
        return new Serializers.Entry<>(NBTCompound.class,
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

    record Entry<T, N extends NBT>(Class<N> nbtType, Function<N, T> reader, Function<T, N> writer) {
        T read(N nbt) {
            return reader.apply(nbt);
        }

        N write(T value) {
            return writer.apply(value);
        }
    }
}
