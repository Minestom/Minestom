package net.minestom.server.tag;

import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TagNbtSeparatorTest {

    @Test
    public void primitives() {
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Byte("key"), (byte) 1),
                "key", NBT.Byte(1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Short("key"), (short) 1),
                "key", NBT.Short(1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Integer("key"), 1),
                "key", NBT.Int(1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Long("key"), 1L),
                "key", NBT.Long(1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Float("key"), 1f),
                "key", NBT.Float(1));
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Double("key"), 1d),
                "key", NBT.Double(1));
    }

    @Test
    public void compound() {
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Byte("key").path("path"), (byte) 1),
                "path", NBT.Compound(Map.of("key", NBT.Byte(1))));
    }

    @Test
    public void compoundMultiple() {
        assertSeparation(Set.of(new TagNbtSeparator.Entry<>(Tag.Byte("key").path("path"), (byte) 1),
                        new TagNbtSeparator.Entry<>(Tag.Integer("key2").path("path"), 2)),
                "path", NBT.Compound(Map.of("key", NBT.Byte(1), "key2", NBT.Int(2))));
    }

    @Test
    public void list() {
        assertSeparation(new TagNbtSeparator.Entry<>(Tag.Integer("key").list(), List.of(1)),
                "key", NBT.List(NBTType.TAG_Int, NBT.Int(1)));
    }

    void assertSeparation(Set<TagNbtSeparator.Entry<?>> expected, String key, NBT nbt) {
        assertEquals(expected, retrieve(key, nbt));
    }

    void assertSeparation(TagNbtSeparator.Entry<?> expected, String key, NBT nbt) {
        var entries = retrieve(key, nbt);
        assertEquals(1, entries.size());
        assertEquals(expected, entries.iterator().next());
    }

    Set<TagNbtSeparator.Entry<?>> retrieve(String key, NBT nbt) {
        Set<TagNbtSeparator.Entry<?>> entries = new HashSet<>();
        TagNbtSeparator.separate(key, nbt, entries::add);
        return Set.copyOf(entries);
    }
}
