package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class TtlCondition implements RemoverCondition {
    private Duration ttl;
    private final boolean removeAfterTtlExpires;
    private long expireAt;

    static {
        REMOVER_CONDITIONS.add(TtlCondition.class);
    }

    public TtlCondition(Duration ttl) {
        this(ttl, true);
    }

    public TtlCondition(Duration ttl, boolean removeAfterTtlExpires) {
        this.removeAfterTtlExpires = removeAfterTtlExpires;
        this.ttl = ttl;
        resetExpiration();
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public void resetExpiration() {
        expireAt = System.currentTimeMillis() + ttl.toMillis();
    }

    public boolean shouldRemove() {
        return removeAfterTtlExpires && System.currentTimeMillis() > expireAt;
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return System.currentTimeMillis() < expireAt;
    }
}
