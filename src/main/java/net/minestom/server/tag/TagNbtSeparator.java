package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Map.entry;

/**
 * Handles conversion of {@link NBT} subtypes into one or multiple primitive {@link Tag tags}.
 */
final class TagNbtSeparator {
    static final Map<NBTType<?>, Function<String, Tag<?>>> SUPPORTED_TYPES = Map.ofEntries(
            entry(NBTType.TAG_Byte, Tag::Byte),
            entry(NBTType.TAG_Short, Tag::Short),
            entry(NBTType.TAG_Int, Tag::Integer),
            entry(NBTType.TAG_Long, Tag::Long),
            entry(NBTType.TAG_Float, Tag::Float),
            entry(NBTType.TAG_Double, Tag::Double),
            entry(NBTType.TAG_String, Tag::String));

    static void separate(String key, NBT nbt, Consumer<Entry> consumer) {
        convert(new ArrayList<>(), key, nbt, consumer);
    }

    private static void convert(List<String> path, String key, NBT nbt, Consumer<Entry> consumer) {
        if (nbt instanceof NBTByte nbtByte) {
            consumer.accept(makeEntry(path, Tag.Byte(key), nbtByte.getValue()));
        } else if (nbt instanceof NBTShort nbtShort) {
            consumer.accept(makeEntry(path, Tag.Short(key), nbtShort.getValue()));
        } else if (nbt instanceof NBTInt nbtInt) {
            consumer.accept(makeEntry(path, Tag.Integer(key), nbtInt.getValue()));
        } else if (nbt instanceof NBTLong nbtLong) {
            consumer.accept(makeEntry(path, Tag.Long(key), nbtLong.getValue()));
        } else if (nbt instanceof NBTFloat nbtFloat) {
            consumer.accept(makeEntry(path, Tag.Float(key), nbtFloat.getValue()));
        } else if (nbt instanceof NBTDouble nbtDouble) {
            consumer.accept(makeEntry(path, Tag.Double(key), nbtDouble.getValue()));
        } else if (nbt instanceof NBTString nbtString) {
            consumer.accept(makeEntry(path, Tag.String(key), nbtString.getValue()));
        } else if (nbt instanceof NBTCompound nbtCompound) {
            for (var ent : nbtCompound) {
                var newPath = new ArrayList<>(path);
                newPath.add(key);
                convert(newPath, ent.getKey(), ent.getValue(), consumer);
            }
        } else if (nbt instanceof NBTList<?> nbtList) {
            var tagFunction = SUPPORTED_TYPES.get(nbtList.getSubtagType());
            if (tagFunction == null) {
                // Invalid list subtype, fallback to nbt
                consumer.accept(makeEntry(path, Tag.NBT(key), nbt));
            } else {
                try {
                    var tag = tagFunction.apply(key).list();
                    Object[] values = new Object[nbtList.getSize()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = nbtValue(nbtList.get(i));
                    }
                    consumer.accept(makeEntry(path, Tag.class.cast(tag), List.of(values)));
                } catch (Exception e) {
                    e.printStackTrace();
                    consumer.accept(makeEntry(path, Tag.NBT(key), nbt));
                }
            }
        } else {
            // TODO array support
            consumer.accept(makeEntry(path, Tag.NBT(key), nbt));
        }
    }

    private static <T> Entry<?> makeEntry(List<String> path, Tag<T> tag, T value) {
        return new Entry<>(tag.path(path.toArray(String[]::new)), value);
    }

    record Entry<T>(Tag<T> tag, T value) {
    }

    private static Object nbtValue(NBT nbt) throws IllegalArgumentException {
        if (nbt instanceof NBTByte nbtByte) {
            return nbtByte.getValue();
        } else if (nbt instanceof NBTShort nbtShort) {
            return nbtShort.getValue();
        } else if (nbt instanceof NBTInt nbtInt) {
            return nbtInt.getValue();
        } else if (nbt instanceof NBTLong nbtLong) {
            return nbtLong.getValue();
        } else if (nbt instanceof NBTFloat nbtFloat) {
            return nbtFloat.getValue();
        } else if (nbt instanceof NBTDouble nbtDouble) {
            return nbtDouble.getValue();
        } else if (nbt instanceof NBTString nbtString) {
            return nbtString.getValue();
        } else {
            throw new IllegalArgumentException("Unsupported NBT type: " + nbt.getClass().getName());
        }
    }
}
