package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.binary.BinaryWriter;
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

    private static @NotNull ExplosionPacket fromReader(@NotNull NetworkBuffer reader) {
        double x = reader.read(DOUBLE), y = reader.read(DOUBLE), z = reader.read(DOUBLE);
        float radius = reader.read(FLOAT);
        byte[] records = reader.readBytes(reader.read(VAR_INT) * 3);
        float playerMotionX = reader.read(FLOAT), playerMotionY = reader.read(FLOAT), playerMotionZ = reader.read(FLOAT);
        BlockInteraction blockInteraction = reader.readEnum(BlockInteraction.class);
        int smallParticleId = reader.read(VAR_INT);
        byte[] smallParticleData = readParticleData(reader, Particle.fromId(smallParticleId));
        int largeParticleId = reader.read(VAR_INT);
        byte[] largeParticleData = readParticleData(reader, Particle.fromId(largeParticleId));
        SoundEvent sound = reader.read(SoundEvent.NETWORK_TYPE);
        return new ExplosionPacket(x, y, z, radius, records, playerMotionX, playerMotionY, playerMotionZ,
                blockInteraction, smallParticleId, smallParticleData, largeParticleId, largeParticleData,
                sound);
    }

    private static byte @NotNull [] readParticleData(@NotNull NetworkBuffer reader, Particle particle) {
        //Need to do this because particle data isn't at the end of the packet
        BinaryWriter writer = new BinaryWriter();
        if (particle.equals(Particle.BLOCK) || particle.equals(Particle.BLOCK_MARKER) || particle.equals(Particle.FALLING_DUST) || particle.equals(Particle.SHRIEK)) {
            writer.writeVarInt(reader.read(VAR_INT));
        }
        else if (particle.equals(Particle.VIBRATION)) {
            writer.writeVarInt(reader.read(VAR_INT));
            writer.writeBlockPosition(reader.read(BLOCK_POSITION));
            writer.writeVarInt(reader.read(VAR_INT));
            writer.writeFloat(reader.read(FLOAT));
            writer.writeVarInt(reader.read(VAR_INT));
        }
        else if (particle.equals(Particle.SCULK_CHARGE)) {
            writer.writeFloat(reader.read(FLOAT));
            return writer.toByteArray();
        }
        else if (particle.equals(Particle.ITEM)) {
            writer.writeItemStack(reader.read(ItemStack.NETWORK_TYPE));
        }
        else if (particle.equals(Particle.DUST_COLOR_TRANSITION)) {
            for (int i = 0; i < 7; i++) writer.writeFloat(reader.read(FLOAT));
        }
        else if (particle.equals(Particle.DUST)) {
            for (int i = 0; i < 4; i++) writer.writeFloat(reader.read(FLOAT));
        }

        return writer.toByteArray();
    }

    public ExplosionPacket(@NotNull NetworkBuffer reader) {
        this(fromReader(reader));
    }

    public ExplosionPacket(double x, double y, double z, float radius, byte @NotNull [] records,
                           float playerMotionX, float playerMotionY, float playerMotionZ) {
        this(x, y, z, radius, records, playerMotionX, playerMotionY, playerMotionZ,
                BlockInteraction.DESTROY, Particle.EXPLOSION.id(), new byte[] {},
                Particle.EXPLOSION_EMITTER.id(), new byte[] {},
                DEFAULT_SOUND);
    }

    private ExplosionPacket(@NotNull ExplosionPacket packet) {
        this(packet.x, packet.y, packet.z, packet.radius, packet.records, packet.playerMotionX, packet.playerMotionY, packet.playerMotionZ,
                packet.blockInteraction, packet.smallParticleId, packet.smallParticleData, packet.largeParticleId, packet.largeParticleData,
                packet.sound);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(DOUBLE, x);
        writer.write(DOUBLE, y);
        writer.write(DOUBLE, z);
        writer.write(FLOAT, radius);
        writer.write(VAR_INT, records.length / 3); // each record is 3 bytes long
        writer.write(RAW_BYTES, records);
        writer.write(FLOAT, playerMotionX);
        writer.write(FLOAT, playerMotionY);
        writer.write(FLOAT, playerMotionZ);
        writer.write(VAR_INT, blockInteraction.ordinal());
        writer.write(VAR_INT, smallParticleId);
        writer.write(RAW_BYTES, smallParticleData);
        writer.write(VAR_INT, largeParticleId);
        writer.write(RAW_BYTES, largeParticleData);
        writer.write(SoundEvent.NETWORK_TYPE, sound);
    }

    public enum BlockInteraction {
        KEEP, DESTROY, DESTROY_WITH_DECAY, TRIGGER_BLOCK
    }
}
