package net.minestom.server.network;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static net.minestom.server.network.NetworkBuffer.NBT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ComponentNetworkBufferTypeTest {
    @Test
    void empty() {
        var comp = Component.empty();
        assertWriteReadEquality(comp);
    }

    @Test
    void text() {
        var comp = Component.text("Hello, world!");
        assertWriteReadEquality(comp);
    }

    @Test
    void textChildren() {
        var comp = Component.text("Hello, world!").children(List.of(
                Component.text("child 1"),
                Component.text("child 2")
        ));
        assertWriteReadEquality(comp);
    }

    @Test
    void translatable() {
        var comp = Component.translatable("a.b.c", "I am fallback", Component.text("arg1"), Component.text("arg2"));
        assertWriteReadEquality(comp);
    }

    @Test
    void score() {
        var comp = Component.score("test123", "obj");
        assertWriteReadEquality(comp);
    }

    @Test
    void selector() {
        var comp = Component.selector("@a", Component.text(", "));
        assertWriteReadEquality(comp);
    }

    @Test
    void keybind() {
        var comp = Component.keybind("key.jump");
        assertWriteReadEquality(comp);
    }

    @Test
    void textModifiedUtf8() {
        var comp = Component.text("abc\0\0def");
        assertWriteReadEquality(comp);
    }

    @Test
    void hoverAction() {
        var comp = Component.text("hello").hoverEvent(Component.text("world"));
        assertWriteReadEquality(comp);
    }

    @Test
    void testObjectComponentHeadString() {
        var comp = Component.object(ObjectContents.playerHead("Hello"));
        assertWriteReadEquality(comp);
    }

    @Test
    void testObjectComponentHeadUUID() {
        var comp = Component.object(ObjectContents.playerHead(UUID.randomUUID()));
        assertWriteReadEquality(comp);
    }

    @Test
    void objectComponentHeadTexture() {
        var comp = Component.object(ObjectContents.playerHead()
                .texture(Key.key("red"))
                .build());

        final CompoundBinaryTag player = write(comp).getCompound("player");
        assertEquals("red", player.getString("texture"));
        assertFalse(player.contains("body"));
        assertWriteReadEquality(comp);
    }

    @Test
    void objectComponentFallback() {
        var comp = Component.object()
                .contents(ObjectContents.sprite(Key.key("missing")))
                .fallback(Component.text("Missing"))
                .build();

        final CompoundBinaryTag written = write(comp);
        assertInstanceOf(CompoundBinaryTag.class, written.get("fallback"));
        assertEquals(comp, Codec.COMPONENT.decode(Transcoder.NBT, written).orElseThrow());
    }

    private static void assertWriteReadEquality(Component comp) {
        final CompoundBinaryTag written = write(comp);
        final Component actual = Codec.COMPONENT.decode(Transcoder.NBT, written).orElseThrow();
        assertEquals(comp, actual);
    }

    private static CompoundBinaryTag write(Component comp) {
        var array = NetworkBuffer.makeArray(buffer -> buffer.write(COMPONENT, comp));
        var buffer = NetworkBuffer.wrap(array, 0, array.length);
        return assertInstanceOf(CompoundBinaryTag.class, buffer.read(NBT));
    }
}
