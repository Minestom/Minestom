package net.minestom.server.tag;

import net.minestom.server.item.ItemStack;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.function.Function;

/**
 * Basic serializers for {@link Tag tags}.
 */
final class Serializers {
    static final Entry<TagHandlerImpl, NBTCompound> PATH = new Entry<>(TagHandlerImpl::fromCompound, TagHandlerImpl::asCompound);

    static final Entry<Byte, NBTByte> BYTE = new Entry<>(NBTByte::getValue, NBT::Byte);
    static final Entry<Boolean, NBTByte> BOOLEAN = new Entry<>(NBTByte::asBoolean, NBT::Boolean);
    static final Entry<Short, NBTShort> SHORT = new Entry<>(NBTShort::getValue, NBT::Short);
    static final Entry<Integer, NBTInt> INT = new Entry<>(NBTInt::getValue, NBT::Int);
    static final Entry<Long, NBTLong> LONG = new Entry<>(NBTLong::getValue, NBT::Long);
    static final Entry<Float, NBTFloat> FLOAT = new Entry<>(NBTFloat::getValue, NBT::Float);
    static final Entry<Double, NBTDouble> DOUBLE = new Entry<>(NBTDouble::getValue, NBT::Double);
    static final Entry<String, NBTString> STRING = new Entry<>(NBTString::getValue, NBT::String);
    static final Entry<NBT, NBT> NBT_ENTRY = new Entry<>(Function.identity(), Function.identity());

    static final Entry<ItemStack, NBTCompound> ITEM = new Entry<>(ItemStack::fromItemNBT, ItemStack::toItemNBT);

    static <T> Entry<T, NBTCompound> fromTagSerializer(TagSerializer<T> serializer) {
        return new Serializers.Entry<>(
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

    record Entry<T, N extends NBT>(Function<N, T> read, Function<T, N> write) {
    }
}
