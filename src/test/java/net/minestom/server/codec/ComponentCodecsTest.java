package net.minestom.server.codec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.MinestomAdventure;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentCodecsTest {

    @Test
    void readExpandFromStringInList() throws Exception {
        var input = MinestomAdventure.tagStringIO().asTag("{extra:[{color:\"red\",text:\"Hello\"},\" World\"],text:\"\"}");
        var actual = ComponentCodecs.COMPONENT.decode(Transcoder.NBT, input).orElseThrow();
        var expected = Component.text()
                .append(Component.text("Hello", NamedTextColor.RED))
                .append(Component.text(" World"))
                .build();
        assertEquals(expected, actual);
    }

    @Test
    void writeFlattenToInList() throws IOException {
        var component = Component.text()
                .append(Component.text("Hello", NamedTextColor.RED))
                .append(Component.text(" World"))
                .build();
        var nbt = ComponentCodecs.COMPONENT.encode(Transcoder.NBT, component).orElseThrow();
        assertEquals("{extra:[{color:\"red\",text:\"Hello\"},\" World\"],text:\"\"}", MinestomAdventure.tagStringIO().asString(nbt));
    }

    @Test
    void writeCustomClickEvent() throws IOException {
        var component = Component.text("Click me!").clickEvent(ClickEvent.custom(
                Key.key("hello:world"), MinestomAdventure.wrapNbt(IntBinaryTag.intBinaryTag(55))));
        var nbt = ComponentCodecs.COMPONENT.encode(Transcoder.NBT, component).orElseThrow();
        assertEquals("{click_event:{payload:55,action:\"custom\",id:\"hello:world\"},text:\"Click me!\"}",
                MinestomAdventure.tagStringIO().asString(nbt));
    }

    @Test
    void readCustomClickEvent() throws IOException {
        var input = MinestomAdventure.tagStringIO().asTag("{click_event:{payload:55,action:\"custom\",id:\"hello:world\"},text:\"Click me!\"}");
        var actual = ComponentCodecs.COMPONENT.decode(Transcoder.NBT, input).orElseThrow();
        var expected = Component.text("Click me!").clickEvent(ClickEvent.custom(
                Key.key("hello:world"), MinestomAdventure.wrapNbt(IntBinaryTag.intBinaryTag(55))));
        assertEquals(expected, actual);
    }

}
