package net.minestom.server.event.animation;

import net.minestom.server.event.CancellableEvent;

public class ArmAnimationEvent extends CancellableEvent {

    private ArmAnimationType armAnimationType;

    public ArmAnimationEvent(ArmAnimationType armAnimationType) {
        this.armAnimationType = armAnimationType;
    }

    public ArmAnimationType getArmAnimationType() {
        return armAnimationType;
    }

    public enum ArmAnimationType {
        BOW,
        CROSSBOW,
        TRIDENT,
        SHIELD,
        EAT
    }

}
