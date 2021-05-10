package net.minestom.server.entity;

import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.entity.metadata.other.ExperienceOrbMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Vector;

public enum EntitySpawnType {
    BASE {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            SpawnEntityPacket packet = new SpawnEntityPacket();
            packet.entityId = entity.getEntityId();
            packet.uuid = entity.getUuid();
            packet.type = entity.getEntityType().ordinal();
            packet.position = entity.getPosition();
            if (entity.getEntityMeta() instanceof ObjectDataProvider) {
                ObjectDataProvider objectDataProvider = (ObjectDataProvider) entity.getEntityMeta();
                packet.data = objectDataProvider.getObjectData();
                if (objectDataProvider.requiresVelocityPacketAtSpawn()) {
                    Vector velocity = entity.getVelocityForPacket();
                    packet.velocityX = (short) velocity.getX();
                    packet.velocityY = (short) velocity.getY();
                    packet.velocityZ = (short) velocity.getZ();
                }
            }
            return packet;
        }
    },
    LIVING {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            SpawnLivingEntityPacket packet = new SpawnLivingEntityPacket();
            packet.entityId = entity.getEntityId();
            packet.entityUuid = entity.getUuid();
            packet.entityType = entity.getEntityType().ordinal();
            packet.position = entity.getPosition();
            packet.headPitch = entity.getPosition().getPitch();
            Vector velocity = entity.getVelocityForPacket();
            packet.velocityX = (short) velocity.getX();
            packet.velocityY = (short) velocity.getY();
            packet.velocityZ = (short) velocity.getZ();
            return packet;
        }
    },
    PLAYER {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            SpawnPlayerPacket packet = new SpawnPlayerPacket();
            packet.entityId = entity.getEntityId();
            packet.playerUuid = entity.getUuid();
            packet.position = entity.getPosition();
            return packet;
        }
    },
    EXPERIENCE_ORB {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            SpawnExperienceOrbPacket packet = new SpawnExperienceOrbPacket();
            packet.entityId = entity.getEntityId();
            packet.position = entity.getPosition();
            if (entity.getEntityMeta() instanceof ExperienceOrbMeta) {
                ExperienceOrbMeta experienceOrbMeta = (ExperienceOrbMeta) entity.getEntityMeta();
                packet.expCount = (short) experienceOrbMeta.getCount();
            }
            return packet;
        }
    },
    PAINTING {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            SpawnPaintingPacket packet = new SpawnPaintingPacket();
            packet.entityId = entity.getEntityId();
            packet.entityUuid = entity.getUuid();
            if (entity.getEntityMeta() instanceof PaintingMeta) {
                PaintingMeta paintingMeta = (PaintingMeta) entity.getEntityMeta();
                packet.motive = paintingMeta.getMotive().ordinal();
                packet.position = new BlockPosition(
                        Math.max(0, (paintingMeta.getMotive().getWidth() >> 1) - 1),
                        paintingMeta.getMotive().getHeight() >> 1,
                        0
                );
                switch (paintingMeta.getDirection()) {
                    case SOUTH:
                        packet.direction = 0;
                        break;
                    case WEST:
                        packet.direction = 1;
                        break;
                    case NORTH:
                        packet.direction = 2;
                        break;
                    case EAST:
                        packet.direction = 3;
                        break;
                }
            } else {
                packet.position = new BlockPosition(0, 0, 0);
            }
            return packet;
        }
    };

    public abstract ServerPacket getSpawnPacket(Entity entity);
    
}
