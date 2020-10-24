package net.minestom.server.event.item;

import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;

public class PickupExperienceEvent extends CancellableEvent {

    private final ExperienceOrb experienceOrb;
    private short experienceCount;

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
}
