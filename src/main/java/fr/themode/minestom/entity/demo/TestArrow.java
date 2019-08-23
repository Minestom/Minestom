package fr.themode.minestom.entity.demo;

import fr.themode.minestom.entity.LivingEntity;
import fr.themode.minestom.entity.ObjectEntity;

public class TestArrow extends ObjectEntity {

    private LivingEntity shooter;

    public TestArrow(LivingEntity shooter) {
        super(2);
        this.shooter = shooter;
    }

    @Override
    public int getObjectData() {
        return shooter.getEntityId() + 1;
    }

    @Override
    public void update() {

    }
}
