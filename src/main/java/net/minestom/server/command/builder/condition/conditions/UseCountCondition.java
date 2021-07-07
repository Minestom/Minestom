package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class UseCountCondition implements CommandCondition {
    private int useCount = 0;

    public void resetUseCount() {
        useCount = 0;
    }

    public void incrementUseCount() {
        useCount++;
    }

    public int getUseCount() {
        return useCount;
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return true;
    }

    @Override
    public Collection<Player> getAffectedPlayers() {
        //noinspection unchecked
        return Collections.EMPTY_LIST;
    }
}
