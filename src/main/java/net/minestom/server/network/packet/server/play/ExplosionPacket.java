package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ExplosionPacket(double x, double y, double z, float radius,
                              byte @NotNull [] records,
                              float playerMotionX, float playerMotionY, float playerMotionZ,
                              @NotNull BlockInteraction blockInteraction,
                              int smallParticleId, byte @NotNull [] smallParticleData,
                              int largeParticleId, byte @NotNull [] largeParticleData,
                              @NotNull SoundEvent sound) implements ServerPacket.Play {
    public static final SoundEvent DEFAULT_SOUND = SoundEvent.ENTITY_GENERIC_EXPLODE;

    public static final NetworkBuffer.Type<ExplosionPacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ExplosionPacket value) {
            buffer.write(DOUBLE, value.x);
            buffer.write(DOUBLE, value.y);
            buffer.write(DOUBLE, value.z);
            buffer.write(FLOAT, value.radius);
            buffer.write(VAR_INT, value.records.length / 3); // each record is 3 bytes long
            buffer.write(RAW_BYTES, value.records);
            buffer.write(FLOAT, value.playerMotionX);
            buffer.write(FLOAT, value.playerMotionY);
            buffer.write(FLOAT, value.playerMotionZ);
            buffer.write(VAR_INT, value.blockInteraction.ordinal());
            buffer.write(VAR_INT, value.smallParticleId);
            buffer.write(RAW_BYTES, value.smallParticleData);
            buffer.write(VAR_INT, value.largeParticleId);
            buffer.write(RAW_BYTES, value.largeParticleData);
            buffer.write(SoundEvent.NETWORK_TYPE, value.sound);
        }

        @Override
        public ExplosionPacket read(@NotNull NetworkBuffer buffer) {
            double x = buffer.read(DOUBLE), y = buffer.read(DOUBLE), z = buffer.read(DOUBLE);
            float radius = buffer.read(FLOAT);
            byte[] records = buffer.readBytes(buffer.read(VAR_INT) * 3);
            float playerMotionX = buffer.read(FLOAT), playerMotionY = buffer.read(FLOAT), playerMotionZ = buffer.read(FLOAT);
            BlockInteraction blockInteraction = buffer.readEnum(BlockInteraction.class);
            int smallParticleId = buffer.read(VAR_INT);
            byte[] smallParticleData = readParticleData(buffer, Particle.fromId(smallParticleId));
            int largeParticleId = buffer.read(VAR_INT);
            byte[] largeParticleData = readParticleData(buffer, Particle.fromId(largeParticleId));
            SoundEvent sound = buffer.read(SoundEvent.NETWORK_TYPE);
            return new ExplosionPacket(x, y, z, radius, records, playerMotionX, playerMotionY, playerMotionZ,
                    blockInteraction, smallParticleId, smallParticleData, largeParticleId, largeParticleData,
                    sound);
        }
    };

    private static byte @NotNull [] readParticleData(@NotNull NetworkBuffer reader, Particle particle) {
        return NetworkBuffer.makeArray(buffer -> {
            //Need to do this because particle data isn't at the end of the packet
            if (particle.equals(Particle.BLOCK) || particle.equals(Particle.BLOCK_MARKER) || particle.equals(Particle.FALLING_DUST) || particle.equals(Particle.SHRIEK)) {
                buffer.write(VAR_INT, reader.read(VAR_INT));
            } else if (particle.equals(Particle.VIBRATION)) {
                buffer.write(VAR_INT, reader.read(VAR_INT));
                buffer.write(BLOCK_POSITION, reader.read(BLOCK_POSITION));
                buffer.write(VAR_INT, reader.read(VAR_INT));
                buffer.write(FLOAT, reader.read(FLOAT));
                buffer.write(VAR_INT, reader.read(VAR_INT));
            } else if (particle.equals(Particle.SCULK_CHARGE)) {
                buffer.write(FLOAT, reader.read(FLOAT));
            } else if (particle.equals(Particle.ITEM)) {
                buffer.write(ItemStack.NETWORK_TYPE, reader.read(ItemStack.NETWORK_TYPE));
            } else if (particle.equals(Particle.DUST_COLOR_TRANSITION)) {
                for (int i = 0; i < 7; i++) buffer.write(FLOAT, reader.read(FLOAT));
            } else if (particle.equals(Particle.DUST)) {
                for (int i = 0; i < 4; i++) buffer.write(FLOAT, reader.read(FLOAT));
            }
        });
    }

    public ExplosionPacket(double x, double y, double z, float radius, byte @NotNull [] records,
                           float playerMotionX, float playerMotionY, float playerMotionZ) {
        this(x, y, z, radius, records, playerMotionX, playerMotionY, playerMotionZ,
                BlockInteraction.DESTROY, Particle.EXPLOSION.id(), new byte[]{},
                Particle.EXPLOSION_EMITTER.id(), new byte[]{},
                DEFAULT_SOUND);
    }

    public enum BlockInteraction {
        KEEP, DESTROY, DESTROY_WITH_DECAY, TRIGGER_BLOCK
    }
}
