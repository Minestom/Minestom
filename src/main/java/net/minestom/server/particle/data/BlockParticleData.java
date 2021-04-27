package net.minestom.server.particle.data;

import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;

public class BlockParticleData extends ParticleData {
    private final short blockstateID;

    public BlockParticleData(Particle particle, short blockstateID) {
        super(particle);
        this.blockstateID = blockstateID;
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(blockstateID);
    }
}
