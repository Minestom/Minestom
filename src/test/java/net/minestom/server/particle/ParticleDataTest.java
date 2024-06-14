package net.minestom.server.particle;

import net.minestom.server.color.Color;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.data.BlockParticleData;
import net.minestom.server.particle.data.DustParticleData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParticleDataTest {

    @Test
    public void testDustParticleDefault() {
        Particle particle = Particle.DUST;
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> packet.write(new NetworkBuffer()));
    }

    @Test
    public void testDustParticleInvalid() {
        var particle = Particle.DUST.withData(null);
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> packet.write(new NetworkBuffer()));
    }

    @Test
    public void testDustParticleWrongData() {
        var particle = Particle.DUST.withData(new BlockParticleData(Block.STONE));
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> packet.write(new NetworkBuffer()));
    }

    @Test
    public void testDustParticleWrongParameters() {
        assertThrows(IllegalArgumentException.class, () -> Particle.DUST.withData(new DustParticleData(new Color(255, 255, 255), 0)));
    }

    @Test
    public void testParticleValid() {
        var particle = Particle.AMBIENT_ENTITY_EFFECT;
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> packet.write(new NetworkBuffer()));
    }

    @Test
    public void testParticleData() {
        var particle = Particle.AMBIENT_ENTITY_EFFECT;
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> packet.write(new NetworkBuffer()));
    }

    @Test
    public void invalidBlock() {
        var particle = Particle.BLOCK.withData(new BlockParticleData(null));
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThrows(NullPointerException.class, () -> packet.write(new NetworkBuffer()));
    }
}
