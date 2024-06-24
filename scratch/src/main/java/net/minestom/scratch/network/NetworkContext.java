package net.minestom.scratch.network;

import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketRegistry.PacketInfo;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.DataFormatException;

public sealed interface NetworkContext {

    boolean read(Function<ByteBuffer, Integer> reader, Consumer<ClientPacket> consumer);

    void write(Packet packet);

    void flush();

    ConnectionState state();

    default void write(ServerPacket packet) {
        write(new Packet.PacketIdPair(packet));
    }

    default void write(List<ServerPacket> packets) {
        for (ServerPacket packet : packets) write(packet);
    }

    default void writePlays(List<ServerPacket.Play> packets) {
        write(new NetworkContext.Packet.PlayList(packets));
    }

    sealed interface Packet {
        record PacketIdPair(ServerPacket packet) implements Packet {
        }

        record PlayList(List<ServerPacket.Play> packets, int[] exception) implements Packet {
            public PlayList {
                packets = List.copyOf(packets);
            }

            public PlayList(List<ServerPacket.Play> packets) {
                this(packets, IntArrays.EMPTY_ARRAY);
            }
        }
    }

    final class Async implements NetworkContext {
        private final PacketParser.Server serverParser = new PacketParser.Server();
        private final PacketParser.Client clientParser = new PacketParser.Client();

        final AtomicReference<ConnectionState> stateRef = new AtomicReference<>(ConnectionState.HANDSHAKE);
        final MpmcUnboundedXaddArrayQueue<Packet> packetWriteQueue = new MpmcUnboundedXaddArrayQueue<>(1024);

        final ReentrantLock writeLock = new ReentrantLock();
        final Condition writeCondition = writeLock.newCondition();

        final ByteBuffer readBuffer = ByteBuffer.allocateDirect(ServerFlag.POOLED_BUFFER_SIZE);
        final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(ServerFlag.POOLED_BUFFER_SIZE);
        ByteBuffer cacheBuffer; // Contains half-read packets

        @Override
        public boolean read(Function<ByteBuffer, Integer> reader, Consumer<ClientPacket> consumer) {
            ByteBuffer buffer = this.readBuffer.clear();
            if (cacheBuffer != null) buffer.put(cacheBuffer);
            final int length = reader.apply(buffer);
            if (length == -1) return false;
            this.cacheBuffer = null;
            readPackets(clientParser, buffer.flip(), stateRef, clientPacket -> {
                stateRef.set(nextState(clientPacket, stateRef.get()));
                consumer.accept(clientPacket);
            });
            if (buffer.hasRemaining()) {
                // Copy remaining data to cache buffer
                this.cacheBuffer = ByteBuffer.allocateDirect(buffer.remaining()).put(buffer);
            }
            return true;
        }

        public boolean write(Function<ByteBuffer, Integer> writer) {
            try {
                this.writeLock.lock();
                this.writeCondition.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                this.writeLock.unlock();
            }

            AtomicBoolean result = new AtomicBoolean(true);
            ByteBuffer buffer = this.writeBuffer.clear();
            Packet packet;
            while ((packet = packetWriteQueue.poll()) != null) {
                NetworkContext.write(serverParser, state(), packet, buffer, b -> {
                    final int length = writer.apply(b);
                    b.compact();
                    if (length == -1) {
                        result.setPlain(false);
                        return false;
                    }
                    return true;
                });
            }
            while (buffer.hasRemaining()) {
                final int length = writer.apply(buffer);
                if (length == -1) {
                    result.setPlain(false);
                    break;
                }
            }
            return result.getPlain();
        }

        @Override
        public void write(Packet packet) {
            this.packetWriteQueue.add(packet);
        }

        @Override
        public void flush() {
            try {
                this.writeLock.lock();
                this.writeCondition.signal();
            } finally {
                this.writeLock.unlock();
            }
        }

        @Override
        public ConnectionState state() {
            return stateRef.get();
        }
    }

    final class Sync implements NetworkContext {
        private final PacketParser.Server serverParser = new PacketParser.Server();
        private final PacketParser.Client clientParser = new PacketParser.Client();

        final AtomicReference<ConnectionState> stateRef = new AtomicReference<>(ConnectionState.HANDSHAKE);
        final Predicate<ByteBuffer> writer;
        final ArrayDeque<Packet> packetWriteQueue = new ArrayDeque<>();

