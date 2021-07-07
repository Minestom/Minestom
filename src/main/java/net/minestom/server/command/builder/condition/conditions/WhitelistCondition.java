package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WhitelistCondition implements CommandCondition {
    private final List<CommandSender> whitelistedSenders;

    public WhitelistCondition(List<CommandSender> whitelistedSenders) {
        this.whitelistedSenders = whitelistedSenders;
    }

    public List<CommandSender> getWhitelistedSenders() {
        return whitelistedSenders;
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return whitelistedSenders.contains(sender);
    }
}
