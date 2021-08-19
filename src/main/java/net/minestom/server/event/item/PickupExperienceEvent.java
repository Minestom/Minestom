package net.minestom.server.event.item;

import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

public class PickupExperienceEvent implements CancellableEvent {

    private final ExperienceOrb experienceOrb;
    private final Player player;
    private short experienceCount;

    private boolean cancelled;

    public PickupExperienceEvent(@NotNull ExperienceOrb experienceOrb, @NotNull Player player) {
        this.experienceOrb = experienceOrb;
        this.player = player;
        this.experienceCount = experienceOrb.getExperienceCount();
    }

    @NotNull
    public ExperienceOrb getExperienceOrb() {
        return experienceOrb;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public short getExperienceCount() {
        return experienceCount;
    }

    public void setExperienceCount(short experienceCount) {
        this.experienceCount = experienceCount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
