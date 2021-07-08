package net.minestom.server.entity;

import net.minestom.server.coordinate.Vec;

import java.util.Comparator;

public class ExperienceOrb extends Entity {

    private short experienceCount;
    private Player target;
    private long lastTargetUpdateTick;

    public ExperienceOrb(short experienceCount) {
        super(EntityType.EXPERIENCE_ORB);
        setBoundingBox(0.5f, 0.5f, 0.5f);
        //todo vanilla sets random velocity here?
        this.experienceCount = experienceCount;
    }

    @Override
    public void update(long time) {

        // TODO slide toward nearest player

        //todo water movement
        if (hasNoGravity()) {
            setVelocity(getVelocity().add(0, -0.3f, 0));
        }

        //todo lava

        double d = 8.0;
        if (lastTargetUpdateTick < time - 20 + getEntityId() % 100) {
            if (target == null || target.getPosition().distanceSquared(getPosition()) > 64) {
                this.target = getClosestPlayer(this, 8);
            }

            lastTargetUpdateTick = time;
        }

        if (target != null && target.getGameMode() == GameMode.SPECTATOR) {
            target = null;
        }

        if (target != null) {
            final var pos = getPosition();
            final var targetPos = target.getPosition();
            final Vec toTarget = new Vec(targetPos.x() - pos.x(), targetPos.y() + (target.getEyeHeight() / 2) - pos.y(), targetPos.z() - pos.z());
            double e = toTarget.length(); //could really be lengthSquared
            if (e < 8) {
                double f = 1 - (e / 8);
                setVelocity(getVelocity().add(toTarget.normalize().mul(f * f * 0.1)));
            }
        }

        // Move should be called here
        float g = 0.98f;
        if (this.onGround) {
//            g = 2f;
            g = 0.6f * 0.98f;
        }
        // apply slipperiness

        setVelocity(getVelocity().mul(new Vec(g, 0.98f, g)));
        if (isOnGround()) {
            setVelocity(getVelocity().mul(new Vec(1, -0.9f, 1)));
        }
    }

    @Override
    public void spawn() {

    }

    /**
     * Gets the experience count.
     *
     * @return the experience count
     */
    public short getExperienceCount() {
        return experienceCount;
    }

    /**
     * Changes the experience count.
     *
     * @param experienceCount the new experience count
     */
    public void setExperienceCount(short experienceCount) {
        // Remove the entity in order to respawn it with the correct experience count
        getViewers().forEach(this::removeViewer);

        this.experienceCount = experienceCount;

        getViewers().forEach(this::addViewer);
    }

    private Player getClosestPlayer(Entity entity, float maxDistance) {
        Player closest = entity.getInstance()
                .getPlayers()
                .stream()
                .min(Comparator.comparingDouble(a -> a.getDistance(entity)))
                .orElse(null);
        if (closest == null) return null;
        if (closest.getDistance(entity) > maxDistance) return null;
        return closest;
    }
}
