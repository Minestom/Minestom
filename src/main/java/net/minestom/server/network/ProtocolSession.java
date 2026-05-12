package net.minestom.server.network;

import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.*;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.DataFormatException;

/**
 * Stateful vanilla packet reader/writer for users that want protocol handling without the full player framework.
 */
@ApiStatus.Experimental
public final class ProtocolSession {
    private final NetworkBuffer readBuffer;
    private final NetworkBuffer writeBuffer;
    private final Queue<SendablePacket> queue = new ConcurrentLinkedQueue<>();

    private @Nullable NetworkBuffer leftover;
    private @Nullable Cipher encryptCipher;
    private @Nullable Cipher decryptCipher;
    private int compressionThreshold;
    private ConnectionState clientState = ConnectionState.HANDSHAKE;
    private ConnectionState serverState = ConnectionState.HANDSHAKE;

    private ProtocolSession(Builder builder) {
        this.readBuffer = NetworkBuffer.resizableBuffer(builder.readBufferSize, builder.registries);
        this.writeBuffer = NetworkBuffer.resizableBuffer(builder.writeBufferSize, builder.registries);
        this.compressionThreshold = builder.compressionThreshold;
    }

    public static Builder builder(Registries registries) {
        return new Builder(registries);
    }

    public int readFrom(ReadableByteChannel channel) throws IOException {
        final long writeIndex = readBuffer.writeIndex();
        final int length = readBuffer.readChannel(channel);
        if (length > 0 && decryptCipher != null) {
            readBuffer.cipher(decryptCipher, writeIndex, length);
        }
        return length;
    }

    public PacketReading.Result<ClientPacket> readPackets(PacketParser<ClientPacket> parser) throws DataFormatException {
        final PacketReading.Result<ClientPacket> result = PacketReading.readPackets(
                readBuffer,
                parser,
                clientState,
                PacketVanilla::nextClientState,
                compressionThreshold > 0
        );
        switch (result) {
            case PacketReading.Result.Success<ClientPacket> success -> {
                for (PacketReading.ParsedPacket<ClientPacket> packet : success.packets()) {
                    clientState(packet.nextState());
                }
                readBuffer.compact();
            }
            case PacketReading.Result.Empty<ClientPacket> _ -> {
            }
            case PacketReading.Result.Failure<ClientPacket> failure -> {
                readBuffer.resize(failure.requiredCapacity());
            }
        }
        return result;
    }

    public void send(SendablePacket packet) {
        queue.add(packet);
    }

    public void sendAll(Iterable<? extends SendablePacket> packets) {
        for (SendablePacket packet : packets) queue.add(packet);
    }

    public boolean hasPendingWrite() {
        return leftover != null || !queue.isEmpty();
    }

    public boolean flushTo(SocketChannel channel) throws IOException {
        return flushTo(channel, PacketOperator.identity(), null);
    }

    public boolean flushTo(SocketChannel channel, PacketOperator packetOperator, @Nullable Runnable packetWritten)
            throws IOException {
        NetworkBuffer pending = leftover;
        if (pending != null) {
            if (!pending.writeChannel(channel)) return false;
            leftover = null;
        }

        if (queue.isEmpty()) return true;
        writeBuffer.clear();
        while (!queue.isEmpty()) {
            final long index = writeBuffer.writeIndex();
            final SendablePacket currentPacket = queue.peek();
            assert currentPacket != null;
            final SendablePacket packet = packetOperator.apply(serverState, currentPacket);
            if (packet == null) {
                queue.remove();
                if (packetWritten != null) packetWritten.run();
                continue;
            }
            try {
                writeSendable(writeBuffer, packet);
                queue.remove();
                if (packetWritten != null) packetWritten.run();
            } catch (IndexOutOfBoundsException exception) {
                writeBuffer.writeIndex(index);
                if (index == 0) {
                    writeBuffer.resize(Math.max(writeBuffer.capacity() * 2, writeBuffer.capacity() + 1));
                    continue;
                }
                break;
            }
        }

        if (writeBuffer.writeIndex() == 0) return queue.isEmpty();
        if (encryptCipher != null) {
            writeBuffer.cipher(encryptCipher, 0, writeBuffer.writeIndex());
        }
        if (!writeBuffer.writeChannel(channel)) {
            leftover = writeBuffer.copy(writeBuffer.readIndex(), writeBuffer.readableBytes(), 0, writeBuffer.readableBytes());
            return false;
        }
        return queue.isEmpty();
    }

