package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.data.VecDelta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityMovementPacketTest {

    @Test
    public void rotationPacketWireLayout() {
        var packet = new EntityRotationPacket(7, 90f, -45f, true);
        var buffer = NetworkBuffer.resizableBuffer();
        EntityRotationPacket.SERIALIZER.write(buffer, packet);

        // The ground flag precedes the angles.
        assertEquals(7, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(true, buffer.read(NetworkBuffer.BOOLEAN));
        assertEquals((byte) 64, buffer.read(NetworkBuffer.BYTE));
        assertEquals((byte) -32, buffer.read(NetworkBuffer.BYTE));
        assertEquals(0L, buffer.readableBytes());
    }

    @Test
    public void positionPacketLinearWireLayout() {
        var packet = new EntityPositionPacket(7, new VecDelta.Linear((short) 1, (short) -2, (short) 3), true);
        var buffer = NetworkBuffer.resizableBuffer();
        EntityPositionPacket.SERIALIZER.write(buffer, packet);

        // The ground bit is the low bit of the properties, a linear delta has a step count of zero.
        assertEquals(7, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(1, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals((short) 1, buffer.read(NetworkBuffer.SHORT));
        assertEquals((short) -2, buffer.read(NetworkBuffer.SHORT));
        assertEquals((short) 3, buffer.read(NetworkBuffer.SHORT));
        assertEquals(0L, buffer.readableBytes());
    }

    @Test
    public void positionPacketSteppedWireLayout() {
        var delta = new VecDelta.Stepped(List.of(
                new VecDelta.Step(0, (short) 1, (short) 2, (short) 3),
                new VecDelta.Step(4, (short) 5, (short) 6, (short) 7)));
        var packet = new EntityPositionPacket(7, delta, false);
        var buffer = NetworkBuffer.resizableBuffer();
        EntityPositionPacket.SERIALIZER.write(buffer, packet);

        // Two steps off the ground pack as 2 << 1.
        assertEquals(7, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(4, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(0, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals((short) 1, buffer.read(NetworkBuffer.SHORT));
        assertEquals((short) 2, buffer.read(NetworkBuffer.SHORT));
        assertEquals((short) 3, buffer.read(NetworkBuffer.SHORT));
        assertEquals(4, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals((short) 5, buffer.read(NetworkBuffer.SHORT));
        assertEquals((short) 6, buffer.read(NetworkBuffer.SHORT));
        assertEquals((short) 7, buffer.read(NetworkBuffer.SHORT));
        assertEquals(0L, buffer.readableBytes());
    }

    @Test
    public void positionAndRotationPacketWireLayout() {
        var delta = new VecDelta.Stepped(List.of(new VecDelta.Step(2, (short) 1, (short) 2, (short) 3)));
        var packet = new EntityPositionAndRotationPacket(7, delta, 90f, -45f, true);
        var buffer = NetworkBuffer.resizableBuffer();
        EntityPositionAndRotationPacket.SERIALIZER.write(buffer, packet);

        // One step on the ground packs as 1 << 1 | 1, the angles trail the delta.
        assertEquals(7, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(3, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals(2, buffer.read(NetworkBuffer.VAR_INT));
        assertEquals((short) 1, buffer.read(NetworkBuffer.SHORT));
        assertEquals((short) 2, buffer.read(NetworkBuffer.SHORT));
        assertEquals((short) 3, buffer.read(NetworkBuffer.SHORT));
        assertEquals((byte) 64, buffer.read(NetworkBuffer.BYTE));
        assertEquals((byte) -32, buffer.read(NetworkBuffer.BYTE));
        assertEquals(0L, buffer.readableBytes());
    }
}
