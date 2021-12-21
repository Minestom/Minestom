package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArgumentLoop<T> extends Argument<List<T>> {

    private final List<Argument<T>> arguments;

    @SafeVarargs
    public ArgumentLoop(@NotNull String id, @NotNull Argument<T>... arguments) {
        super(id);
        this.arguments = List.of(arguments);
    }

    @Override
    public @NotNull List<T> parse(@NotNull StringReader input) throws CommandException {
        int pos = input.position();
        List<T> result = new ArrayList<>();

        for (int i = 0; i < this.arguments.size(); i++) {
            try {
                result.add(arguments.get(i).parse(input));
            } catch (CommandException exception) {
                input.position(pos);
                throw exception;
            }
            if (i != this.arguments.size() - 1) {
                if (!input.canRead() || !StringReader.isValidWhitespace(input.peek())) {
                    throw CommandException.COMMAND_EXPECTED_SEPARATOR.generateException(input.all(), pos);
                }
                input.skipWhitespace();
            }
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
