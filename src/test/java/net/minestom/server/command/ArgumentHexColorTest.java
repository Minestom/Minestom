package net.minestom.server.command;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentHexColor;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentHexColorTest {

    private final CommandSender sender = new ServerSender();

    @Test
    public void parsesValidHexColors() {
        ArgumentHexColor arg = ArgumentType.HexColor("color");

        assertEquals(TextColor.color(0xFF8800),
                assertDoesNotThrow(() -> arg.parse(sender, "#ff8800")));
        assertEquals(TextColor.color(0xFFFFFF),
                assertDoesNotThrow(() -> arg.parse(sender, "#FFFFFF")));
        assertEquals(TextColor.color(0x000000),
                assertDoesNotThrow(() -> arg.parse(sender, "#000000")));
        assertEquals(TextColor.color(0xABCDEF),
                assertDoesNotThrow(() -> arg.parse(sender, "#aBcDeF")));
    }

    @Test
    public void rejectsMissingHashPrefix() {
        ArgumentHexColor arg = ArgumentType.HexColor("color");
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(sender, "ff8800"));
    }

    @Test
    public void rejectsWrongLength() {
        ArgumentHexColor arg = ArgumentType.HexColor("color");
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(sender, "#fff"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(sender, "#ff88000"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(sender, "#"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(sender, ""));
    }

    @Test
    public void rejectsNonHexCharacters() {
        ArgumentHexColor arg = ArgumentType.HexColor("color");
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(sender, "#gg0000"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(sender, "#ff 800"));
        assertThrows(ArgumentSyntaxException.class, () -> arg.parse(sender, "#zzzzzz"));
    }

    @Test
    public void usesHexColorParserType() {
        ArgumentHexColor arg = ArgumentType.HexColor("color");
        assertEquals(ArgumentParserType.HEX_COLOR, arg.parser());
    }

    @Test
    public void toStringIncludesId() {
        ArgumentHexColor arg = ArgumentType.HexColor("myColor");
        assertEquals("HexColor<myColor>", arg.toString());
    }
}
