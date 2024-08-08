package net.minestom.server.particle;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParticleDataTest {

    @Test
    public void testDustParticleDefault() {
        Particle particle = Particle.DUST;
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void testDustParticleInvalid() {
        var particle = Particle.DUST.withProperties(null, 1);
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThrows(NullPointerException.class, () -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void testParticleValid() {
        var particle = Particle.ENTITY_EFFECT;
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void testParticleData() {
        var particle = Particle.ENTITY_EFFECT;
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void invalidBlock() {
        var particle = Particle.BLOCK.withBlock(null);
        ParticlePacket packet = new ParticlePacket(particle, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThrows(NullPointerException.class, () -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }
}
