package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument which will give you a {@link ChatColor}.
 * <p>
 * Example: red, white, reset
 */
public class ArgumentColor extends Argument<ChatColor> {

    public static final int UNDEFINED_COLOR = -2;

    public ArgumentColor(String id) {
        super(id);
    }

    @NotNull
    @Override
    public ChatColor parse(@NotNull String input) throws ArgumentSyntaxException {
        final ChatColor color = ChatColor.fromName(input);
        if (color == ChatColor.NO_COLOR)
            throw new ArgumentSyntaxException("Undefined color", input, UNDEFINED_COLOR);

        return color;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:color";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }
}
