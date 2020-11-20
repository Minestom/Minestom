package net.minestom.server.network.netty.packet;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet which is already framed.
 * Can be used if you want to send the exact same buffer to multiple clients without processing it more than once.
 */
public class FramedPacket {

    public final ByteBuf body;
    public boolean releaseBuf = false;

    public FramedPacket(@NotNull ByteBuf body) {
        this.body = body;
    }

    public FramedPacket(@NotNull ByteBuf body, boolean releaseBuf) {
        this.body = body;
        this.releaseBuf = releaseBuf;
    }

}
