package net.minestom.server.tag;

import net.kyori.adventure.nbt.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.nbt.BinaryTagUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Map.entry;

/**
 * Handles conversion of {@link BinaryTag} subtypes into one or multiple primitive {@link Tag tags}.
 */
final class TagNbtSeparator {
    static final Map<BinaryTagType<?>, Function<String, Tag<?>>> SUPPORTED_TYPES = Map.ofEntries(
            entry(BinaryTagTypes.BYTE, Tag::Byte),
            entry(BinaryTagTypes.SHORT, Tag::Short),
            entry(BinaryTagTypes.INT, Tag::Integer),
            entry(BinaryTagTypes.LONG, Tag::Long),
            entry(BinaryTagTypes.FLOAT, Tag::Float),
            entry(BinaryTagTypes.DOUBLE, Tag::Double),
            entry(BinaryTagTypes.STRING, Tag::String));

    static void separate(CompoundBinaryTag nbtCompound, Consumer<Entry> consumer) {
        for (var ent : nbtCompound) {
            convert(new ArrayList<>(), ent.getKey(), ent.getValue(), consumer);
        }
    }

    static void separate(String key, BinaryTag nbt, Consumer<Entry> consumer) {
        convert(new ArrayList<>(), key, nbt, consumer);
    }

    static Entry separateSingle(String key, BinaryTag nbt) {
        assert !(nbt instanceof CompoundBinaryTag);
        AtomicReference<Entry<?>> entryRef = new AtomicReference<>();
        convert(new ArrayList<>(), key, nbt, entry -> {
            assert entryRef.getPlain() == null : "Multiple entries found for nbt tag: " + key + " -> " + nbt;
            entryRef.setPlain(entry);
        });
        var entry = entryRef.getPlain();
        assert entry != null;
        return entry;
    }

    private static void convert(List<String> path, String key, BinaryTag nbt, Consumer<Entry> consumer) {
        var tagFunction = SUPPORTED_TYPES.get(nbt.type());
        if (tagFunction != null) {
            Tag tag = tagFunction.apply(key);
            consumer.accept(makeEntry(path, tag, BinaryTagUtil.nbtValueFromTag(nbt)));
        } else if (nbt instanceof CompoundBinaryTag nbtCompound) {
            for (var ent : nbtCompound) {
                var newPath = new ArrayList<>(path);
                newPath.add(key);
                convert(newPath, ent.getKey(), ent.getValue(), consumer);
            }
        } else if (nbt instanceof ListBinaryTag nbtList) {
            tagFunction = SUPPORTED_TYPES.get(nbtList.elementType());
            if (tagFunction == null) {
                // Invalid list subtype, fallback to nbt
                consumer.accept(makeEntry(path, Tag.NBT(key), nbt));
            } else {
                try {
                    var tag = tagFunction.apply(key).list();
                    Object[] values = new Object[nbtList.size()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = BinaryTagUtil.nbtValueFromTag(nbtList.get(i));
                    }
                    consumer.accept(makeEntry(path, Tag.class.cast(tag), List.of(values)));
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
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
