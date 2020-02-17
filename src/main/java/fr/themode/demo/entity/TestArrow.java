package fr.themode.demo.entity;

import fr.themode.minestom.entity.LivingEntity;
import fr.themode.minestom.entity.ObjectEntity;

public class TestArrow extends ObjectEntity {

    private LivingEntity shooter;

    public TestArrow(LivingEntity shooter) {
        super(2);
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
