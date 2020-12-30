package net.minestom.server.entity;

import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.SpawnExperienceOrbPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExperienceOrb extends Entity {

    private short experienceCount;

    public ExperienceOrb(short experienceCount, @NotNull Position spawnPosition) {
        super(EntityType.EXPERIENCE_ORB, spawnPosition);
        setGravity(0.02f, 0.04f, 1.96f);
        setBoundingBox(0.5f, 0.5f, 0.5f);
        this.experienceCount = experienceCount;
    }

    public ExperienceOrb(short experienceCount, @NotNull Position spawnPosition, @Nullable Instance instance) {
        this(experienceCount, spawnPosition);

        if (instance != null) {
            setInstance(instance);
        }
    }

    @Override
    public void update(long time) {
        // TODO slide toward nearest player
    }

    @Override
    public void spawn() {

    }

    @Override
    public ServerPacket getSpawnPacket() {
        SpawnExperienceOrbPacket experienceOrbPacket = new SpawnExperienceOrbPacket();
        experienceOrbPacket.entityId = getEntityId();
        experienceOrbPacket.position = getPosition();
        experienceOrbPacket.expCount = experienceCount;
        return experienceOrbPacket;
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
}
