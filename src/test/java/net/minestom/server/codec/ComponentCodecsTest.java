package net.minestom.server.codec;

import net.kyori.adventure.nbt.TagStringIOExt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentCodecsTest {

    @Test
    void readExpandFromStringInList() throws Exception {
        var input = TagStringIOExt.readTag("{extra:[{color:\"red\",text:\"Hello\"},\" World\"],text:\"\"}");
        var actual = ComponentCodecs.COMPONENT.decode(Transcoder.NBT, input).orElseThrow();
        var expected = Component.text()
                .append(Component.text("Hello", NamedTextColor.RED))
                .append(Component.text(" World"))
                .build();
        assertEquals(expected, actual);
    }

    @Test
    void writeFlattenToInList() {
        var component = Component.text()
                .append(Component.text("Hello", NamedTextColor.RED))
                .append(Component.text(" World"))
                .build();
        var nbt = ComponentCodecs.COMPONENT.encode(Transcoder.NBT, component).orElseThrow();
        assertEquals("{extra:[{color:\"red\",text:\"Hello\"},\" World\"],text:\"\"}", TagStringIOExt.writeTag(nbt));
    }

}
