package net.minestom.server.particle;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class DustPillarParticle extends ParticleImpl {
    private final @NotNull Block block;

    DustPillarParticle(@NotNull NamespaceID namespace, int id, @NotNull Block block) {
        super(namespace, id);
        this.block = block;
    }

    @Contract(pure = true)
    public @NotNull DustPillarParticle withBlock(@NotNull Block block) {
        return new DustPillarParticle(namespace(), id(), block);
    }

    public @NotNull Block block() {
        return block;
    }

    @Override
    public @NotNull DustPillarParticle readData(@NotNull NetworkBuffer reader) {
        short blockState = reader.read(NetworkBuffer.VAR_INT).shortValue();
        Block block = Block.fromStateId(blockState);
        Check.stateCondition(block == null, "Block state " + blockState + " is invalid");
        return this.withBlock(block);
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, (int) block.stateId());
    }
}
