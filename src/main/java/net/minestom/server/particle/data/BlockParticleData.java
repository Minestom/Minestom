package net.minestom.server.particle.data;

import net.minestom.server.instance.block.BlockState;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BlockParticleData extends ParticleData {
    private final short blockstateID;

    public BlockParticleData(@NotNull Particle particle, short blockstateID) {
        super(particle);
        this.blockstateID = blockstateID;
    }

    public BlockParticleData(@NotNull Particle particle, @NotNull BlockState state) {
        this(particle, state.getId());
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(blockstateID);
    }
}