    public ConnectionState clientState() {
        return clientState;
    }

    public ConnectionState serverState() {
        return serverState;
    }

    public void clientState(ConnectionState clientState) {
        this.clientState = clientState;
        if (serverState == ConnectionState.HANDSHAKE) this.serverState = clientState;
    }

    public void serverState(ConnectionState serverState) {
        this.serverState = serverState;
    }

    public void compressionThreshold(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
    }

    public void encryption(@Nullable Cipher encryptCipher, @Nullable Cipher decryptCipher) {
        this.encryptCipher = encryptCipher;
        this.decryptCipher = decryptCipher;
    }

    public boolean encryptionEnabled() {
        return encryptCipher != null || decryptCipher != null;
    }

    public NetworkBuffer readBuffer() {
        return readBuffer;
    }

    private void writeSendable(NetworkBuffer buffer, SendablePacket packet) {
        final ConnectionState state = serverState;
        switch (packet) {
            case ServerPacket serverPacket -> writePacket(buffer, state, serverPacket);
            case FramedPacket framedPacket -> {
                final ServerPacket serverPacket = framedPacket.packet();
                writeBuffer(buffer, framedPacket.body(), 0, framedPacket.body().capacity());
                if (serverPacket != null) serverState = PacketVanilla.nextServerState(serverPacket, state);
            }
            case CachedPacket cachedPacket -> {
                final NetworkBuffer body = cachedPacket.body(state);
                if (body != null) {
                    writeBuffer(buffer, body, 0, body.capacity());
                    final ServerPacket serverPacket = cachedPacket.packet(state);
                    if (serverPacket != null) serverState = PacketVanilla.nextServerState(serverPacket, state);
                } else {
                    writePacket(buffer, state, cachedPacket.packet(state));
                }
            }
            case LazyPacket lazyPacket -> writePacket(buffer, state, lazyPacket.packet());
            case BufferedPacket bufferedPacket ->
                    writeBuffer(buffer, bufferedPacket.buffer(), bufferedPacket.index(), bufferedPacket.length());
        }
    }

    private void writePacket(NetworkBuffer buffer, ConnectionState state, ServerPacket packet) {
        PacketWriting.writeFramedPacket(buffer, state, packet, compressionThreshold);
        serverState = PacketVanilla.nextServerState(packet, state);
    }

    private static void writeBuffer(NetworkBuffer target, NetworkBuffer source, long index, long length) {
        target.ensureWritable(length);
        NetworkBuffer.copy(source, index, target, target.writeIndex(), length);
        target.advanceWrite(length);
    }

    @FunctionalInterface
    public interface PacketOperator {
        static PacketOperator identity() {
            return (state, packet) -> packet;
        }

        @Nullable SendablePacket apply(ConnectionState state, SendablePacket packet);
    }

    public static final class Builder {
        private final Registries registries;
        private int readBufferSize = 4096;
        private int writeBufferSize = 4096;
        private int compressionThreshold;

        private Builder(Registries registries) {
            this.registries = registries;
        }

        public Builder readBufferSize(int readBufferSize) {
            this.readBufferSize = readBufferSize;
            return this;
        }

        public Builder writeBufferSize(int writeBufferSize) {
            this.writeBufferSize = writeBufferSize;
            return this;
        }

        public Builder compressionThreshold(int compressionThreshold) {
            this.compressionThreshold = compressionThreshold;
            return this;
        }

        public ProtocolSession build() {
            return new ProtocolSession(this);
        }
    }
}
