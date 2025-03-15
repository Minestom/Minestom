package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;

// TODO(1.21.5): Remove this entirely its useless
public enum EntitySpawnType {
    BASE {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            return EntitySpawnType.basicEntity(entity);
        }
    },
    LIVING {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            return EntitySpawnType.basicEntity(entity);
        }
    },
    PLAYER {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            return EntitySpawnType.basicEntity(entity);
        }
    },
    EXPERIENCE_ORB {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            return EntitySpawnType.basicEntity(entity);
        }
    },
    PAINTING {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            return EntitySpawnType.basicEntity(entity);
        }
    };

    public abstract ServerPacket getSpawnPacket(Entity entity);

    private static SpawnEntityPacket basicEntity(Entity entity) {
        int data = 0;
        short velocityX = 0, velocityZ = 0, velocityY = 0;
        if (entity.getEntityMeta() instanceof ObjectDataProvider objectDataProvider) {
            data = objectDataProvider.getObjectData();
            if (objectDataProvider.requiresVelocityPacketAtSpawn()) {
                final var velocity = entity.getVelocityForPacket();
                velocityX = (short) velocity.x();
                velocityY = (short) velocity.y();
                velocityZ = (short) velocity.z();
            }
        }
        final Pos position = entity.getPosition();
        return new SpawnEntityPacket(entity.getEntityId(), entity.getUuid(), entity.getEntityType().id(),
                position, position.yaw(), data, velocityX, velocityY, velocityZ);
    }
}
