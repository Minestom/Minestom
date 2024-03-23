package net.minestom.scratch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.scratch.tools.ScratchBlockTools.World;
import net.minestom.scratch.tools.ScratchFeature;
import net.minestom.scratch.tools.ScratchNetworkTools.NetworkContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.play.data.DeathLocation;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static net.minestom.scratch.tools.ScratchNetworkTools.readPackets;
import static net.minestom.scratch.tools.ScratchTools.REGISTRY_DATA_PACKET;

/**
 * Limbo server example.
 * <p>
 * Players are unsynchronized, each in their own world.
 */
public final class ScratchLimbo {
    private static final SocketAddress ADDRESS = new InetSocketAddress("0.0.0.0", 25565);
    private static final int VIEW_DISTANCE = 8;

    public static void main(String[] args) throws Exception {
        new ScratchLimbo();
    }

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.INET);

    private final ReentrantLock stopLock = new ReentrantLock();
    private final Condition stopCondition = stopLock.newCondition();

    ScratchLimbo() throws Exception {
        server.bind(ADDRESS);
        System.out.println("Server started on: " + ADDRESS);
        Thread.startVirtualThread(this::listenCommands);
        Thread.startVirtualThread(this::listenConnections);
        // Wait until the server is stopped
        stopLock.lock();
        try {
            stopCondition.await();
        } finally {
            stopLock.unlock();
        }
        server.close();
        System.out.println("Server stopped");
    }

    void listenCommands() {
        Scanner scanner = new Scanner(System.in);
        while (serverRunning()) {
            final String line = scanner.nextLine();
            if (line.equals("stop")) {
                stop();
                System.out.println("Stopping server...");
            } else if (line.equals("gc")) {
                System.gc();
            }
        }
    }

    void listenConnections() {
        while (serverRunning()) {
            try {
                final SocketChannel client = server.accept();
                System.out.println("Accepted connection from " + client.getRemoteAddress());
                Connection connection = new Connection(client);
                Thread.startVirtualThread(connection::networkLoopRead);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void stop() {
        stopLock.lock();
        try {
            stopCondition.signal();
        } finally {
            stopLock.unlock();
        }
        stop.set(true);
    }

    boolean serverRunning() {
        return !stop.get();
    }

    static final class Connection {
        final SocketChannel client;
        final NetworkContext networkContext = new NetworkContext.Sync(this::write);
        final AtomicReference<ConnectionState> stateRef = new AtomicReference<>(ConnectionState.HANDSHAKE);
        boolean online = true;

        String username;
        UUID uuid;

        Connection(SocketChannel client) {
            this.client = client;
        }

        void networkLoopRead() {
            while (online) {
                this.networkContext.lendReadBuffer(readBuffer -> {
                    try {
                        final int length = client.read(readBuffer);
                        if (length > 0) {
                            readPackets(readBuffer.flip(), stateRef, this::handlePacket);
                        } else if (length == -1) online = false;
                    } catch (IOException e) {
                        online = false;
                    }
                    return online;
                });
            }
        }

        boolean write(ByteBuffer buffer) {
            try {
                final int length = client.write(buffer.flip());
                if (length == -1) online = false;
            } catch (IOException e) {
                online = false;
            }
            return online;
        }

        void handlePacket(ClientPacket packet) {
            //System.out.println(state + ": " + packet);
            switch (stateRef.get()) {
                case HANDSHAKE -> {
                    if (packet instanceof ClientHandshakePacket handshakePacket) {
                        final ConnectionState state = switch (handshakePacket.intent()) {
                            case 1 -> ConnectionState.STATUS;
                            case 2 -> ConnectionState.LOGIN;
                            default -> throw new IllegalStateException("Unexpected value: " + handshakePacket.intent());
                        };
                        this.stateRef.set(state);
                    }
                }
                case STATUS -> {
                    if (packet instanceof StatusRequestPacket) {
                        networkContext.writeStatus(new ResponsePacket("""
                                {
                                    "version": {
                                        "name": "1.20.4",
                                        "protocol": 765
                                    },
                                    "players": {
                                        "max": 0,
                                        "online": 0
                                    },
                                    "description": {
                                        "text": "Awesome Minestom Limbo"
                                    },
                                    "enforcesSecureChat": false,
                                    "previewsChat": false
                                }
                                """));
                    } else if (packet instanceof ClientPingRequestPacket pingRequestPacket) {
                        networkContext.writeStatus(new PingResponsePacket(pingRequestPacket.number()));
                    }
                }
                case LOGIN -> {
                    if (packet instanceof ClientLoginStartPacket startPacket) {
                        username = startPacket.username();
                        uuid = UUID.randomUUID();
                        networkContext.writeLogin(new LoginSuccessPacket(startPacket.profileId(), startPacket.username(), 0));
                    } else if (packet instanceof ClientLoginAcknowledgedPacket) {
                        stateRef.set(ConnectionState.CONFIGURATION);
                        networkContext.writeConfiguration(REGISTRY_DATA_PACKET);
                        networkContext.writeConfiguration(new FinishConfigurationPacket());
                    }
                }
                case CONFIGURATION -> {
                    if (packet instanceof ClientFinishConfigurationPacket) {
                        stateRef.set(ConnectionState.PLAY);
                        init();
                    }
                }
                case PLAY -> processPacket(packet);
            }
            this.networkContext.flush();
        }

        private final int id = 1;
        private final World world = new World(DimensionType.OVERWORLD);
        Pos position;
        Pos oldPosition;

        final ScratchFeature.Messaging messaging = new ScratchFeature.Messaging(new ScratchFeature.Messaging.Mapping() {
            @Override
            public Component formatMessage(String message) {
                return Component.text(username).color(TextColor.color(0x00FF00))
                        .append(Component.text(" > "))
                        .append(Component.text(message));
            }

            @Override
            public void signal(ServerPacket.Play packet) {
                networkContext.writePlay(packet);
            }
        });

        final ScratchFeature.Movement movement = new ScratchFeature.Movement(new ScratchFeature.Movement.Mapping() {
            @Override
            public int id() {
                return id;
            }

            @Override
            public Pos position() {
                return position;
            }

            @Override
            public void updatePosition(Pos position) {
                Connection.this.position = position;
            }

            @Override
            public void signalMovement(ServerPacket.Play packet) {
                // Nothing to update
            }
        });

        final ScratchFeature.ChunkLoading chunkLoading = new ScratchFeature.ChunkLoading(new ScratchFeature.ChunkLoading.Mapping() {
            @Override
            public int viewDistance() {
                return VIEW_DISTANCE;
            }

            @Override
            public Pos oldPosition() {
                return oldPosition;
            }

            @Override
            public ChunkDataPacket chunkPacket(int chunkX, int chunkZ) {
                return world.generatePacket(chunkX, chunkZ);
            }

            @Override
            public void sendPacket(ServerPacket.Play packet) {
                networkContext.writePlay(packet);
            }
        });

        void init() {
            final Pos position = new Pos(0, 55, 0);
            this.position = position;
            this.oldPosition = position;
            final DimensionType dimensionType = world.dimensionType();

            this.networkContext.writePlay(new JoinGamePacket(
                    id, false, List.of(), 0,
                    VIEW_DISTANCE, VIEW_DISTANCE,
                    false, true, false,
                    dimensionType.toString(), "world",
                    0, GameMode.CREATIVE, null, false, true,
                    new DeathLocation("dimension", Vec.ZERO), 0));
            this.networkContext.writePlay(new SpawnPositionPacket(position, 0));
            this.networkContext.writePlay(new PlayerPositionAndLookPacket(position, (byte) 0, 0));
            this.networkContext.writePlay(new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                    List.of(
                            new PlayerInfoUpdatePacket.Entry(uuid, username, List.of(),
                                    true, 1, GameMode.CREATIVE, null, null)
                    )));

            this.networkContext.writePlay(new UpdateViewDistancePacket(VIEW_DISTANCE));
            this.networkContext.writePlay(new UpdateViewPositionPacket(position.chunkX(), position.chunkZ()));

            ChunkUtils.forChunksInRange(position.chunkX(), position.chunkZ(), VIEW_DISTANCE,
                    (x, z) -> networkContext.writePlay(world.generatePacket(x, z)));

            this.networkContext.writePlay(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f));
        }

        void processPacket(ClientPacket packet) {
            this.messaging.accept(packet);
            this.movement.accept(packet);
            this.chunkLoading.accept(packet);
            {
                final long heapUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                final PlayerListHeaderAndFooterPacket listHeaderAndFooterPacket = new PlayerListHeaderAndFooterPacket(
                        Component.text("Welcome to Minestom Limbo!"),
                        Component.text("Heap: " + heapUsage / 1024 / 1024 + "MB")
                );
                this.networkContext.writePlay(listHeaderAndFooterPacket);
            }
            this.oldPosition = this.position;
        }
    }
}
