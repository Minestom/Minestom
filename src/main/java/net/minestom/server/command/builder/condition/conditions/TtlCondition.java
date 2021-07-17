package net.minestom.server.command.builder.condition.conditions;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.ComplexCondition;
import net.minestom.server.command.builder.condition.RemoveCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class TtlCondition implements ComplexCondition {
    private Duration ttl;
    private final boolean removeAfterTtlExpires;
    private long expireAt;

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

    public Duration timeUntilExpiration() {
        return Duration.ofMillis(expireAt-System.currentTimeMillis());
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return System.currentTimeMillis() < expireAt;
    }
}
