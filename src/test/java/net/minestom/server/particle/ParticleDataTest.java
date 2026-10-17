package net.minestom.server.particle;

import com.google.gson.JsonParser;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import org.junit.jupiter.api.Test;

import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParticleDataTest {

    @Test
    public void testDustParticleDefault() {
        Particle particle = Particle.DUST;
        ParticlePacket packet = new ParticlePacket(particle, false, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void testDustParticleInvalid() {
        var particle = Particle.DUST.withProperties(null, 1);
        ParticlePacket packet = new ParticlePacket(particle, false, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThrows(NullPointerException.class, () -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void testParticleValid() {
        var particle = Particle.ENTITY_EFFECT;
        ParticlePacket packet = new ParticlePacket(particle, false, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void testParticleData() {
        var particle = Particle.ENTITY_EFFECT;
        ParticlePacket packet = new ParticlePacket(particle, false, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void invalidBlock() {
        var particle = Particle.BLOCK.withBlock(null);
        ParticlePacket packet = new ParticlePacket(particle, false, true, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThrows(NullPointerException.class, () -> ParticlePacket.SERIALIZER.write(NetworkBuffer.resizableBuffer(), packet));
    }

    @Test
    public void particlePacketWireLayout() {
        var packet = new ParticlePacket(Particle.FLAME, false, true, 1, 2, 3,
                0.5f, 0.25f, 0.125f, 0.1f, 0.2f, 0.3f,
                300, ParticlePacket.Randomization.ALTERNATIVE_WITH_SPEED);
        var buffer = NetworkBuffer.resizableBuffer();
        ParticlePacket.SERIALIZER.write(buffer, packet);

        // The particle leads, the count is a VarInt and the randomization trails.
        assertEquals(Particle.FLAME.id(), buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(false, buffer.read(NetworkBuffer.BOOLEAN));
        assertEquals(true, buffer.read(NetworkBuffer.BOOLEAN));
        assertEquals(1, buffer.read(NetworkBuffer.DOUBLE));
        assertEquals(2, buffer.read(NetworkBuffer.DOUBLE));
        assertEquals(3, buffer.read(NetworkBuffer.DOUBLE));
        assertEquals(0.5f, buffer.read(NetworkBuffer.FLOAT));
        assertEquals(0.25f, buffer.read(NetworkBuffer.FLOAT));
        assertEquals(0.125f, buffer.read(NetworkBuffer.FLOAT));
        assertEquals(0.1f, buffer.read(NetworkBuffer.FLOAT));
        assertEquals(0.2f, buffer.read(NetworkBuffer.FLOAT));
        assertEquals(0.3f, buffer.read(NetworkBuffer.FLOAT));
        assertEquals(300, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(2, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(0L, buffer.readableBytes());
    }

    @Test
    public void blockParticleStateForms() {
        var expected = Particle.BLOCK.withBlock(Block.OAK_STAIRS);
        var mapJson = JsonParser.parseString("{\"type\":\"minecraft:block\",\"block_state\":{\"id\":\"minecraft:oak_stairs\"}}");
        var nameJson = JsonParser.parseString("{\"type\":\"minecraft:block\",\"block_state\":\"minecraft:oak_stairs\"}");
        assertEquals(expected, assertOk(Particle.CODEC.decode(Transcoder.JSON, mapJson)));
        assertEquals(expected, assertOk(Particle.CODEC.decode(Transcoder.JSON, nameJson)));

        var written = assertOk(Particle.CODEC.encode(Transcoder.JSON, expected));
        assertEquals(JsonParser.parseString("{\"type\":\"minecraft:block\",\"block_state\":{\"id\":\"oak_stairs\"}}"), written);

        var withProperties = Particle.FALLING_DUST.withBlock(Block.OAK_STAIRS.withProperty("facing", "south"));
        var propertiesJson = JsonParser.parseString("{\"type\":\"minecraft:falling_dust\",\"block_state\":{\"id\":\"minecraft:oak_stairs\",\"properties\":{\"facing\":\"south\"}}}");
        assertEquals(withProperties, assertOk(Particle.CODEC.decode(Transcoder.JSON, propertiesJson)));
        assertEquals(withProperties, assertOk(Particle.CODEC.decode(Transcoder.JSON, assertOk(Particle.CODEC.encode(Transcoder.JSON, withProperties)))));
    }
}
