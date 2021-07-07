package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaxUsageCondition extends UseCountCondition implements RemoverCondition {
    private final boolean removeAfterNoUsageLeft;
    private int maxUsage;

    static {
        REMOVER_CONDITIONS.add(MaxUsageCondition.class);
    }

    public MaxUsageCondition(int maxUsage) {
        this(maxUsage, true);
    }

    public MaxUsageCondition(int maxUsage, boolean removeAfterNoUsageLeft) {
        this.removeAfterNoUsageLeft = removeAfterNoUsageLeft;
        this.maxUsage = maxUsage;
        resetRemainingUsage();
    }

    public void setMaxUsage(int maxUsage) {
        this.maxUsage = maxUsage;
    }

    public void resetRemainingUsage() {
        resetUseCount();
    }

    public int getRemainingUsage() {
        return maxUsage - getUseCount();
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
