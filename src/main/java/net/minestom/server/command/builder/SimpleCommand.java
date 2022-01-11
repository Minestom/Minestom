package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleCommand extends Command {
    public SimpleCommand(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);

        setCondition(this::hasAccess);

        setDefaultExecutor((sender, context) -> process(sender, context.getMessage(), context.getStartingPosition()));

        final var params = ArgumentType.String("params").setReadType(ArgumentString.ReadType.GREEDY);
        addSyntax((sender, context) -> process(sender, context.getMessage(), context.getStartingPosition()), params);
    }

    /**
     * Called when the command is executed by a {@link CommandSender}.
     *
     * @param sender the sender who executed the command
     * @param command the entire string that is the command
     * @param startingPosition the position in the {@code command} argument where the parsing started
     * @return true if the command was successfully executed, otherwise false
     */
    public abstract boolean process(@NotNull CommandSender sender, @NotNull String command, int startingPosition);

    /**
     * Used to know if the sender has access to the command.
     *
     * @param sender the sender who sent the command
     * @param command the entire string that is the command
     * @param startingPosition the position in the {@code command} argument where the parsing started
     * @return true if the sender can run the command, otherwise false
     */
    public abstract boolean hasAccess(@NotNull CommandSender sender, @Nullable String command, int startingPosition);

}
