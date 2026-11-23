package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.color.Color;
import net.minestom.server.color.TeamColor;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

/**
 * Represents an argument which will give you a {@link Color}.
 * <p>
 * Accepts a hex color such as {@code #ff5555} as well as a named color such as {@code red}.
 * <p>
 * Example: #ff5555, red, dark_blue
 */
public class ArgumentHexColor extends Argument<Color> {

    public static final int INVALID_HEX_COLOR = -2;
    public static final int UNDEFINED_COLOR = -3;

    private static final int HEX_LENGTH = 7;

    public ArgumentHexColor(String id) {
        super(id);
    }

    @Override
    public Color parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        if (input.startsWith("#")) return new Color(parseHex(input));

        final TeamColor color = TeamColor.fromName(input);
        if (color != null) return new Color(color.red(), color.green(), color.blue());

        throw new ArgumentSyntaxException("Undefined color", input, UNDEFINED_COLOR);
    }

    private static int parseHex(String input) throws ArgumentSyntaxException {
        if (input.length() != HEX_LENGTH) {
            throw new ArgumentSyntaxException("Hex color must be 6 hexadecimal digits", input, INVALID_HEX_COLOR);
        }
        for (int i = 1; i < HEX_LENGTH; i++) {
            if (Character.digit(input.charAt(i), 16) == -1) {
                throw new ArgumentSyntaxException("Hex color must be 6 hexadecimal digits", input, INVALID_HEX_COLOR);
            }
        }
        return Integer.parseInt(input.substring(1), 16);
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
