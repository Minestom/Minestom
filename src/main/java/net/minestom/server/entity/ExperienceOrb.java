package net.minestom.server.entity;

import net.minestom.server.network.packet.server.play.SpawnExperienceOrbPacket;
import net.minestom.server.network.player.PlayerConnection;

import java.util.HashSet;
import java.util.Set;

public class ExperienceOrb extends Entity {

    private short experienceCount;

    public ExperienceOrb(short experienceCount) {
        super(23);
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
        PlayerConnection playerConnection = player.getPlayerConnection();

        SpawnExperienceOrbPacket experienceOrbPacket = new SpawnExperienceOrbPacket();
        experienceOrbPacket.entityId = getEntityId();
        experienceOrbPacket.position = getPosition();
        experienceOrbPacket.expCount = experienceCount;

        playerConnection.sendPacket(experienceOrbPacket);
        playerConnection.sendPacket(getVelocityPacket());

        return super.addViewer(player); // Add player to viewers list
    }

    /**
     * @return the experience amount contained in the entity
     */
    public short getExperienceCount() {
        return experienceCount;
    }

    /**
     * @param experienceCount the new experience amount
     */
    public void setExperienceCount(short experienceCount) {
        // Remove the entity in order to respawn it with the correct experience count
        Set<Player> viewerCache = new HashSet<>(getViewers());

        viewerCache.forEach(player -> removeViewer(player));

        this.experienceCount = experienceCount;

        viewerCache.forEach(player -> addViewer(player));
    }
}
