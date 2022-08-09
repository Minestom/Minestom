package net.minestom.server.entity;

import net.minestom.server.coordinate.Vec;

import java.util.Comparator;
import java.util.function.Predicate;

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

        //todo water movement
//        if (hasNoGravity()) {
//            setVelocity(getVelocity().add(0, -0.3f, 0));
//        }

        //todo lava

        if (lastTargetUpdateTick < time - 20 + getEntityId() % 100) {
            if (target == null || target.getPosition().distanceSquared(getPosition()) > 8 * 8) {
                this.target = getClosestPlayer(this, player -> player.getGameMode() != GameMode.SPECTATOR, 8);
            }

            lastTargetUpdateTick = time;
        }

        if (target != null && target.getGameMode() == GameMode.SPECTATOR) {
            target = null;
        }

        // This is needed due to glitchy clientside predictions
        if (isOnGround()) {
            this.velocity = velocity.withY(-0.5);
        }

        // Slide toward target (Should be after gravity)
        if (this.target != null) {
            Vec targetPos = Vec.fromPoint(this.target.getPosition());
            this.position = this.position.withLookAt(targetPos);

            double distanceToTarget = targetPos.distance(this.position);
            // https://gaming.stackexchange.com/questions/386506/how-quickly-do-experience-orbs-travel
            // License: (CC BY-SA 4.0)
            double velocity = -5.91561 * Math.sin(0.19495 * distanceToTarget) + 5.89166;
            // TODO: Smoothen the clientside prediction
            this.velocity = targetPos.sub(this.position).normalize().mul(velocity);
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
        return getClosestPlayer(entity, player -> true, maxDistance);
    }

    private Player getClosestPlayer(Entity entity, Predicate<Player> predicate, float maxDistance) {
        Player closest = entity.getInstance()
                .getPlayers()
                .stream()
                .filter(predicate)
                .min(Comparator.comparingDouble(a -> a.getDistance(entity)))
                .orElse(null);
        if (closest == null) return null;
        if (closest.getDistance(entity) > maxDistance) return null;
        return closest;
    }
}
