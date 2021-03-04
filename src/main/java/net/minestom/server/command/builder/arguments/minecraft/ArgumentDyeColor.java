package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.color.DyeColor;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * An argument that returns a {@link DyeColor} from the name of the dye color.
 */
public class ArgumentDyeColor extends Argument<DyeColor> {
    public static int UNDEFINED_DYE_COLOR = -2;

    public ArgumentDyeColor(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull DyeColor parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            return DyeColor.valueOf(input.toUpperCase().replace(' ', '_').trim());
        } catch (IllegalArgumentException ignored) {
            throw new ArgumentSyntaxException("Undefined dye color", input, UNDEFINED_DYE_COLOR);
        }
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {

    }
}
