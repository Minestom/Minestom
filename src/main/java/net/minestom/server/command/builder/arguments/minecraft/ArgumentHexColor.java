package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.color.Color;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

/**
 * Represents an argument which will give you a {@link Color}.
 * <p>
 * Accepts a hex color such as {@code ff5050} or shorthand such as {@code f00}.
 * <p>
 * Example: ff5050, FF5050, f00, F00
 */
public class ArgumentHexColor extends Argument<Color> {

    public static final int INVALID_HEX_COLOR = -2;

    public ArgumentHexColor(String id) {
        super(id);
    }

    @Override
    public Color parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        final int length = input.length();
        if (length == 6) {
            for (int i = 0; i < 6; i++) {
                if (Character.digit(input.charAt(i), 16) == -1) {
                    throw new ArgumentSyntaxException("Hex color must be 3 or 6 hexadecimal digits", input, INVALID_HEX_COLOR);
                }
            }
            return new Color(Integer.parseInt(input, 16));
        } else if (length == 3) {
            final int r = Character.digit(input.charAt(0), 16);
            final int g = Character.digit(input.charAt(1), 16);
            final int b = Character.digit(input.charAt(2), 16);
            if (r == -1 || g == -1 || b == -1) {
                throw new ArgumentSyntaxException("Hex color must be 3 or 6 hexadecimal digits", input, INVALID_HEX_COLOR);
            }
            return new Color((r << 4) | r, (g << 4) | g, (b << 4) | b);
        }

        throw new ArgumentSyntaxException("Hex color must be 3 or 6 hexadecimal digits", input, INVALID_HEX_COLOR);
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
