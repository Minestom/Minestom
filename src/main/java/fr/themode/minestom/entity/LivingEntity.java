package fr.themode.minestom.entity;

public abstract class LivingEntity extends Entity {

    protected float yaw, pitch;
    protected boolean onGround;

    public LivingEntity() {
        super();
    }

    public abstract void update();

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

}
