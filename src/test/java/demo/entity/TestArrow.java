package demo.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.utils.Position;

public class TestArrow extends ObjectEntity {

    private LivingEntity shooter;

    public TestArrow(LivingEntity shooter, Position spawnPosition) {
        super(EntityType.ARROW, spawnPosition);
        this.shooter = shooter;
    }

    @Override
    public int getObjectData() {
        return shooter.getEntityId() + 1;
    }

    public LivingEntity getShooter() {
        return shooter;
    }
}
