package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.builder.condition.CommandCondition;

import java.util.HashSet;
import java.util.Set;

public interface RemoverCondition extends CommandCondition {
    Set<Class<? extends RemoverCondition>> REMOVER_CONDITIONS = new HashSet<>();

    boolean shouldRemove();
}
