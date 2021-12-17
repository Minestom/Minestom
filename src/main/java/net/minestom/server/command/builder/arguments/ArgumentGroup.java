package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArgumentGroup extends Argument<CommandContext> {

    private final List<Argument<?>> group;

    public ArgumentGroup(@NotNull String id, @NotNull List<Argument<?>> group) {
        super(id);
        this.group = List.copyOf(group);
    }

    @Override
    public @NotNull CommandContext parse(@NotNull StringReader input) throws CommandException {
        // FIXME: Complete
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        for (int i = 0; i < group.size(); i++) {
            final boolean isLast = i == group.size() - 1;
            group.get(i).processNodes(nodeMaker, executable && isLast);
        }
    }
}
