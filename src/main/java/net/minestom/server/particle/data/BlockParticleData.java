package net.minestom.server.particle.data;

import net.minestom.server.instance.block.BlockState;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class BlockParticleData extends ParticleData {
    public static final BiFunction<Particle<BlockParticleData>, String, BlockParticleData> READER = (particle, data) -> {
        //TODO better block state parsing, also required for ArgumentBlockState
        return new BlockParticleData(particle, Registry.BLOCK_REGISTRY.get(data).getDefaultBlockStateId());
    };

    private final short blockstateID;

    public BlockParticleData(@NotNull Particle<BlockParticleData> particle, short blockstateID) {
        super(particle);
        this.blockstateID = blockstateID;
    }

    public BlockParticleData(@NotNull Particle<BlockParticleData> particle, @NotNull BlockState state) {
        this(particle, state.getId());
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(blockstateID);
    }
}
