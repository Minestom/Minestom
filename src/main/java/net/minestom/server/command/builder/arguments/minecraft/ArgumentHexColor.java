package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

/**
 * Represents an argument which will give you a {@link TextColor} parsed from a
 * 6-digit hex string prefixed with {@code #}.
 * <p>
 * Example: {@code #ff8800}, {@code #FFFFFF}
 * <p>
 * The vanilla {@code minecraft:hex_color} parser was added in 1.21.6.
 */
public class ArgumentHexColor extends Argument<TextColor> {

    public static final int INVALID_HEX_COLOR = -3;

    public ArgumentHexColor(String id) {
        super(id);
    }

    @Override
    public TextColor parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        if (input == null || input.length() != 7 || input.charAt(0) != '#') {
            throw new ArgumentSyntaxException("Invalid hex color", input, INVALID_HEX_COLOR);
        }
        for (int i = 1; i < 7; i++) {
            final char c = input.charAt(i);
            final boolean isHexDigit = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!isHexDigit) {
                throw new ArgumentSyntaxException("Invalid hex color", input, INVALID_HEX_COLOR);
            }
        }
        final int rgb = Integer.parseInt(input, 1, 7, 16);
        return TextColor.color(rgb);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.HEX_COLOR;
    }

    @Override
    public String toString() {
        return String.format("HexColor<%s>", getId());
    }
}
