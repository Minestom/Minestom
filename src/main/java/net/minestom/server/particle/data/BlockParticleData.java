package net.minestom.server.particle.data;

import net.minestom.server.utils.binary.BinaryWriter;

public class BlockParticleData extends ParticleData {
    private final short blockstateID;

    public BlockParticleData(short blockstateID) {
        this.blockstateID = blockstateID;
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(blockstateID);
    }
}
