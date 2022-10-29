package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument which will give you a {@link Style} containing the colour or no
 * colour if the argument was {@code reset}.
 * <p>
 * Example: red, white, reset
 */
public class ArgumentColor extends Argument<Style> {

    public static final int UNDEFINED_COLOR = -2;

    public ArgumentColor(String id) {
        super(id);
    }

    @NotNull
    @Override
    public Style parse(@NotNull String input) throws ArgumentSyntaxException {

        // check for colour
        NamedTextColor color = NamedTextColor.NAMES.value(input);
        if (color != null) {
            return Style.style(color);
        }

        // check for reset
        if (input.equals("reset")) {
            return Style.empty();
        }

        throw new ArgumentSyntaxException("Undefined color", input, UNDEFINED_COLOR);
    }

    @Override
    public String parser() {
        return "minecraft:color";
    }

    @Override
    public String toString() {
        return String.format("Color<%s>", getId());
    }
}
