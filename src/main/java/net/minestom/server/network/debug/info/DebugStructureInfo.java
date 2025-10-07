package net.minestom.server.network.debug.info;

import net.minestom.server.collision.BlockBoundingBox;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;

public record DebugStructureInfo(BlockBoundingBox boundingBox, List<Piece> pieces) {

    public static final NetworkBuffer.Type<DebugStructureInfo> SERIALIZER = NetworkBufferTemplate.template(
            BlockBoundingBox.NETWORK_TYPE, DebugStructureInfo::boundingBox,
            Piece.SERIALIZER.list(), DebugStructureInfo::pieces,
            DebugStructureInfo::new);

    public DebugStructureInfo {
        pieces = List.copyOf(pieces);
    }

    public record Piece(BlockBoundingBox boundingBox, boolean isStart) {
        public static final NetworkBuffer.Type<Piece> SERIALIZER = NetworkBufferTemplate.template(
                BlockBoundingBox.NETWORK_TYPE, Piece::boundingBox,
                NetworkBuffer.BOOLEAN, Piece::isStart,
                Piece::new);
    }
}
