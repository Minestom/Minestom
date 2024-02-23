package net.minestom.server.particle.data;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record BlockParticleData(Block block) implements ParticleData {
    BlockParticleData(NetworkBuffer reader) {
        this(Block.fromStateId(reader.read(NetworkBuffer.VAR_INT).shortValue()));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
    }
}
