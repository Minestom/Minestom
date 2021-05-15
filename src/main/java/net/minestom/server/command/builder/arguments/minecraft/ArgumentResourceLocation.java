package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ArgumentResourceLocation extends Argument<String> {

    public static final int SPACE_ERROR = 1;

    public ArgumentResourceLocation(@NotNull String id) {
        super(id);
    }

    @NotNull
    @Override
    public String parse(@NotNull String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Resource location cannot contain space character", input, SPACE_ERROR);

        return input;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:resource_location";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("ResourceLocation<%s>", getId());
    }
}
