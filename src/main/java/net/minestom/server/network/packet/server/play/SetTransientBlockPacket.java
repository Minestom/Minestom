package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;

/**
 * Shows a block at the given position on the client without changing the world.
 * The client keeps the block until the next block update at that position, which is used to
 * hide the gap between a falling block landing and the chunk update reaching the client.
 *
 * @param blockPosition the position of the transient block
 * @param block         the block state to display
 */
public record SetTransientBlockPacket(Point blockPosition, Block block) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetTransientBlockPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, SetTransientBlockPacket::blockPosition,
            Block.STATE_NETWORK_TYPE, SetTransientBlockPacket::block,
            SetTransientBlockPacket::new);
}
