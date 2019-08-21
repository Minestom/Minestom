package fr.themode.minestom.entity;

// TODO attributes https://wiki.vg/Protocol#Entity_Properties
public abstract class LivingEntity extends Entity {

    protected boolean onGround;

    public LivingEntity(int entityType) {
        super(entityType);
    }

}
