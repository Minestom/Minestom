package net.minestom.server.adventure.nbt;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNbtComponentSerializer {

    @Test
    void testReadStringChildren() {
        var tag = CompoundBinaryTag.builder()
                .putString("text", "Hello")
                .put("extra", ListBinaryTag.from(List.of(
                        stringBinaryTag(" "),
                        stringBinaryTag("World!")
                )))
                .build();
        var deserialized = NbtComponentSerializer.nbt().deserialize(tag);

        var expected = Component.text("Hello").appendSpace().append(Component.text("World!"));
        assertEquals(expected, deserialized);
    }

    @Test
    void testWriteRead() {
        var serializer = NbtComponentSerializer.nbt();
        var comp = Component.text("Hello").appendSpace().append(Component.text("World!"));

        var tag = serializer.serialize(comp);
        var comp2 = serializer.deserialize(tag);

        assertEquals(comp, comp2);
    }

}
