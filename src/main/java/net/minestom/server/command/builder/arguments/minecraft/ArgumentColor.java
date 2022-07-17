package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
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

    @Override
    public @NotNull Result<Style> parse(CommandReader reader) {
        final String input = reader.readWord();
        // check for colour
        NamedTextColor color = NamedTextColor.NAMES.value(input);
        if (color != null) {
            return Result.success(Style.style(color));
        }

        // check for reset
        if (input.equals("reset")) {
            return Result.success(Style.empty());
        }

        return Result.syntaxError("Undefined color", input, UNDEFINED_COLOR);
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
