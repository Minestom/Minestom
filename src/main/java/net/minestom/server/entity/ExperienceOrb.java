package net.minestom.server.entity;

import net.minestom.server.network.packet.server.play.SpawnExperienceOrbPacket;
import net.minestom.server.network.player.PlayerConnection;

public class ExperienceOrb extends Entity {

    private short experienceCount;

    public ExperienceOrb(short experienceCount) {
        super(23);
        setGravity(0.02f);
        setBoundingBox(0.5f, 0.5f, 0.5f);
        this.experienceCount = experienceCount;
    }

    @Override
    public void update() {
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
        return super.addViewer(player); // Add player to viewers list and send velocity packet
    }

    public short getExperienceCount() {
        return experienceCount;
    }

    public void setExperienceCount(short experienceCount) {
        this.experienceCount = experienceCount;
    }
}
