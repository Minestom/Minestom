package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentLoop<T> extends Argument<List<T>> {

    private final List<Argument<T>> arguments = new ArrayList<>();

    @SafeVarargs
    public ArgumentLoop(@NotNull String id, @NotNull Argument<T>... arguments) {
        super(id, true, true);
        this.arguments.addAll(Arrays.asList(arguments));
    }

    @NotNull
    @Override
    public List<T> parse(@NotNull String input) throws ArgumentSyntaxException {
        // TODO
        return null;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node[] latestNodes = nodeMaker.getLatestNodes();

        for (DeclareCommandsPacket.Node latestNode : latestNodes) {
            final int id = nodeMaker.getNodeIdsMap().getInt(latestNode);

            for (Argument<T> argument : arguments) {
                DeclareCommandsPacket.Node[] latestCache = nodeMaker.getLatestNodes();
                argument.processNodes(nodeMaker, executable);

                NodeMaker.ConfiguredNodes configuredNodes = nodeMaker.getLatestConfiguredNodes();
                // For the next loop argument to start at the same place
                configuredNodes.getOptions().setPreviousNodes(latestCache);
                for (DeclareCommandsPacket.Node lastArgumentNode : configuredNodes.getNodes()) {
                    lastArgumentNode.flags |= 0x08;
                    lastArgumentNode.redirectedNode = id;
                }
            }
        }
    }
}
