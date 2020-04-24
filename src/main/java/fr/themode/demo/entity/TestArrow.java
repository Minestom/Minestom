package fr.themode.demo.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ObjectEntity;

public class TestArrow extends ObjectEntity {

    private LivingEntity shooter;

    public TestArrow(LivingEntity shooter) {
        super(EntityType.ARROW.getId());
        this.shooter = shooter;
    }

    @Override
    public void update() {

    }

    @Override
    public void spawn() {

    }

    @Override
    public int getObjectData() {
        return shooter.getEntityId() + 1;
    }

    public LivingEntity getShooter() {
        return shooter;
    }
}
