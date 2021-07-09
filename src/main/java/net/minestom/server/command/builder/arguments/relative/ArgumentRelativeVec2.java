package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a {@link Vec} with 2 floating numbers (x;z) which can take relative coordinates.
 * <p>
 * Example: -1.2 ~
 */
public class ArgumentRelativeVec2 extends ArgumentRelativeVec {

    public ArgumentRelativeVec2(@NotNull String id) {
        super(id, 2);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:vec2";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("RelativeVec2<%s>", getId());
    }

    @Override
    Function<String, ? extends Number> getRelativeNumberParser() {
        return Double::parseDouble;
    }

    @Override
    Function<String, ? extends Number> getAbsoluteNumberParser() {
        return Double::parseDouble;
    }
}
