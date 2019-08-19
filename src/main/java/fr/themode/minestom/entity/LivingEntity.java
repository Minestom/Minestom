package fr.themode.minestom.entity;

public abstract class LivingEntity extends Entity {

    protected boolean onGround;

    public LivingEntity(int entityType) {
        super(entityType);
    }

    public boolean chunkTest(double x, double z) {
        return getInstance().getChunk((int) Math.floor(x / 16), (int) Math.floor(z / 16)) == null;
    }

}
