package net.minestom.server.particle.data;

import net.minestom.server.instance.block.BlockState;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockParticleEffect extends ParticleEffect {

    private final short blockstateID;

    public BlockParticleEffect(short blockstateID) {
        this.blockstateID = blockstateID;
    }

    public BlockParticleEffect(@NotNull BlockState state) {
        this(state.getId());
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(blockstateID);
    }

    @Override
    public @Nullable BlockParticleEffect read(@Nullable String data) {
        if (data == null) return null;

        //TODO better block state parsing, also required for ArgumentBlockState
        return new BlockParticleEffect(Registry.BLOCK_REGISTRY.get(data).getDefaultBlockStateId());
    }
}
