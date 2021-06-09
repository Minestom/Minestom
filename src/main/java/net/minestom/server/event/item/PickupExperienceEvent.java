package net.minestom.server.event.item;

import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

public class PickupExperienceEvent implements CancellableEvent {

    private final ExperienceOrb experienceOrb;
    private short experienceCount;

    private boolean cancelled;

    public PickupExperienceEvent(@NotNull ExperienceOrb experienceOrb) {
        this.experienceOrb = experienceOrb;
        this.experienceCount = experienceOrb.getExperienceCount();
    }

    @NotNull
    public ExperienceOrb getExperienceOrb() {
        return experienceOrb;
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
