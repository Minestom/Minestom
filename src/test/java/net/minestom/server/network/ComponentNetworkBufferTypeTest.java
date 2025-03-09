package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static net.minestom.server.network.NetworkBuffer.NBT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComponentNetworkBufferTypeTest {
    // All of these tests use NbtComponentSerializerImpl as the source of truth. If there is an inaccuracy in that
    // implementation, these tests will not be accurate. This will be replaced with the adventure serializer once
    // it is merged into adventure (see https://github.com/KyoriPowered/adventure/pull/1084). This can be considered
    // a known-good implementation.

    private static final ComponentNetworkBufferTypeImpl WRITER = new ComponentNetworkBufferTypeImpl();
    private static final NbtComponentSerializer NBT_READER = NbtComponentSerializer.nbt();

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

    private static void assertWriteReadEquality(@NotNull Component comp) {
        var array = NetworkBuffer.makeArray(buffer -> buffer.write(COMPONENT, comp));
        var buffer = NetworkBuffer.wrap(array, 0, array.length);
        var actual = NBT_READER.deserialize(buffer.read(NBT));
        assertEquals(comp, actual);
    }
}
