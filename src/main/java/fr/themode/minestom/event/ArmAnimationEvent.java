package fr.themode.minestom.event;

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
