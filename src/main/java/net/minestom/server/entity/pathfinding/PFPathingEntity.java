package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.Gravitation;
import com.extollit.gaming.ai.path.model.IPathingEntity;
import com.extollit.gaming.ai.path.model.Passibility;
import com.extollit.linalg.immutable.Vec3d;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeImpl;
import net.minestom.server.attribute.VanillaAttribute;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class PFPathingEntity implements IPathingEntity {
    private final Navigator navigator;
    private final Entity entity;

    private float searchRange;

    // Capacities
    private boolean fireResistant;
    private boolean cautious;
    private boolean climber;
    private boolean swimmer;
    private boolean aquatic;
    private boolean avian;
    private boolean aquaphobic;
    private boolean avoidsDoorways;
    private boolean opensDoors;

    public PFPathingEntity(Navigator navigator) {
        this.navigator = navigator;
        this.entity = navigator.getEntity();

        this.searchRange = getAttributeValue(VanillaAttribute.GENERIC_FOLLOW_RANGE);
    }

    @Override
    public int age() {
        return (int) entity.getAliveTicks();
    }

    @Override
    public boolean bound() {
        return entity.hasVelocity();
    }

    @Override
    public float searchRange() {
        return searchRange;
    }

    /**
     * Changes the search range of the entity
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

    public boolean isAquatic() {
        return aquatic;
    }

    public void setAquatic(boolean aquatic) {
        this.aquatic = aquatic;
    }

    public boolean isAvian() {
        return avian;
    }

    public void setAvian(boolean avian) {
        this.avian = avian;
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
                return getAttributeValue(VanillaAttribute.GENERIC_MOVEMENT_SPEED);
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
                return aquatic;
            }

            @Override
            public boolean avian() {
                return avian;
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
        final Point targetPosition = new Vec(position.x, position.y, position.z);
        this.navigator.moveTowards(targetPosition, getAttributeValue(VanillaAttribute.GENERIC_MOVEMENT_SPEED));
        final double entityY = entity.getPosition().y() + 0.00001D; // After any negative y movement, entities will always be extremely
                                                                    // slightly below floor level. This +0.00001D is here to offset this
                                                                    // error and stop the entity from permanently jumping.

        if (entityY < targetPosition.y()) {
            this.navigator.jump(1);
        }
    }

    @Override
    public Vec3d coordinates() {
        final var position = entity.getPosition();
        return new Vec3d(position.x(), position.y(), position.z());
    }

    @Override
    public float width() {
        return (float) entity.getBoundingBox().width();
    }

    @Override
    public float height() {
        return (float) entity.getBoundingBox().height();
    }

    private float getAttributeValue(@NotNull Attribute attribute) {
        if (entity instanceof LivingEntity) {
            return ((LivingEntity) entity).getAttributeValue(attribute);
        }
        return 0f;
    }
}
