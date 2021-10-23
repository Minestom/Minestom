package net.minestom.server.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.entity.metadata.other.ExperienceOrbMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;

public enum EntitySpawnType {
    BASE {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            SpawnEntityPacket packet = new SpawnEntityPacket();
            packet.entityId = entity.getEntityId();
            packet.uuid = entity.getUuid();
            packet.type = entity.getEntityType().id();
            packet.position = entity.getPosition();
            if (entity.getEntityMeta() instanceof ObjectDataProvider objectDataProvider) {
                packet.data = objectDataProvider.getObjectData();
                if (objectDataProvider.requiresVelocityPacketAtSpawn()) {
                    final var velocity = entity.getVelocityForPacket();
                    packet.velocityX = (short) velocity.x();
                    packet.velocityY = (short) velocity.y();
                    packet.velocityZ = (short) velocity.z();
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
            packet.entityType = entity.getEntityType().id();
            packet.position = entity.getPosition();
            packet.headPitch = entity.getPosition().pitch();
            final var velocity = entity.getVelocityForPacket();
            packet.velocityX = (short) velocity.x();
            packet.velocityY = (short) velocity.y();
            packet.velocityZ = (short) velocity.z();
            return packet;
        }
    },
    PLAYER {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            return new SpawnPlayerPacket(entity.getEntityId(), entity.getUuid(), entity.getPosition());
        }
    },
    EXPERIENCE_ORB {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            SpawnExperienceOrbPacket packet = new SpawnExperienceOrbPacket();
            packet.entityId = entity.getEntityId();
            packet.position = entity.getPosition();
            if (entity.getEntityMeta() instanceof ExperienceOrbMeta experienceOrbMeta) {
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
            if (entity.getEntityMeta() instanceof PaintingMeta paintingMeta) {
                packet.motive = paintingMeta.getMotive().ordinal();
                packet.position = new Vec(
                        Math.max(0, (paintingMeta.getMotive().getWidth() >> 1) - 1),
                        paintingMeta.getMotive().getHeight() >> 1,
                        0
                );
                switch (paintingMeta.getDirection()) {
                    case SOUTH -> packet.direction = 0;
                    case WEST -> packet.direction = 1;
                    case NORTH -> packet.direction = 2;
                    case EAST -> packet.direction = 3;
                }
            } else {
                packet.position = Vec.ZERO;
            }
            return packet;
        }
    };

    public abstract ServerPacket getSpawnPacket(Entity entity);

}
