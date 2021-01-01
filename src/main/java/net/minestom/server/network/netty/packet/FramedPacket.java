package net.minestom.server.network.netty.packet;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet which is already framed. (packet id+payload) + optional compression
 * Can be used if you want to send the exact same buffer to multiple clients without processing it more than once.
 */
public class FramedPacket {

    public final ByteBuf body;

    public FramedPacket(@NotNull ByteBuf body) {
        this.body = body;
    }

}
