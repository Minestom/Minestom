package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.SignTextSlot;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;

public record OpenSignEditorPacket(Point position, SignTextSlot slot) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<OpenSignEditorPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, OpenSignEditorPacket::position,
            SignTextSlot.NETWORK_TYPE, OpenSignEditorPacket::slot,
            OpenSignEditorPacket::new);
}
