package net.minestom.server.entity;

import net.minestom.server.network.packet.server.play.SpawnExperienceOrbPacket;
import net.minestom.server.network.player.PlayerConnection;

public class ExperienceOrb extends Entity {

    private short experienceCount;

    public ExperienceOrb(short experienceCount) {
        super(EntityType.EXPERIENCE_ORB);
        setGravity(0.02f);
        setBoundingBox(0.5f, 0.5f, 0.5f);
        this.experienceCount = experienceCount;
    }

    @Override
    public void update(long time) {
        // TODO slide toward nearest player
    }

    @Override
    public void spawn() {

    }

    @Override
    public boolean addViewer(Player player) {
        final boolean result = super.addViewer(player); // Add player to viewers list
        if (!result)
            return false;

        final PlayerConnection playerConnection = player.getPlayerConnection();

        SpawnExperienceOrbPacket experienceOrbPacket = new SpawnExperienceOrbPacket();
        experienceOrbPacket.entityId = getEntityId();
        experienceOrbPacket.position = getPosition();
        experienceOrbPacket.expCount = experienceCount;

        playerConnection.sendPacket(experienceOrbPacket);
        playerConnection.sendPacket(getVelocityPacket());

        return true;
    }

    /**
     * Get the experience count
     *
     * @return the experience count
     */
    public short getExperienceCount() {
        return experienceCount;
    }

    /**
     * Change the experience count
     *
     * @param experienceCount the new experience count
     */
    public void setExperienceCount(short experienceCount) {
        // Remove the entity in order to respawn it with the correct experience count
        getViewers().forEach(this::removeViewer);

        this.experienceCount = experienceCount;

        getViewers().forEach(this::addViewer);
    }
}
