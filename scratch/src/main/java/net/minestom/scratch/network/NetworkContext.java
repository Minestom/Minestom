package net.minestom.scratch.network;

import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketRegistry.PacketInfo;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;

public sealed interface NetworkContext {

    boolean read(Consumer<NetworkBuffer> reader, Consumer<ClientPacket> consumer);

    void write(Packet packet);

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
        final AtomicReference<ConnectionState> stateRef = new AtomicReference<>(ConnectionState.HANDSHAKE);
        final MpmcUnboundedXaddArrayQueue<Packet> packetWriteQueue = new MpmcUnboundedXaddArrayQueue<>(1024);

        final ReentrantLock writeLock = new ReentrantLock();
        final Condition writeCondition = writeLock.newCondition();

        final NetworkBuffer readBuffer = NetworkBuffer.staticBuffer(ServerFlag.POOLED_BUFFER_SIZE);
        NetworkBuffer writeLeftover;  // Contains half-write packets

        @Override
        public boolean read(Consumer<NetworkBuffer> reader, Consumer<ClientPacket> consumer) {
            try {
                reader.accept(readBuffer);
            } catch (Exception e) {
                return false;
            }

            PacketReading.Result<ClientPacket> result;
            try {
                result = PacketReading.readClients(readBuffer, stateRef.get(), false);
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }

            switch (result) {
                case PacketReading.Result.Empty<ClientPacket> ignored -> {
                    // Empty
                }
                case PacketReading.Result.Failure<ClientPacket> failure -> {
                    readBuffer.resize(failure.requiredCapacity());
                }
                case PacketReading.Result.Success<ClientPacket> success -> {
                    for (ClientPacket packet : success.packets()) {
                        stateRef.set(success.newState());
                        consumer.accept(packet);
                    }
                    readBuffer.compact();
                }
            }
            return true;
        }

        public boolean write(Consumer<NetworkBuffer> writer) {
            // Write leftover if any
            NetworkBuffer leftover = this.writeLeftover;
            if (leftover != null) {
                try {
                    writer.accept(leftover);
                } catch (Exception e) {
                    return false;
                }
                final boolean success = leftover.readableBytes() == 0;
                if (success) {
                    this.writeLeftover = null;
                    PacketVanilla.PACKET_POOL.add(leftover);
                } else {
                    // Failed to write the whole leftover, try again next flush
                    return true;
                }
            }

            if (packetWriteQueue.isEmpty()) {
                try {
                    this.writeLock.lock();
                    this.writeCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    this.writeLock.unlock();
                }
            }

            NetworkBuffer buffer = PacketVanilla.PACKET_POOL.get();
            // Write to buffer
            PacketWriting.writeQueue(buffer, packetWriteQueue, 1, (b, packet) -> {
                try {
                    NetworkContext.write(b, PacketVanilla.SERVER_PACKET_PARSER, state(), packet);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });

            // Write to channel
            try {
                writer.accept(buffer);
            } catch (Exception e) {
                return false;
            }
            final boolean success = buffer.readableBytes() == 0;
            // Keep the buffer if not fully written
            if (success) PacketVanilla.PACKET_POOL.add(buffer);
            else this.writeLeftover = buffer;

            return true;
        }

        @Override
        public void write(Packet packet) {
            this.packetWriteQueue.add(packet);
            this.writeLock.lock();
            this.writeCondition.signal();
            this.writeLock.unlock();
        }

        @Override
        public ConnectionState state() {
            return stateRef.get();
        }
    }

    static void write(NetworkBuffer buffer, PacketParser<ServerPacket> parser, ConnectionState state,
                      Packet packet) {
        switch (packet) {
            case NetworkContext.Packet.PacketIdPair packetPair -> {
                final ServerPacket packetLoop = packetPair.packet;
                final PacketInfo<ServerPacket> info = parser.stateRegistry(state).packetInfo(packetLoop.getClass());
                PacketWriting.writeFramedPacket(buffer, info, packetLoop, 0);
            }
            case NetworkContext.Packet.PlayList playList -> {
                final Collection<ServerPacket.Play> packets = playList.packets();
                final int[] exception = playList.exception();
                int index = 0;
                for (ServerPacket.Play packetLoop : packets) {
                    if (exception.length > 0 && Arrays.binarySearch(exception, index++) >= 0) continue;
                    final PacketInfo<ServerPacket> info = parser.stateRegistry(state).packetInfo(packetLoop.getClass());
                    PacketWriting.writeFramedPacket(buffer, info, packetLoop, 0);
                }
            }
        }
    }
}
