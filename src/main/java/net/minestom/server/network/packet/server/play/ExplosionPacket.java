package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ExplosionPacket(double x, double y, double z, float radius,
                              byte @NotNull [] records,
                              float playerMotionX, float playerMotionY, float playerMotionZ,
                              @NotNull BlockInteraction blockInteraction,
                              int smallParticleId, byte @NotNull [] smallParticleData,
                              int largeParticleId, byte @NotNull [] largeParticleData,
                              @NotNull String soundName, boolean hasFixedSoundRange, float soundRange) implements ServerPacket {
    private static @NotNull ExplosionPacket fromReader(@NotNull NetworkBuffer reader) {
        double x = reader.read(DOUBLE), y = reader.read(DOUBLE), z = reader.read(DOUBLE);
        float radius = reader.read(FLOAT);
        byte[] records = reader.readBytes(reader.read(VAR_INT) * 3);
        float playerMotionX = reader.read(FLOAT), playerMotionY = reader.read(FLOAT), playerMotionZ = reader.read(FLOAT);
        BlockInteraction blockInteraction = BlockInteraction.values()[reader.read(VAR_INT)];
        int smallParticleId = reader.read(VAR_INT);
        byte[] smallParticleData = readParticleData(reader, Particle.fromId(smallParticleId));
        int largeParticleId = reader.read(VAR_INT);
        byte[] largeParticleData = readParticleData(reader, Particle.fromId(largeParticleId));
        String soundName = reader.read(STRING);
        boolean hasFixedSoundRange = reader.read(BOOLEAN);
        float soundRange = hasFixedSoundRange ? reader.read(FLOAT) : 0;
        return new ExplosionPacket(x, y, z, radius, records, playerMotionX, playerMotionY, playerMotionZ,
                blockInteraction, smallParticleId, smallParticleData, largeParticleId, largeParticleData,
                soundName, hasFixedSoundRange, soundRange);
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
            writer.writeItemStack(reader.read(ITEM));
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
                SoundEvent.ENTITY_GENERIC_EXPLODE.name(), false, 0);
    }

    private ExplosionPacket(@NotNull ExplosionPacket packet) {
        this(packet.x, packet.y, packet.z, packet.radius, packet.records, packet.playerMotionX, packet.playerMotionY, packet.playerMotionZ,
                packet.blockInteraction, packet.smallParticleId, packet.smallParticleData, packet.largeParticleId, packet.largeParticleData,
                packet.soundName, packet.hasFixedSoundRange, packet.soundRange);
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
        writer.write(STRING, soundName);
        writer.write(BOOLEAN, hasFixedSoundRange);
        if (hasFixedSoundRange) writer.write(FLOAT, soundRange);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.EXPLOSION;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }

    public enum BlockInteraction {
        KEEP, DESTROY, DESTROY_WITH_DECAY, TRIGGER_BLOCK
    }
}
