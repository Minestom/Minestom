package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument which will give you an {@link RGBLike} color parsed from hex format (e.g. #RRGGBB).
 */
public class ArgumentHexColor extends Argument<RGBLike> {

    public static final int INVALID_HEX_COLOR = -1;

    public ArgumentHexColor(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull RGBLike parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        if (input.startsWith("#")) {
            TextColor color = TextColor.fromHexString(input);
            if (color != null) {
                return new Color(color);
            }
        }
        throw new ArgumentSyntaxException("Invalid hex color format", input, INVALID_HEX_COLOR);
    }

    @Override
    public @NotNull ArgumentParserType parser() {
        return ArgumentParserType.HEX_COLOR;
    }

    @Override
    public String toString() {
        return String.format("HexColor<%s>", getId());
    }
}
