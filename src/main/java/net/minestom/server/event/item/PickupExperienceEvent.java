package net.minestom.server.event.item;

import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.event.CancellableEvent;

public class PickupExperienceEvent extends CancellableEvent {

    private ExperienceOrb experienceOrb;
    private short experienceCount;

    public PickupExperienceEvent(ExperienceOrb experienceOrb) {
        this.experienceOrb = experienceOrb;
        this.experienceCount = experienceOrb.getExperienceCount();
    }

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
