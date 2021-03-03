package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument that will give you a {@link TextDecoration}. Valid values can
 * be found in the text decoration class. Values are case-insensitive.
 */
public class ArgumentTextDecoration extends Argument<TextDecoration> {
    public static final int UNDEFINED_DECORATION = -2;

    public ArgumentTextDecoration(@NotNull String id) {
        super(id);
    }

    @NotNull
    @Override
    public TextDecoration parse(@NotNull String input) throws ArgumentSyntaxException {
        TextDecoration decoration = TextDecoration.NAMES.value(input.toLowerCase());

        if (decoration != null) {
            return decoration;
        }

        throw new ArgumentSyntaxException("Undefined text decoration", input, UNDEFINED_DECORATION);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:text_color";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }
}
