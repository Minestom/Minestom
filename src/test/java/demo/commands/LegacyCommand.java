package demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.SimpleCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegacyCommand extends SimpleCommand {
    public LegacyCommand() {
        super("test", "alias");
    }

    @Override
    public boolean process(@NotNull CommandOrigin origin, @NotNull String command, int startingPosition) {
        System.gc();
        origin.sender().sendMessage(Component.text("Successfully ran the garbage collector!", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public boolean hasAccess(@NotNull CommandOrigin origin, @Nullable String command, int startingPosition) {
        return true;
    }

}
