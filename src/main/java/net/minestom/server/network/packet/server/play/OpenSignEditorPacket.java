package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record OpenSignEditorPacket(@NotNull Point position, boolean isFrontText) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<OpenSignEditorPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, OpenSignEditorPacket::position,
            BOOLEAN, OpenSignEditorPacket::isFrontText,
            OpenSignEditorPacket::new);
}
