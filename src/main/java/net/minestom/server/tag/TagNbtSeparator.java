package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class TagNbtSeparator {

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
        }
        // TODO list/array support
    }

    private static <T> Entry<?> makeEntry(List<String> path, Tag<T> tag, T value) {
        return new Entry<>(tag.path(path.toArray(String[]::new)), value);
    }

    record Entry<T>(Tag<T> tag, T value) {
    }
}
