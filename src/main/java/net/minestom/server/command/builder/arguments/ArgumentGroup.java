package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.parser.CommandParser;
import net.minestom.server.command.builder.parser.ValidSyntaxHolder;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArgumentGroup extends Argument<CommandContext> {

    public static final int INVALID_ARGUMENTS_ERROR = 1;

    private final Argument<?>[] group;

    public ArgumentGroup(@NotNull String id, @NotNull Argument<?>... group) {
        super(id, true, false);
        this.group = group;
    }

    @NotNull
    @Override
    public CommandContext parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        List<ValidSyntaxHolder> validSyntaxes = new ArrayList<>();
        CommandParser.parse(sender, null, group, input.split(StringUtils.SPACE), input, validSyntaxes, null);

        CommandContext context = new CommandContext(input);
        CommandParser.findMostCorrectSyntax(validSyntaxes, context);
        if (validSyntaxes.isEmpty()) {
            throw new ArgumentSyntaxException("Invalid arguments", input, INVALID_ARGUMENTS_ERROR);
        }

        return context;
    }

    @Override
    public ArgumentParserType parser() {
        return null;
    }

    public List<Argument<?>> group() {
        return List.of(group);
    }
}
