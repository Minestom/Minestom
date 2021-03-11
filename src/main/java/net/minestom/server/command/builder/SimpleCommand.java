package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleCommand extends Command {
    public SimpleCommand(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);

        setCondition(this::hasAccess);

        setDefaultExecutor((sender, context) ->
                process(sender, context.getCommandName(), new String[0]));

        final var params = ArgumentType.StringArray("params");
        addSyntax((sender, context) ->
                process(sender, context.getCommandName(), context.get(params)), params);
    }

    /**
     * Called when the command is executed by a {@link CommandSender}.
     *
     * @param sender  the sender which executed the command
     * @param command the command name used
     * @param args    an array containing all the args (split by space char)
     * @return true when the command is successful, false otherwise
     */
    public abstract boolean process(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args);

    /**
     * Called to know if a player has access to the command.
     *
     * @param sender        the command sender to check the access
     * @param commandString the raw command string,
     *                      null if this is an access request
     * @return true if the player has access to the command, false otherwise
     */
    public abstract boolean hasAccess(@NotNull CommandSender sender, @Nullable String commandString);

}
