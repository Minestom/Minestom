package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IPathingEntity;
import com.extollit.linalg.immutable.Vec3d;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.utils.Position;

public class PFPathingEntity implements IPathingEntity {

    private static final float SEARCH_RANGE = 32;

    private EntityCreature entity;

    private Position targetPosition;

    public PFPathingEntity(EntityCreature entity) {
        this.entity = entity;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    @Override
    public int age() {
        return (int) entity.getAliveTicks();
    }

    @Override
    public float searchRange() {
        return SEARCH_RANGE;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities() {
            @Override
            public float speed() {
                return entity.getAttributeValue(Attribute.MOVEMENT_SPEED);
            }

            @Override
            public boolean fireResistant() {
                return false;
            }

            @Override
            public boolean cautious() {
                return false;
            }

            @Override
            public boolean climber() {
                return true;
            }

            @Override
            public boolean swimmer() {
                return true;
            }

            @Override
            public boolean aquaphobic() {
                return false;
            }

            @Override
            public boolean avoidsDoorways() {
                return false;
            }

            @Override
            public boolean opensDoors() {
                return false;
            }
        };
    }

    @Override
    public void moveTo(Vec3d position) {
        final float x = (float) position.x;
        final float y = (float) position.y;
        final float z = (float) position.z;
        this.targetPosition = new Position(x, y, z);

        final float entityY = entity.getPosition().getY();
        if (entityY < y) {
            //entity.jump(1);
        }
    }

    @Override
    public Vec3d coordinates() {
        final Position position = entity.getPosition();
        return new Vec3d(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public float width() {
        return entity.getBoundingBox().getWidth();
    }

    @Override
    public float height() {
        return entity.getBoundingBox().getHeight();
    }
}
