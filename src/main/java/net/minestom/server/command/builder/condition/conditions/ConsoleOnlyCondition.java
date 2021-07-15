package net.minestom.server.command.builder.condition.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.ExecuteCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConsoleOnlyCondition implements ExecuteCondition {
    protected Component message = Component.text("The command is only available form the console", NamedTextColor.RED);
    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        if (!sender.isConsole()) {
            if (commandString != null) {
                sender.sendMessage(message);
            }
            return false;
        }
        return true;
    }
}
