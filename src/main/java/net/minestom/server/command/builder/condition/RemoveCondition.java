package net.minestom.server.command.builder.condition;

import java.util.Collection;

@FunctionalInterface
public interface RemoveCondition extends Condition {
    boolean shouldRemove();

    static boolean shouldRemove(Collection<RemoveCondition> conditions) {
        for (RemoveCondition condition : conditions) {
            if (condition.shouldRemove()) {
                return true;
            }
        }
        return false;
    }
}
