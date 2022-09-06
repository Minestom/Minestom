package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.entity.metadata.other.ExperienceOrbMeta;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.packet.server.play.SpawnExperienceOrbPacket;
import net.minestom.server.network.packet.server.play.SpawnPlayerPacket;

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
            return new SpawnPlayerPacket(entity.getEntityId(), entity.getUuid(), entity.getPosition());
        }
    },
    EXPERIENCE_ORB {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            final short expCount = (short) (entity.getEntityMeta() instanceof ExperienceOrbMeta experienceOrbMeta ?
                    experienceOrbMeta.getCount() : 0);
            return new SpawnExperienceOrbPacket(entity.getEntityId(), entity.getPosition(), expCount);
        }
    },
    PAINTING {
        @Override
        public ServerPacket getSpawnPacket(Entity entity) {
            return EntitySpawnType.basicEntity(entity);
           /* int motive = 0;
            Point position = Vec.ZERO;
            byte direction = 0;
            if (entity.getEntityMeta() instanceof PaintingMeta paintingMeta) {
                motive = paintingMeta.getMotive().ordinal();
                position = new Vec(
                        Math.max(0, (paintingMeta.getMotive().getWidth() >> 1) - 1),
                        paintingMeta.getMotive().getHeight() >> 1,
                        0
                );
                direction = switch (paintingMeta.getDirection()) {
                    case SOUTH -> 0;
                    case WEST -> 1;
                    case NORTH -> 2;
                    case EAST -> 3;
                    default -> 0;
                };
            }
            return new SpawnPaintingPacket(entity.getEntityId(), entity.getUuid(), motive, position, direction);*/
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
