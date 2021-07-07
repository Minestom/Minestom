package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HiddenCondition implements CommandCondition {
    public static final HiddenCondition HIDDEN_CONDITION = new HiddenCondition();

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return commandString != null;
    }
}
