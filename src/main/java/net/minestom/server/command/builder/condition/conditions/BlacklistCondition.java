package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlacklistCondition implements CommandCondition {
    private final List<CommandSender> blacklistedSenders;

    public BlacklistCondition(List<CommandSender> blacklistedSenders) {
        this.blacklistedSenders = blacklistedSenders;
    }

    public List<CommandSender> getBlacklistedSenders() {
        return blacklistedSenders;
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return !blacklistedSenders.contains(sender);
    }
}
