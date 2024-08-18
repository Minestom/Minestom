package net.minestom.server.tag;

import net.kyori.adventure.nbt.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TagNbtSeparatorTest {

    @Test
    void primitives() {
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Byte("key"), (byte) 1),
                "key", ByteBinaryTag.byteBinaryTag((byte) 1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Short("key"), (short) 1),
                "key", ShortBinaryTag.shortBinaryTag((short) 1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Integer("key"), 1),
                "key", IntBinaryTag.intBinaryTag(1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Long("key"), 1L),
                "key", LongBinaryTag.longBinaryTag(1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Float("key"), 1f),
                "key", FloatBinaryTag.floatBinaryTag(1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Double("key"), 1d),
                "key", DoubleBinaryTag.doubleBinaryTag(1));
    }

    @Test
    void compound() {
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Byte("key").path("path"), (byte) 1),
                "path", CompoundBinaryTag.builder().putByte("key", (byte) 1).build());
    }

    @Test
    void compoundMultiple() {
        assertSeparation(Set.of(new TagNbtSeparator.Entry<>(Tag.Byte("key").path("path"), (byte) 1),
                        new TagNbtSeparator.Entry<>(Tag.Integer("key2").path("path"), 2)),
                "path", CompoundBinaryTag.builder().putByte("key", (byte) 1).putInt("key2", 2).build());
    }

    @Test
    void list() {
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Integer("key").list(), List.of(1)),
                "key", ListBinaryTag.listBinaryTag(BinaryTagTypes.INT, List.of(IntBinaryTag.intBinaryTag(1))));
    }

    void assertSeparation(Set<TagNbtSeparator.Entry<?>> expected, String key, BinaryTag nbt) {
        assertEquals(expected, retrieve(key, nbt));
    }

    void assertSeparation(TagNbtSeparator.Entry<?> expected, String key, BinaryTag nbt) {
        var entries = retrieve(key, nbt);
        assertEquals(1, entries.size());
        assertEquals(expected, entries.iterator().next());
    }

    Set<TagNbtSeparator.Entry<?>> retrieve(String key, BinaryTag nbt) {
        Set<TagNbtSeparator.Entry<?>> entries = new HashSet<>();
        TagNbtSeparator.separate(key, nbt, entries::add);
        return Set.copyOf(entries);
    }
}
