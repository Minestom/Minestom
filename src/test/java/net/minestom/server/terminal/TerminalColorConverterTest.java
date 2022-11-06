package net.minestom.server.terminal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalColorConverterTest {
    @Test
    void testFormat() {
        String input = "§c§lHello §r§b§lWorld";
        String expected = "\u001B[38;2;255;85;85m\u001B[1mHello \u001B[m\u001B[38;2;85;255;255m\u001B[1mWorld\u001B[m";
        String actual = TerminalColorConverter.format(input);
        assertEquals(expected, actual);
    }

    @Test
    void testComponentFormat() {
        Component input = Component.text("Hello World").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        String expected = "\u001B[38;2;255;85;85m\u001B[1mHello World\u001B[m";
        String actual = TerminalColorConverter.format(LegacyComponentSerializer.legacySection().serialize(input));
        assertEquals(expected, actual);
    }
}