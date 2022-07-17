package net.minestom.server.command.builder.arguments;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandParser;
import net.minestom.server.command.CommandReader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class ArgumentCommand extends Argument<CommandParser.Result> {
    private boolean onlyCorrect;
    private String shortcut = "";

    public ArgumentCommand(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Result<CommandParser.Result> parse(CommandReader reader) {
        if (reader.readWord().equals(getId())) {
            return Result.success(MinecraftServer.getCommandManager().getDispatcher().parse(reader.readRemaining()));
        } else {
            return Result.incompatibleType();
        }
    }

    @Override
    public String parser() {
        return null;
    }

    public boolean isOnlyCorrect() {
        return onlyCorrect;
    }

    public ArgumentCommand setOnlyCorrect(boolean onlyCorrect) {
        this.onlyCorrect = onlyCorrect;
        return this;
    }

    @NotNull
    public String getShortcut() {
        return shortcut;
    }

    @ApiStatus.Experimental
    public ArgumentCommand setShortcut(@NotNull String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Command<%s>", getId());
    }
}
