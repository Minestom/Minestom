package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.Gravitation;
import com.extollit.gaming.ai.path.model.IPathingEntity;
import com.extollit.gaming.ai.path.model.Passibility;
import com.extollit.linalg.immutable.Vec3d;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.utils.Position;

public class PFPathingEntity implements IPathingEntity {

    private EntityCreature entity;

    private float searchRange;
    private Position targetPosition;

    // Capacities
    private boolean fireResistant;
    private boolean cautious;
    private boolean climber;
    private boolean swimmer;
    private boolean aquaphobic;
    private boolean avoidsDoorways;
    private boolean opensDoors;

    public PFPathingEntity(EntityCreature entity) {
        this.entity = entity;
        this.searchRange = entity.getAttributeValue(Attribute.FOLLOW_RANGE);
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
        return searchRange;
    }

    /**
     * Change the search range of the entity
     *
     * @param searchRange the new entity's search range
     */
    public void setSearchRange(float searchRange) {
        this.searchRange = searchRange;
    }

    public boolean isFireResistant() {
        return fireResistant;
    }

    public void setFireResistant(boolean fireResistant) {
        this.fireResistant = fireResistant;
    }

    public boolean isCautious() {
        return cautious;
    }

    public void setCautious(boolean cautious) {
        this.cautious = cautious;
    }

    public boolean isClimber() {
        return climber;
    }

    public void setClimber(boolean climber) {
        this.climber = climber;
    }

    public boolean isSwimmer() {
        return swimmer;
    }

    public void setSwimmer(boolean swimmer) {
        this.swimmer = swimmer;
    }

    public boolean isAquaphobic() {
        return aquaphobic;
    }

    public void setAquaphobic(boolean aquaphobic) {
        this.aquaphobic = aquaphobic;
    }

    public boolean isAvoidsDoorways() {
        return avoidsDoorways;
    }

    public void setAvoidsDoorways(boolean avoidsDoorways) {
        this.avoidsDoorways = avoidsDoorways;
    }

    public boolean isOpensDoors() {
        return opensDoors;
    }

    public void setOpensDoors(boolean opensDoors) {
        this.opensDoors = opensDoors;
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
                return fireResistant;
            }

            @Override
            public boolean cautious() {
                return cautious;
            }

            @Override
            public boolean climber() {
                return climber;
            }

            @Override
            public boolean swimmer() {
                return swimmer;
            }

            @Override
            public boolean aquatic() {
                return false;
            }

            @Override
            public boolean avian() {
                return false;
            }

            @Override
            public boolean aquaphobic() {
                return aquaphobic;
            }

            @Override
            public boolean avoidsDoorways() {
                return avoidsDoorways;
            }

            @Override
            public boolean opensDoors() {
                return opensDoors;
            }
        };
    }

    @Override
    public void moveTo(Vec3d position, Passibility passibility, Gravitation gravitation) {
        final float x = (float) position.x;
        final float y = (float) position.y;
        final float z = (float) position.z;
        this.targetPosition = new Position(x, y, z);

        final float entityY = entity.getPosition().getY();
        if (entityY < y) {
            entity.jump(1);
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
