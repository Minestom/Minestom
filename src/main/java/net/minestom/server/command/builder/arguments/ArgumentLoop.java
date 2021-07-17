package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentLoop<T> extends Argument<List<T>> {

    public static final int INVALID_INPUT_ERROR = 1;

    private final List<Argument<T>> arguments = new ArrayList<>();

    @SafeVarargs
    public ArgumentLoop(@NotNull String id, @NotNull Argument<T>... arguments) {
        super(id);
        this.arguments.addAll(Arrays.asList(arguments));
    }

    @NotNull
    @Override
    public List<T> parse(@NotNull ArgumentReader reader) throws ArgumentSyntaxException {
        // FIXME: 2021. 07. 17. Loop properly, maybe use ArgumentGroup?
        List<T> result = new ArrayList<>();

        for (Argument<T> argument : arguments) {
            final T value = argument.parse(reader);
            result.add(value);
        }

        return result;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node[] latestNodes = nodeMaker.getLatestNodes();

        for (DeclareCommandsPacket.Node latestNode : latestNodes) {
            final int id = nodeMaker.getNodeIdsMap().getInt(latestNode);

            for (Argument<T> argument : arguments) {
                argument.processNodes(nodeMaker, executable);

                NodeMaker.ConfiguredNodes configuredNodes = nodeMaker.getLatestConfiguredNodes();
                // For the next loop argument to start at the same place
                configuredNodes.getOptions().setPreviousNodes(latestNodes);
                for (DeclareCommandsPacket.Node lastArgumentNode : configuredNodes.getNodes()) {
                    lastArgumentNode.flags |= 0x08;
                    lastArgumentNode.redirectedNode = id;
                }
            }
        }
    }
}
