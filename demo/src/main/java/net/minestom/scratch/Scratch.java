package net.minestom.scratch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.scratch.tools.ScratchBlockTools.World;
import net.minestom.scratch.tools.ScratchFeature;
import net.minestom.scratch.tools.ScratchNetworkTools.NetworkContext;
import net.minestom.scratch.tools.ScratchVelocityTools;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.minestom.scratch.tools.ScratchNetworkTools.readPackets;
import static net.minestom.scratch.tools.ScratchTools.REGISTRY_DATA_PACKET;
import static net.minestom.scratch.tools.ScratchViewTools.Broadcaster;
import static net.minestom.scratch.tools.ScratchViewTools.Synchronizer;

/**
 * Example of the minestom API used to create a server from scratch.
 * <p>
 * Tools are used to ease the implementation.
 */
public final class Scratch {
    private static final SocketAddress ADDRESS = new InetSocketAddress("0.0.0.0", 25565);
    private static final int VIEW_DISTANCE = 8;

    public static void main(String[] args) throws Exception {
        new Scratch();
    }

    private final AtomicInteger lastEntityId = new AtomicInteger();
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.INET);
    private final ConcurrentLinkedQueue<PlayerInfo> waitingPlayers = new ConcurrentLinkedQueue<>();

    private final Instance instance = new Instance(DimensionType.OVERWORLD, new World(DimensionType.OVERWORLD));
    private final Map<Integer, Player> players = new HashMap<>();
    private final Map<Integer, Entity> entities = new HashMap<>();

    Scratch() throws Exception {
        server.bind(ADDRESS);
        System.out.println("Server started on: " + ADDRESS);
        Thread.startVirtualThread(this::listenCommands);
        Thread.startVirtualThread(this::listenConnections);
        ticks();
        server.close();
        System.out.println("Server stopped");
    }

    void listenCommands() {
        Scanner scanner = new Scanner(System.in);
        while (serverRunning()) {
            final String line = scanner.nextLine();
            if (line.equals("stop")) {
                stop.set(true);
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

    void ticks() {
        {
            var entity = new Entity(EntityType.ZOMBIE, instance, new Pos(0, 60, 0));
            entities.put(entity.id, entity);
        }
        int keepAliveId = 0;
        while (serverRunning()) {
            final long time = System.nanoTime();
            // Connect waiting players
            PlayerInfo playerInfo;
            while ((playerInfo = waitingPlayers.poll()) != null) {
                final Player player = new Player(playerInfo, instance, new Pos(0, 55, 0));
                this.players.put(player.id, player);
            }
            // Tick playing players
            final boolean sendKeepAlive = keepAliveId++ % (20 * 20) == 0;
            for (Player player : players.values()) {
                if (!player.connection.online) {
                    this.players.remove(player.id);
                    var synchronizerEntry = player.synchronizerEntry;
                    if (synchronizerEntry != null) synchronizerEntry.unmake();
                    continue;
                }
                if (sendKeepAlive) player.connection.sendPacket(new KeepAlivePacket(keepAliveId));
                player.tick();
            }
            // Tick entities
            for (Entity entity : entities.values()) {
                entity.tick();
            }
            // Compute broadcast packets
            try (Broadcaster.Collector collector = instance.broadcaster.collector()) {
                final List<ServerPacket.Play> packets = collector.packets();
                if (!packets.isEmpty()) {
                    for (Player player : players.values()) {
                        final int[] exception = collector.exception(player.id);
                        player.connection.networkContext.write(new NetworkContext.Packet.PlayList(packets, exception));
                    }
                }
            }
            // Compute view packets
            this.instance.synchronizer.computePackets((playerId, packet) -> {
                final Player player = players.get(playerId);
                if (player != null) player.connection.networkContext.write(packet);
            });
            {
                final long heapUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                final double elapsed = (System.nanoTime() - time) / 1_000_000.0;
                final PlayerListHeaderAndFooterPacket packet = new PlayerListHeaderAndFooterPacket(
                        Component.text("Welcome to Minestom!"),
                        Component.text("Tick: " + String.format("%.2f", elapsed) + "ms")
                                .append(Component.newline())
                                .append(Component.text("Heap: " + heapUsage / 1024 / 1024 + "MB"))
                );
                players.values().forEach(player -> player.connection.sendPacket(packet));
            }
            // Flush all connections
            for (Player player : players.values()) {
                player.connection.networkContext.flush();
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    boolean serverRunning() {
        return !stop.get();
    }

    static final class Instance {
        final DimensionType dimensionType;
        final World world;
        final Broadcaster broadcaster = new Broadcaster();
        final Synchronizer synchronizer = new Synchronizer(VIEW_DISTANCE);

        public Instance(DimensionType dimensionType,
                        World world) {
            this.dimensionType = dimensionType;
            this.world = world;
        }
    }

    final class Connection {
        final SocketChannel client;
        final NetworkContext.Async networkContext = new NetworkContext.Async();
        final AtomicReference<ConnectionState> stateRef = new AtomicReference<>(ConnectionState.HANDSHAKE);
        final ConcurrentLinkedQueue<ClientPacket> packetQueue = new ConcurrentLinkedQueue<>();
        volatile boolean online = true;

        final AtomicReference<String> nameRef = new AtomicReference<>();
        final AtomicReference<UUID> uuidRef = new AtomicReference<>();

        Connection(SocketChannel client) {
            this.client = client;
        }

        void networkLoopRead() {
            while (online) {
                this.networkContext.lendReadBuffer(readBuffer -> {
                    try {
                        final int length = client.read(readBuffer);
                        if (length > 0) {
                            readPackets(readBuffer.flip(), stateRef, this::handleAsyncPacket);
                        } else if (length == -1) online = false;
                    } catch (IOException e) {
                        online = false;
                    }
                    return online;
                });
            }
        }

        void networkLoopWrite() {
            while (online) {
                this.networkContext.lendWriteBuffer(writeBuffer -> {
                    try {
                        final int length = client.write(writeBuffer.flip());
                        if (length == -1) online = false;
                    } catch (IOException e) {
                        online = false;
                    }
                    return online;
                });
            }
        }

        void handleAsyncPacket(ClientPacket packet) {
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
                                        "max": 100,
                                        "online": 0
                                    },
                                    "description": {
                                        "text": "Awesome Minestom"
                                    },
                                    "enforcesSecureChat": false,
                                    "previewsChat": false
                                }
                                """));
                    } else if (packet instanceof ClientPingRequestPacket pingRequestPacket) {
                        networkContext.writeStatus(new PingResponsePacket(pingRequestPacket.number()));
                    }
                    this.networkContext.flush();
                }
                case LOGIN -> {
                    if (packet instanceof ClientLoginStartPacket startPacket) {
                        nameRef.set(startPacket.username());
                        uuidRef.set(UUID.randomUUID());
                        networkContext.writeLogin(new LoginSuccessPacket(startPacket.profileId(), startPacket.username(), 0));
                    } else if (packet instanceof ClientLoginAcknowledgedPacket) {
                        stateRef.set(ConnectionState.CONFIGURATION);
                        networkContext.writeConfiguration(REGISTRY_DATA_PACKET);
                        networkContext.writeConfiguration(new FinishConfigurationPacket());
                    }
                    this.networkContext.flush();
                }
                case CONFIGURATION -> {
                    if (packet instanceof ClientFinishConfigurationPacket) {
                        stateRef.set(ConnectionState.PLAY);
                        waitingPlayers.offer(new PlayerInfo(this, nameRef.get(), uuidRef.get()));
                    }
                }
                case PLAY -> packetQueue.add(packet);
            }
        }

        void sendPacket(ServerPacket.Play packet) {
            this.networkContext.writePlay(packet);
        }
    }

    record PlayerInfo(Connection connection, String username, UUID uuid) {
    }

    final class Entity {
        private final int id = lastEntityId.incrementAndGet();
        private final UUID uuid = UUID.randomUUID();
        final EntityType type;
        final Instance instance;
        final Synchronizer.Entry synchronizerEntry;

        final Aerodynamics aerodynamics;

        Pos position;
        Vec velocity = Vec.ZERO;
        boolean onGround;

        Entity(EntityType type, Instance instance, Pos position) {
            this.type = type;
            this.instance = instance;
            this.position = position;
            this.synchronizerEntry = instance.synchronizer.makeEntry(false, id, position,
                    () -> {
                        final var spawnPacket = new SpawnEntityPacket(
                                id, uuid, type.id(),
                                this.position, 0, 0, (short) 0, (short) 0, (short) 0
                        );
                        return List.of(spawnPacket);
                    },
                    () -> List.of(new DestroyEntitiesPacket(id)));

            this.aerodynamics = new Aerodynamics(type.registry().acceleration(), 0.91, 1 - type.registry().drag());
        }

        void tick() {
            Block.Getter getter = (x, y, z, condition) -> y > 47 ? Block.AIR : Block.STONE;
            var worldBorder = new WorldBorder(null);
            PhysicsResult physicsResult = PhysicsUtils.simulateMovement(position, velocity, type.registry().boundingBox(),
                    worldBorder, getter, aerodynamics, false, true, onGround, false);

            this.position = physicsResult.newPosition();
            velocity = physicsResult.newVelocity();
            onGround = physicsResult.isOnGround();

            synchronizerEntry.move(physicsResult.newPosition());
            synchronizerEntry.signalUpdate(new EntityTeleportPacket(id, physicsResult.newPosition(), onGround));
            if (!velocity.isZero())
                synchronizerEntry.signalUpdate(new EntityVelocityPacket(id, velocity.mul(8000f / ServerFlag.SERVER_TICKS_PER_SECOND)));
        }
    }

    final class Player {
        private final int id = lastEntityId.incrementAndGet();
        private final Connection connection;
        private final String username;
        private final UUID uuid;

        final Instance instance;
        final Synchronizer.Entry synchronizerEntry;
        Pos position;
        Pos oldPosition;

        final ScratchFeature.Messaging messaging;
        final ScratchFeature.Movement movement;
        final ScratchFeature.ChunkLoading chunkLoading;
        final ScratchFeature.EntityInteract entityInteract;

        Player(PlayerInfo info, Instance spawnInstance, Pos spawnPosition) {
            this.connection = info.connection;
            this.username = info.username;
            this.uuid = info.uuid;

            this.instance = spawnInstance;
            this.position = spawnPosition;
            this.oldPosition = spawnPosition;

            this.synchronizerEntry = instance.synchronizer.makeEntry(true, id, position,
                    () -> {
                        final var spawnPacket = new SpawnEntityPacket(
                                id, uuid, EntityType.PLAYER.id(),
                                this.position, 0, 0, (short) 0, (short) 0, (short) 0
                        );
                        return List.of(getAddPlayerToList(), spawnPacket);
                    },
                    () -> List.of(new DestroyEntitiesPacket(id)));

            this.messaging = new ScratchFeature.Messaging(new ScratchFeature.Messaging.Mapping() {
                @Override
                public Component formatMessage(String message) {
                    return Component.text(username).color(TextColor.color(0x00FF00))
                            .append(Component.text(" > "))
                            .append(Component.text(message));
                }

                @Override
                public void signal(ServerPacket.Play packet) {
                    instance.broadcaster.append(packet);
                }
            });

            this.movement = new ScratchFeature.Movement(new ScratchFeature.Movement.Mapping() {
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
                    Player.this.position = position;
                    synchronizerEntry.move(position);
                }

                @Override
                public void signalMovement(ServerPacket.Play packet) {
                    synchronizerEntry.signalUpdate(packet);
                }
            });

            this.chunkLoading = new ScratchFeature.ChunkLoading(new ScratchFeature.ChunkLoading.Mapping() {
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
                    return instance.world.generatePacket(chunkX, chunkZ);
                }

                @Override
                public void sendPacket(ServerPacket.Play packet) {
                    Player.this.connection.sendPacket(packet);
                }
            });

            this.entityInteract = new ScratchFeature.EntityInteract(new ScratchFeature.EntityInteract.Mapping() {
                @Override
                public void left(int id) {
                    Entity entity = entities.get(id);
                    if (entity == null) return;
                    entity.velocity = ScratchVelocityTools.knockback(position, 0.4f, entity.velocity, entity.onGround);
                }

                @Override
                public void right(int id) {
                }
            });

            this.connection.networkContext.writePlays(initPackets());
        }

        private List<ServerPacket.Play> initPackets() {
            final DimensionType dimensionType = instance.dimensionType;
            World world = instance.world;
            List<ServerPacket.Play> packets = new ArrayList<>();

            final JoinGamePacket joinGamePacket = new JoinGamePacket(
                    id, false, List.of(), 0,
                    8, 8,
                    false, true, false,
                    dimensionType.toString(), "world",
                    0, GameMode.CREATIVE, null, false, true,
                    new DeathLocation("dimension", Vec.ZERO), 0);
            packets.add(joinGamePacket);
            packets.add(new SpawnPositionPacket(position, 0));
            packets.add(new PlayerPositionAndLookPacket(position, (byte) 0, 0));
            packets.add(getAddPlayerToList());

            packets.add(new UpdateViewDistancePacket(VIEW_DISTANCE));
            packets.add(new UpdateViewPositionPacket(position.chunkX(), position.chunkZ()));
            ChunkUtils.forChunksInRange(position.chunkX(), position.chunkZ(), VIEW_DISTANCE,
                    (x, z) -> packets.add(world.generatePacket(x, z)));

            packets.add(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0f));

            return packets;
        }

        void tick() {
            ClientPacket packet;
            while ((packet = connection.packetQueue.poll()) != null) {
                this.messaging.accept(packet);
                this.movement.accept(packet);
                this.chunkLoading.accept(packet);
                this.entityInteract.accept(packet);
            }
            this.oldPosition = this.position;
        }

        private PlayerInfoUpdatePacket getAddPlayerToList() {
            final var infoEntry = new PlayerInfoUpdatePacket.Entry(uuid, username, List.of(),
                    true, 1, GameMode.CREATIVE, null, null);
            return new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                    List.of(infoEntry));
        }
    }
}
