package fr.themode.minestom.entity;

public abstract class LivingEntity extends Entity {

    protected float yaw, pitch;
    protected boolean onGround;

    public LivingEntity() {
        super();
    }

    public abstract void update();

    public boolean chunkTest(double x, double z) {
        return getInstance().getChunk((int) Math.floor(x / 16), (int) Math.floor(z / 16)) == null;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

}
