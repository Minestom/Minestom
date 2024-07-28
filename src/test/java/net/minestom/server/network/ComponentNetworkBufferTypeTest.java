package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComponentNetworkBufferTypeTest {
    // All of these tests use NbtComponentSerializerImpl as the source of truth. If there is an inaccuracy in that
    // implementation, these tests will not be accurate.

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

    private static void assertWriteReadEquality(@NotNull Component comp) {
        var array = NetworkBuffer.makeArray(buffer -> buffer.write(COMPONENT, comp));
        // Reading uses the normal nbt serializer, not the direct one.
        var actual = new NetworkBuffer(ByteBuffer.wrap(array)).read(COMPONENT);
        assertEquals(comp, actual);
    }
}
