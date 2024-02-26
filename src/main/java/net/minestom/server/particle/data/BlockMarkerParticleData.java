package net.minestom.server.particle.data;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public record BlockMarkerParticleData(@NotNull Block block) implements ParticleData {
    BlockMarkerParticleData(NetworkBuffer reader) {
        this(read(reader));
    }

    private static Block read(NetworkBuffer reader) {
        Block block = Block.fromStateId(reader.read(NetworkBuffer.VAR_INT).shortValue());
        if (block == null) return Block.STONE;
        return block;
    }

    BlockMarkerParticleData() {
        this(Block.STONE);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.BLOCK_MARKER.id();
    }
}
