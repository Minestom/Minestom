package net.minestom.server.entity;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public abstract class ObjectEntity extends Entity {

    public ObjectEntity(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        setGravity(0.02f, 0.04f, 1.96f);
    }

    /**
     * Gets the data of this object entity.
     *
     * @return an object data
     * @see <a href="https://wiki.vg/Object_Data">here</a>
     */
    public abstract int getObjectData();

    @Override
    public void update(long time) {

    }

    @Override
    public void spawn() {

    }

    @Override
    public ServerPacket getSpawnPacket() {
        SpawnEntityPacket spawnEntityPacket = new SpawnEntityPacket();
        spawnEntityPacket.entityId = getEntityId();
        spawnEntityPacket.uuid = getUuid();
        spawnEntityPacket.type = getEntityType().getId();
        spawnEntityPacket.position = getPosition();
        spawnEntityPacket.data = getObjectData();
        return spawnEntityPacket;
    }

}
