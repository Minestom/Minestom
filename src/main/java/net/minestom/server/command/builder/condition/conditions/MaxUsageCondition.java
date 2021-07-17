package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandBase;
import net.minestom.server.command.builder.condition.ComplexCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaxUsageCondition implements ComplexCondition {
    private final boolean removeAfterNoUsageLeft;
    private int maxUsage;
    private int offset;
    private final CommandBase commandBase;

    public MaxUsageCondition(CommandBase commandBase, int maxUsage) {
        this(commandBase, maxUsage, true);
    }

    public MaxUsageCondition(CommandBase commandBase, int maxUsage, boolean removeAfterNoUsageLeft) {
        this.removeAfterNoUsageLeft = removeAfterNoUsageLeft;
        this.maxUsage = maxUsage;
        this.commandBase = commandBase;
        resetRemainingUsage();
    }

    public void setMaxUsage(int maxUsage) {
        this.maxUsage = maxUsage;
    }

    public void resetRemainingUsage() {
        offset = -commandBase.getExecutionCount();
    }

    public int getRemainingUsage() {
        return maxUsage - commandBase.getExecutionCount() + offset;
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return getRemainingUsage() > 0;
    }

    @Override
    public boolean shouldRemove() {
        return removeAfterNoUsageLeft && getRemainingUsage() < 1;
    }
}
