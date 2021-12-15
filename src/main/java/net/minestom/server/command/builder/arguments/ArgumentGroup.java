package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.command.builder.parser.CommandParser;
import net.minestom.server.command.builder.parser.ValidSyntaxHolder;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArgumentGroup extends Argument<CommandContext> {

    public static final int INVALID_ARGUMENTS_ERROR = 1;

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

    @NotNull
    @Override
    public CommandContext parse(@NotNull String input) throws ArgumentSyntaxException {
        List<ValidSyntaxHolder> validSyntaxes = new ArrayList<>();
        CommandParser.parse(null, group, input.split(StringUtils.SPACE), input, validSyntaxes, null);

        CommandContext context = new CommandContext(input);
        CommandParser.findMostCorrectSyntax(validSyntaxes, context);
        if (validSyntaxes.isEmpty()) {
            throw new ArgumentSyntaxException("Invalid arguments", input, INVALID_ARGUMENTS_ERROR);
        }

        return context;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        for (int i = 0; i < group.size(); i++) {
            final boolean isLast = i == group.size() - 1;
            group.get(i).processNodes(nodeMaker, executable && isLast);
        }
    }
}