        final MpmcUnboundedXaddArrayQueue<ClientPacket> packetReadQueue = new MpmcUnboundedXaddArrayQueue<>(1024);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(ServerFlag.POOLED_BUFFER_SIZE);
        ByteBuffer cacheBuffer; // Contains half-read packets

        public Sync(Predicate<ByteBuffer> writer) {
            this.writer = writer;
        }

        @Override
        public boolean read(Function<ByteBuffer, Integer> reader, Consumer<ClientPacket> consumer) {
            ByteBuffer buffer = this.buffer.clear();
            if (cacheBuffer != null) buffer.put(cacheBuffer);
            final int length = reader.apply(buffer);
            if (length == -1) return false;
            this.cacheBuffer = null;
            var queue = this.packetReadQueue;
            readPackets(clientParser, buffer.flip(), stateRef, clientPacket -> {
                stateRef.set(nextState(clientPacket, stateRef.get()));
                queue.relaxedOffer(clientPacket);
            });
            if (buffer.hasRemaining()) {
                // Copy remaining data to cache buffer
                this.cacheBuffer = ByteBuffer.allocateDirect(buffer.remaining()).put(buffer);
            }
            queue.drain(consumer::accept);
            return true;
        }

        @Override
        public void write(Packet packet) {
            this.packetWriteQueue.add(packet);
        }

        @Override
        public void flush() {
            ByteBuffer buffer = this.buffer.clear();
            Packet packet;
            while ((packet = packetWriteQueue.poll()) != null) {
                NetworkContext.write(serverParser, state(), packet, buffer, b -> {
                    final boolean result = writer.test(b);
                    b.compact();
                    return result;
                });
            }
            while (buffer.hasRemaining() && writer.test(buffer)) ;
        }

        @Override
        public ConnectionState state() {
            return stateRef.get();
        }
    }

    static void readPackets(PacketParser<ClientPacket> parser, ByteBuffer buffer,
                            AtomicReference<ConnectionState> stateRef,
                            Consumer<ClientPacket> consumer) {
        try {
            PacketUtils.readPackets(buffer, false,
                    (id, payload) -> {
                        final ConnectionState state = stateRef.get();
                        NetworkBuffer networkBuffer = new NetworkBuffer(payload);
                        final ClientPacket packet = parser.parse(state, id, networkBuffer);
                        payload.position(networkBuffer.readIndex());
                        consumer.accept(packet);
                    });
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }
    }

    static void write(PacketParser<ServerPacket> parser, ConnectionState state,
                      Packet packet, ByteBuffer buffer,
                      Predicate<ByteBuffer> fullCallback) {
        final int checkLength = buffer.limit() / 2;
        switch (packet) {
            case NetworkContext.Packet.PacketIdPair packetPair -> {
                final ServerPacket packetLoop = packetPair.packet;
                if (buffer.position() >= checkLength) {
                    if (!fullCallback.test(buffer)) return;
                }
                final PacketInfo<ServerPacket> info = parser.stateRegistry(state).packetInfo(packetLoop.getClass());
                PacketUtils.writeFramedPacket(buffer, info.id(), info.serializer(), packetLoop, 0);
            }
            case NetworkContext.Packet.PlayList playList -> {
                final Collection<ServerPacket.Play> packets = playList.packets();
                final int[] exception = playList.exception();
                int index = 0;
                for (ServerPacket.Play packetLoop : packets) {
                    if (exception.length > 0 && Arrays.binarySearch(exception, index++) >= 0) continue;
                    if (buffer.position() >= checkLength) {
                        if (!fullCallback.test(buffer)) return;
                    }
                    final PacketInfo<ServerPacket> info = parser.stateRegistry(state).packetInfo(packetLoop.getClass());
                    PacketUtils.writeFramedPacket(buffer, info.id(), info.serializer(), packetLoop, 0);
                }
            }
        }
    }

    static ConnectionState nextState(ClientPacket packet, ConnectionState currentState) {
        return switch (packet) {
            case ClientHandshakePacket handshakePacket -> switch (handshakePacket.intent()) {
                case STATUS -> ConnectionState.STATUS;
                case LOGIN, TRANSFER -> ConnectionState.LOGIN;
            };
            case ClientLoginAcknowledgedPacket ignored -> ConnectionState.CONFIGURATION;
            case ClientFinishConfigurationPacket ignored -> ConnectionState.PLAY;
            default -> currentState;
        };
    }
}
