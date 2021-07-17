package net.minestom.server.command.builder.condition.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.ExecuteCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpCondition implements ExecuteCondition {
    protected Component message = Component.text("Only server operators can use this command!", NamedTextColor.RED);
    protected int requiredLevel;

    public OpCondition(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public OpCondition() {
        this(4);
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        if (sender.getPermissionLevel() >= requiredLevel) {
            return true;
        } else if (commandString != null) {
            sender.sendMessage(message);
        }
        return false;
    }
}
