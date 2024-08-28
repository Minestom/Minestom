package template;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.scratch.listener.ScratchFeature;
import net.minestom.scratch.network.NetworkContext;
import net.minestom.scratch.registry.ScratchRegistryTools;
import net.minestom.scratch.world.ImmutableChunkRepeatWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRangeUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Limbo server example.
 * <p>
 * Players are unsynchronized, each in their own world.
 */
public final class LimboTemplate {
    private static final SocketAddress ADDRESS = new InetSocketAddress("0.0.0.0", 25565);
    private static final int VIEW_DISTANCE = 8;

    public static void main(String[] args) throws Exception {
        new LimboTemplate();
    }

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.INET);

    private final ReentrantLock stopLock = new ReentrantLock();
    private final Condition stopCondition = stopLock.newCondition();

    LimboTemplate() throws Exception {
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
                Thread.startVirtualThread(connection::networkLoopWrite);
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
        final NetworkContext.Async networkContext = new NetworkContext.Async();
        boolean online = true;

        String username;
        UUID uuid;

        Connection(SocketChannel client) {
            this.client = client;
        }

        void networkLoopRead() {
            while (online) {
                this.online = networkContext.read(buffer -> {
                    try {
                        buffer.readChannel(client);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, this::handlePacket);
            }
        }

        void networkLoopWrite() {
            while (online) {
                this.online = this.networkContext.write(buffer -> {
                    try {
                        buffer.writeChannel(client);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        void handlePacket(ClientPacket packet) {
            if (packet instanceof ClientFinishConfigurationPacket) {
                init();
                return;
            }
            if (networkContext.state() == ConnectionState.PLAY) {
                processPacket(packet);
                return;
            }
            switch (packet) {
                case StatusRequestPacket ignored -> {
                    this.networkContext.write(new ResponsePacket("""
                            {
                                "version": {
                                    "name": "%s",
                                    "protocol": %s
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
                            """.formatted(MinecraftServer.VERSION_NAME, MinecraftServer.PROTOCOL_VERSION)));
                }
                case ClientPingRequestPacket pingRequestPacket -> {
                    this.networkContext.write(new PingResponsePacket(pingRequestPacket.number()));
                }
                case ClientLoginStartPacket startPacket -> {
                    username = startPacket.username();
                    uuid = UUID.randomUUID();
                    this.networkContext.write(new LoginSuccessPacket(startPacket.profileId(), startPacket.username(), 0, false));
                }
                case ClientLoginAcknowledgedPacket ignored -> {
                    this.networkContext.write(ScratchRegistryTools.REGISTRY_PACKETS);
                    this.networkContext.write(new FinishConfigurationPacket());
                }
                default -> {
                }
            }
        }

        private final int id = 1;
        private final ImmutableChunkRepeatWorld world = new ImmutableChunkRepeatWorld(ScratchRegistryTools.DIMENSION_REGISTRY.get(DimensionType.OVERWORLD),
                ScratchRegistryTools.BIOME_REGISTRY, unit -> unit.modifier().fillHeight(0, 48, Block.STONE));
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
                networkContext.write(packet);
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
                return world.chunkPacket(chunkX, chunkZ);
            }

            @Override
            public void sendPacket(ServerPacket.Play packet) {
                networkContext.write(packet);
            }
        });

        void init() {
            final Pos position = new Pos(0, 55, 0);
            this.position = position;
            this.oldPosition = position;

            final DimensionType dimension = world.dimensionType();
            final DynamicRegistry.Key<DimensionType> dimensionKey = ScratchRegistryTools.DIMENSION_REGISTRY.getKey(dimension);
            final int dimensionId = ScratchRegistryTools.DIMENSION_REGISTRY.getId(dimensionKey);

            this.networkContext.write(new JoinGamePacket(
                    id, false, List.of(), 0,
                    VIEW_DISTANCE, VIEW_DISTANCE,
                    false, true, false,
                    dimensionId, dimensionKey.name(),
                    0, GameMode.CREATIVE, null, false, true,
                    new WorldPos(dimensionKey.name(), Vec.ZERO), 0, false));
            this.networkContext.write(new SpawnPositionPacket(position, 0));
            this.networkContext.write(new PlayerPositionAndLookPacket(position, (byte) 0, 0));
            this.networkContext.write(new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                    List.of(
                            new PlayerInfoUpdatePacket.Entry(uuid, username, List.of(),
                                    true, 1, GameMode.CREATIVE, null, null)
                    )));

            this.networkContext.write(new UpdateViewDistancePacket(VIEW_DISTANCE));
            this.networkContext.write(new UpdateViewPositionPacket(position.chunkX(), position.chunkZ()));

            ChunkRangeUtils.forChunksInRange(position.chunkX(), position.chunkZ(), VIEW_DISTANCE,
                    (x, z) -> networkContext.write(world.chunkPacket(x, z)));

            this.networkContext.write(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f));
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
                this.networkContext.write(listHeaderAndFooterPacket);
            }
            this.oldPosition = this.position;
        }
    }
}
