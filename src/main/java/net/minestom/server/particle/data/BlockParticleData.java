package net.minestom.server.particle.data;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record BlockParticleData(Block block) implements ParticleData {
    BlockParticleData(NetworkBuffer reader) {
        this(read(reader));
    }

    BlockParticleData() {
        this(Block.STONE);
    }

    private static Block read(NetworkBuffer reader) {
        short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
        Block block = Block.fromStateId(blockState);
        Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
        return block;
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
    }

    @Override
    public boolean validate(int particleId) {
        return particleId == Particle.BLOCK.id();
    }
}
