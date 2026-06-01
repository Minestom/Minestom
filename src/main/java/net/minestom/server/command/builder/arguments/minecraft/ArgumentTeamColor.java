package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

/**
 * Represents an argument which will give you a {@link Style} containing the team color
 * <p>
 * Example: red, white, reset
 */
public class ArgumentTeamColor extends Argument<Style> {

    public static final int UNDEFINED_COLOR = -2;

    public ArgumentTeamColor(String id) {
        super(id);
    }

    @Override
    public Style parse(CommandSender sender, String input) throws ArgumentSyntaxException {

        // check for color
        NamedTextColor color = NamedTextColor.NAMES.value(input);
        if (color != null) {
            return Style.style(color);
        }

        throw new ArgumentSyntaxException("Undefined color", input, UNDEFINED_COLOR);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.TEAM_COLOR;
    }

    @Override
    public String toString() {
        return String.format("Color<%s>", getId());
    }
}
