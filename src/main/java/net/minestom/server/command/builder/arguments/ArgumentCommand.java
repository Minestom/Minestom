package net.minestom.server.command.builder.arguments;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.ApiStatus;

public class ArgumentCommand extends Argument<CommandResult> {

    public static final int INVALID_ID_ERROR = -1;
    public static final int INVALID_COMMAND_ERROR = 1;

    private boolean onlyCorrect;
    private String shortcut = "";

    public ArgumentCommand(String id) {
        super(id, true, true);
    }

    @Override
    public CommandResult parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        String command = input;
        if (command.startsWith(getId())) {
            command = command.substring(getId().length()).stripLeading();
        } else {
            throw new ArgumentSyntaxException("Invalid literal value", input, INVALID_ID_ERROR);
        }
        if (!shortcut.isEmpty()) {
            command = shortcut + StringUtils.SPACE + command;
        }

        CommandDispatcher dispatcher = MinecraftServer.getCommandManager().getDispatcher();
        CommandResult result = dispatcher.parse(sender, command);

        if (result.getType() == CommandResult.Type.UNKNOWN || (onlyCorrect && result.getType() != CommandResult.Type.SUCCESS))
            throw new ArgumentSyntaxException("Invalid command", input, INVALID_COMMAND_ERROR);

        return result;
    }

    @Override
    public ArgumentParserType parser() {
        return null;
    }

    public boolean isOnlyCorrect() {
        return onlyCorrect;
    }

    public ArgumentCommand setOnlyCorrect(boolean onlyCorrect) {
        this.onlyCorrect = onlyCorrect;
        return this;
    }

    public String getShortcut() {
        return shortcut;
    }

    @ApiStatus.Experimental
    public ArgumentCommand setShortcut(String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Command<%s>", getId());
    }
}
