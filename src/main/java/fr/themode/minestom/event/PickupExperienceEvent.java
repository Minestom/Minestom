package fr.themode.minestom.event;

public class PickupExperienceEvent extends CancellableEvent {

    private short experienceCount;

    public PickupExperienceEvent(short experienceCount) {
        this.experienceCount = experienceCount;
    }

    public short getExperienceCount() {
        return experienceCount;
    }

    public void setExperienceCount(short experienceCount) {
        this.experienceCount = experienceCount;
    }
}
