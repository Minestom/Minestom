package net.minestom.server.utils;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.AdventureSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.netty.packet.FramedPacket;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.callback.validator.PlayerValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.zip.DataFormatException;

/**
 * Utils class for packets. Including writing a {@link ServerPacket} into a {@link ByteBuf}
 * for network processing.
 */
public final class PacketUtils {

    private static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();
    private static final ThreadLocal<VelocityCompressor> COMPRESSOR = ThreadLocal.withInitial(() -> Natives.compress.get().create(4));

    private PacketUtils() {

    }

    /**
     * Sends a {@link ServerPacket} to multiple players.
     * <p>
     * Can drastically improve performance since the packet will not have to be processed as much.
     *
     * @param players         the players to send the packet to
     * @param packet          the packet to send to the players
     * @param playerValidator optional callback to check if a specify player of {@code players} should receive the packet
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet,
                                         @Nullable PlayerValidator playerValidator) {
        if (players.isEmpty())
            return;

        // work out if the packet needs to be sent individually due to server-side translating
        boolean needsTranslating = false;

        if (AdventureSerializer.AUTOMATIC_COMPONENT_TRANSLATION && packet instanceof ComponentHoldingServerPacket) {
            needsTranslating = AdventureSerializer.areAnyTranslatable(((ComponentHoldingServerPacket) packet).components());
        }

        if (MinecraftServer.hasGroupedPacket() && !needsTranslating) {
            // Send grouped packet...
            final boolean success = PACKET_LISTENER_MANAGER.processServerPacket(packet, players);
            if (success) {
                final ByteBuf finalBuffer = createFramedPacket(packet, true);
                final FramedPacket framedPacket = new FramedPacket(finalBuffer);

                // Send packet to all players
                for (Player player : players) {

                    if (!player.isOnline())
                        continue;

                    // Verify if the player should receive the packet
                    if (playerValidator != null && !playerValidator.isValid(player))
                        continue;

                    finalBuffer.retain();

                    final PlayerConnection playerConnection = player.getPlayerConnection();
                    if (playerConnection instanceof NettyPlayerConnection) {
                        final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) playerConnection;
                        nettyPlayerConnection.write(framedPacket, true);
                    } else {
                        playerConnection.sendPacket(packet);
                    }

                    finalBuffer.release();
                }
                finalBuffer.release(); // Release last reference
            }
        } else {
            // Write the same packet for each individual players
            for (Player player : players) {

                // Verify if the player should receive the packet
                if (playerValidator != null && !playerValidator.isValid(player))
                    continue;

                final PlayerConnection playerConnection = player.getPlayerConnection();
                playerConnection.sendPacket(packet, false);
            }
        }
    }

    /**
     * Same as {@link #sendGroupedPacket(Collection, ServerPacket, PlayerValidator)}
     * but with the player validator sets to null.
     *
     * @see #sendGroupedPacket(Collection, ServerPacket, PlayerValidator)
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet) {
        sendGroupedPacket(players, packet, null);
    }

    /**
     * Writes a {@link ServerPacket} into a {@link ByteBuf}.
     *
     * @param buf    the recipient of {@code packet}
     * @param packet the packet to write into {@code buf}
     */
    public static void writePacket(@NotNull ByteBuf buf, @NotNull ServerPacket packet) {
        Utils.writeVarIntBuf(buf, packet.getId());
        writePacketPayload(buf, packet);
    }

