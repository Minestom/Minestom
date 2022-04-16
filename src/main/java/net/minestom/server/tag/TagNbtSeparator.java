package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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

    static void separate(NBTCompound nbtCompound, Consumer<Entry> consumer) {
        for (var ent : nbtCompound) {
            convert(new ArrayList<>(), ent.getKey(), ent.getValue(), consumer);
        }
    }

    static void separate(String key, NBT nbt, Consumer<Entry> consumer) {
        convert(new ArrayList<>(), key, nbt, consumer);
    }

    static Entry separateSingle(String key, NBT nbt) {
        assert !(nbt instanceof NBTCompound);
        AtomicReference<Entry<?>> entryRef = new AtomicReference<>();
        convert(new ArrayList<>(), key, nbt, entry -> {
            assert entryRef.getPlain() == null : "Multiple entries found for nbt tag: " + key + " -> " + nbt;
            entryRef.setPlain(entry);
        });
        var entry = entryRef.getPlain();
        assert entry != null;
        return entry;
    }

    private static void convert(List<String> path, String key, NBT nbt, Consumer<Entry> consumer) {
        var tagFunction = SUPPORTED_TYPES.get(nbt.getID());
        if (tagFunction != null) {
            Tag tag = tagFunction.apply(key);
            consumer.accept(makeEntry(path, tag, nbt.getValue()));
        } else if (nbt instanceof NBTCompound nbtCompound) {
            for (var ent : nbtCompound) {
                var newPath = new ArrayList<>(path);
                newPath.add(key);
                convert(newPath, ent.getKey(), ent.getValue(), consumer);
            }
        } else if (nbt instanceof NBTList<?> nbtList) {
            tagFunction = SUPPORTED_TYPES.get(nbtList.getSubtagType());
            if (tagFunction == null) {
                // Invalid list subtype, fallback to nbt
                consumer.accept(makeEntry(path, Tag.NBT(key), nbt));
            } else {
                try {
                    var tag = tagFunction.apply(key).list();
                    Object[] values = new Object[nbtList.getSize()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = nbtList.get(i).getValue();
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
}
