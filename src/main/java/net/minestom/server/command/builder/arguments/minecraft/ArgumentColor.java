package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument which will give you a {@link Style} containing the colour or no
 * colour if the argument was {@code reset}.
 * <p>
 * Example: red, white, reset
 */
public class ArgumentColor extends Argument<Style> {

    public static final int UNDEFINED_COLOR = -2;

    public ArgumentColor(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Style parse(@NotNull StringReader input) throws CommandException {
        String color = input.readUnquotedString();

        NamedTextColor value = NamedTextColor.NAMES.value(color);
        if (value != null){
            return Style.style(value);
        }

        if (color.equals("reset")){
            return Style.empty();
        }

        throw CommandException.ARGUMENT_COLOR_INVALID.generateException(input, color);
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
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:color";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("Color<%s>", getId());
    }
}