    /**
     * Writes a packet payload.
     *
     * @param packet the packet to write
     */
    private static void writePacketPayload(@NotNull ByteBuf buffer, @NotNull ServerPacket packet) {
        BinaryWriter writer = new BinaryWriter(buffer);
        try {
            packet.write(writer);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    /**
     * Frames a buffer for it to be understood by a Minecraft client.
     * <p>
     * The content of {@code packetBuffer} can be either a compressed or uncompressed packet buffer,
     * it depends of it the client did receive a {@link net.minestom.server.network.packet.server.login.SetCompressionPacket} packet before.
     *
     * @param packetBuffer the buffer containing compressed or uncompressed packet data
     * @param frameTarget  the buffer which will receive the framed version of {@code from}
     */
    public static void frameBuffer(@NotNull ByteBuf packetBuffer, @NotNull ByteBuf frameTarget) {
        final int packetSize = packetBuffer.readableBytes();
        final int headerSize = Utils.getVarIntSize(packetSize);

        if (headerSize > 3) {
            throw new IllegalStateException("Unable to fit " + headerSize + " into 3");
        }

        frameTarget.ensureWritable(packetSize + headerSize);

        Utils.writeVarIntBuf(frameTarget, packetSize);
        frameTarget.writeBytes(packetBuffer, packetBuffer.readerIndex(), packetSize);
    }

    /**
     * Compress using zlib the content of a packet.
     * <p>
     * {@code packetBuffer} needs to be the packet content without any header (if you want to use it to write a Minecraft packet).
     *
     * @param compressor        the deflater for zlib compression
     * @param packetBuffer      the buffer containing all the packet fields
     * @param compressionTarget the buffer which will receive the compressed version of {@code packetBuffer}
     */
    public static void compressBuffer(@NotNull VelocityCompressor compressor, @NotNull ByteBuf packetBuffer, @NotNull ByteBuf compressionTarget) {
        final int packetLength = packetBuffer.readableBytes();
        final boolean compression = packetLength > MinecraftServer.getCompressionThreshold();
        Utils.writeVarIntBuf(compressionTarget, compression ? packetLength : 0);
        if (compression) {
            compress(compressor, packetBuffer, compressionTarget);
        } else {
            compressionTarget.writeBytes(packetBuffer);
        }
    }

    private static void compress(@NotNull VelocityCompressor compressor, @NotNull ByteBuf uncompressed, @NotNull ByteBuf compressed) {
        try {
            compressor.deflate(uncompressed, compressed);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
    }

    public static void writeFramedPacket(@NotNull ByteBuf buffer,
                                         @NotNull ServerPacket serverPacket) {
        final int compressionThreshold = MinecraftServer.getCompressionThreshold();
        final boolean compression = compressionThreshold > 0;

        if (compression) {
            // Dummy varint
            final int packetLengthIndex = Utils.writeEmptyVarIntHeader(buffer);
            final int dataLengthIndex = Utils.writeEmptyVarIntHeader(buffer);

            // Write packet
            final int contentIndex = buffer.writerIndex();
            writePacket(buffer, serverPacket);
            final int afterIndex = buffer.writerIndex();
            final int packetSize = (afterIndex - dataLengthIndex) - Utils.VARINT_HEADER_SIZE;

            if (packetSize >= compressionThreshold) {
                // Packet large enough

                final VelocityCompressor compressor = COMPRESSOR.get();
                // Compress id + payload
                ByteBuf uncompressedCopy = buffer.copy(contentIndex, packetSize);
                buffer.writerIndex(contentIndex);
                compress(compressor, uncompressedCopy, buffer);
                uncompressedCopy.release();

                final int totalPacketLength = buffer.writerIndex() - contentIndex + Utils.VARINT_HEADER_SIZE;

                // Update header values
                Utils.overrideVarIntHeader(buffer, packetLengthIndex, totalPacketLength);
                Utils.overrideVarIntHeader(buffer, dataLengthIndex, packetSize);
            } else {
                // Packet too small, just override header values
                final int totalPacketLength = packetSize + Utils.VARINT_HEADER_SIZE;
                Utils.overrideVarIntHeader(buffer, packetLengthIndex, totalPacketLength);
                Utils.overrideVarIntHeader(buffer, dataLengthIndex, 0); // -> Uncompressed
            }
        } else {
            // No compression

            // Write dummy varint
            final int index = Utils.writeEmptyVarIntHeader(buffer);

            // Write packet id + payload
            writePacket(buffer, serverPacket);

            // Rewrite dummy varint to packet length
            final int afterIndex = buffer.writerIndex();
            final int packetSize = (afterIndex - index) - Utils.VARINT_HEADER_SIZE;
            Utils.overrideVarIntHeader(buffer, index, packetSize);
        }
    }

    /**
     * Creates a "framed packet" (packet which can be send and understood by a Minecraft client)
     * from a server packet, directly into an output buffer.
     * <p>
     * Can be used if you want to store a raw buffer and send it later without the additional writing cost.
     * Compression is applied if {@link MinecraftServer#getCompressionThreshold()} is greater than 0.
     *
     * @param serverPacket the server packet to write
     */
    @NotNull
    public static ByteBuf createFramedPacket(@NotNull ServerPacket serverPacket, boolean directBuffer) {
        ByteBuf packetBuf = directBuffer ? BufUtils.getBuffer(true) : Unpooled.buffer();
        writeFramedPacket(packetBuf, serverPacket);
        return packetBuf;
    }

}
