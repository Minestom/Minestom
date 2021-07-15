package net.minestom.server.command.builder.condition;


import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandBase;
import net.minestom.server.command.builder.condition.conditions.*;

import java.time.Duration;
import java.util.List;

/**
 * Common command conditions
 */
public class Conditions {

    private static final ExecuteCondition hiddenCondition = new HiddenCondition();
    private static final ExecuteCondition playerOnlyCondition = new PlayerOnlyCondition();
    private static final ExecuteCondition consoleOnlyCondition = new ConsoleOnlyCondition();
    private static final ExecuteCondition opCondition = new OpCondition();

    private Conditions() {
        //no instance
    }

    public static ExecuteCondition blackList(List<CommandSender> blacklistedSenders) {
        return new BlacklistCondition(blacklistedSenders);
    }

    public static ExecuteCondition whiteList(List<CommandSender> whitelistedSenders) {
        return new WhitelistCondition(whitelistedSenders);
    }

    public static ExecuteCondition hidden() {
        return hiddenCondition;
    }

    public static ExecuteCondition playerOnly() {
        return playerOnlyCondition;
    }

    public static ExecuteCondition consoleOnly() {
        return consoleOnlyCondition;
    }

    public static ExecuteCondition op() {
        return opCondition;
    }

    public static ExecuteCondition op(int level) {
        return new OpCondition(level);
    }

    public static ComplexCondition maxUsage(CommandBase commandBase, int maxUsage) {
        return maxUsage(commandBase, maxUsage, true);
    }

    public static ComplexCondition maxUsage(CommandBase commandBase, int maxUsage, boolean removeAfterNoUsageLeft) {
        return new MaxUsageCondition(commandBase, maxUsage, removeAfterNoUsageLeft);
    }

    public static ComplexCondition ttl(Duration ttl) {
        return ttl(ttl, true);
    }

    public static ComplexCondition ttl(Duration ttl, boolean removeAfterTtlExpires) {
        return new TtlCondition(ttl, removeAfterTtlExpires);
    }

    /**
     * @deprecated Use {@link #playerOnly()}
     */
    @Deprecated
    public static boolean playerOnly(CommandSender sender, String commandString) {
        return playerOnlyCondition.canUse(sender, commandString);
    }

    /**
     * @deprecated Use {@link #consoleOnly()}
     */
    @Deprecated
    public static boolean consoleOnly(CommandSender sender, String commandString) {
        return consoleOnlyCondition.canUse(sender, commandString);
    }
}
