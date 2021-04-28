package net.minestom.server.particle.data;

import net.minestom.server.instance.block.BlockState;
import net.minestom.server.particle.ParticleType;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class BlockParticle extends Particle {
    public static final BiFunction<ParticleType<BlockParticle>, @Nullable String, BlockParticle> READER = (particle, data) -> {
        if (data == null) return null;

        //TODO better block state parsing, also required for ArgumentBlockState
        return new BlockParticle(particle, Registry.BLOCK_REGISTRY.get(data).getDefaultBlockStateId());
    };

    private final short blockstateID;

    public BlockParticle(@NotNull ParticleType<BlockParticle> particleType, short blockstateID) {
        super(particleType);
        this.blockstateID = blockstateID;
    }

    public BlockParticle(@NotNull ParticleType<BlockParticle> particleType, @NotNull BlockState state) {
        this(particleType, state.getId());
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(blockstateID);
    }
}
