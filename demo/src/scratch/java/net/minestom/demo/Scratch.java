package net.minestom.demo;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.GeneratorImpl;
import net.minestom.server.instance.heightmap.HeightmapType;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.configuration.SelectKnownPacksPacket;
import net.minestom.server.network.packet.server.configuration.UpdateEnabledFeaturesPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.network.packet.server.play.data.PlayerSpawnInfo;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;

import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import static net.minestom.server.MinecraftConstants.PROTOCOL_VERSION;

/**
 * A deliberately small Minecraft server built directly on Minestom's packet/data API.
 * <p>
 * This does not start Minestom's socket server, player system, instances, entities, or tick loop.
 * Everything below is plain sockets plus virtual threads, backed by a tiny Registries implementation for packet codecs.
 */
public final class Scratch {
    private static final SocketAddress ADDRESS = new InetSocketAddress("0.0.0.0", 25565);
    private static final int VIEW_DISTANCE = 3;
    private static final String WORLD = "minecraft:overworld";
    private static final int ENTITY_ID = 1;
    private static final int SEA_LEVEL = 63;
    private static final int MIN_Y = DimensionType.VANILLA_MIN_Y;
    private static final int HEIGHT = DimensionType.VANILLA_MAX_Y - DimensionType.VANILLA_MIN_Y + 1;
    private static final int SECTION_COUNT = HEIGHT / 16;
    private static final int GROUND_Y = 0;
    private static final String STATUS_JSON = """
            {
              "version": {"name": "Minestom Scratch", "protocol": %d},
              "players": {"max": 1, "online": 0},
              "description": {"text": "Minestom packet API scratch server"},
              "enforcesSecureChat": false
            }
            """.formatted(PROTOCOL_VERSION);

    static void main() throws IOException {
        var registries = Registries.vanilla();
        var flatWorld = FlatWorld.create(registries);

        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(ADDRESS);
            System.out.println("Scratch server listening on " + ADDRESS);

            while (true) {
                SocketChannel channel = server.accept();
                channel.configureBlocking(true);
                Thread.ofVirtual().name("scratch-client-", 0).start(() -> serve(channel, registries, flatWorld));
            }
        }
    }

    private static void serve(SocketChannel channel, Registries registries, FlatWorld flatWorld) {
        try (channel) {
            var connection = new Connection(channel, registries);
            ConnectionState clientState = ConnectionState.HANDSHAKE;
            while (channel.isOpen()) {
                connection.readBuffer.readChannel(channel);
                switch (PacketReading.readPackets(
                        connection.readBuffer,
                        PacketVanilla.CLIENT_PACKET_PARSER,
                        clientState,
                        PacketVanilla::nextClientState,
                        false
                )) {
                    case PacketReading.Result.Success<ClientPacket> success -> {
                        for (var parsed : success.packets()) {
                            clientState = parsed.nextState();
                            if (parsed.packet() instanceof ClientHandshakePacket) {
                                connection.serverState = clientState;
                            }
                            switch (parsed.packet()) {
                                case StatusRequestPacket ignored -> connection.send(new ResponsePacket(STATUS_JSON));
                                case ClientPingRequestPacket ping ->
                                        connection.send(new PingResponsePacket(ping.number()));
                                case ClientLoginStartPacket login -> connection.send(new LoginSuccessPacket(
                                        new GameProfile(login.profileId(), login.username()),
                                        new UUID(0L, 0L)));
                                case ClientLoginAcknowledgedPacket _ -> sendConfigurationStart(connection);
                                case ClientSelectKnownPacksPacket _ -> sendConfigurationData(connection, registries);
                                case ClientFinishConfigurationPacket _ ->
                                        sendJoinGame(connection, registries, flatWorld);
                                default -> {
                                }
                            }
                        }
                        connection.readBuffer.compact();
                    }
                    case PacketReading.Result.Empty<ClientPacket> _ -> {
                    }
                    case PacketReading.Result.Failure<ClientPacket> failure ->
                            connection.readBuffer.resize(failure.requiredCapacity());
                }
            }
        } catch (EOFException _) {
            // Normal client disconnect.
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static void sendConfigurationStart(Connection connection) {
        connection.send(PluginMessagePacket.brandPacket("Minestom Scratch"));
        connection.send(new UpdateEnabledFeaturesPacket(List.of("minecraft:vanilla")));
        connection.send(new SelectKnownPacksPacket(List.of(SelectKnownPacksPacket.MINECRAFT_CORE)));
    }

    private static void sendConfigurationData(Connection connection, Registries registries) {
        for (SendablePacket packet : Registries.registryDataPackets(registries, false)) {
            connection.send(packet);
        }

        connection.send(Registries.tagsPacket(registries));
        connection.send(new FinishConfigurationPacket());
    }

    private static void sendJoinGame(Connection connection, Registries registries, FlatWorld flatWorld) {
        int dimensionTypeId = registries.dimensionType().getId(DimensionType.OVERWORLD);
        var spawn = new Vec(8.5, GROUND_Y + 2, 8.5);

        connection.send(new JoinGamePacket(
                ENTITY_ID, false, List.of(WORLD), 1,
                VIEW_DISTANCE, VIEW_DISTANCE, false, true, false,
                new PlayerSpawnInfo(dimensionTypeId, WORLD, 0L, GameMode.CREATIVE, null,
                                    false, true, null, 0, SEA_LEVEL),
                true, true
        ));
        connection.send(new ServerDifficultyPacket(Difficulty.PEACEFUL, true));
        connection.send(new SpawnPositionPacket(new WorldPos(WORLD, spawn), 0f, 0f));
        connection.send(new PlayerAbilitiesPacket(
                (byte) (PlayerAbilitiesPacket.FLAG_INVULNERABLE
                        | PlayerAbilitiesPacket.FLAG_ALLOW_FLYING
                        | PlayerAbilitiesPacket.FLAG_INSTANT_BREAK),
                0.05f, 0.1f
        ));
        connection.send(new UpdateViewDistancePacket(VIEW_DISTANCE));
        connection.send(new PlayerPositionAndLookPacket(1, spawn, Vec.ZERO, 0f, 0f, RelativeFlags.NONE));
        connection.send(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0));
        connection.send(new UpdateViewPositionPacket(0, 0));
        connection.send(new ChunkBatchStartPacket());
        ChunkRange.chunksInRange(0, 0, VIEW_DISTANCE, (chunkX, chunkZ) -> connection.send(flatWorld.chunk(chunkX, chunkZ)));
        connection.send(new ChunkBatchFinishedPacket(ChunkRange.chunksCount(VIEW_DISTANCE)));
    }

    private static final class Connection {
        private final SocketChannel channel;
        private final Registries registries;
        private final NetworkBuffer readBuffer;
        private ConnectionState serverState = ConnectionState.STATUS;

        private Connection(SocketChannel channel, Registries registries) {
            this.channel = channel;
            this.registries = registries;
            this.readBuffer = NetworkBuffer.resizableBuffer(4096, registries);
        }

        private void send(SendablePacket packet) {
            ConnectionState previousState = serverState;
            ServerPacket serverPacket = SendablePacket.extractServerPacket(previousState, packet);
            if (serverPacket == null) throw new IllegalArgumentException("Unsupported packet: " + packet);
            serverState = PacketVanilla.nextServerState(serverPacket, serverState);
            NetworkBuffer buffer = NetworkBuffer.resizableBuffer(1024, registries);
            PacketWriting.writeFramedPacket(buffer, previousState, serverPacket, 0);
            try {
                while (!buffer.writeChannel(channel)) Thread.onSpinWait();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private record FlatWorld(byte[] chunkData, Map<HeightmapType, long[]> heightmaps, LightData lightData) {
        private static final Generator GENERATOR = unit -> {
            unit.modifier().fillBiome(Biome.PLAINS);
            unit.modifier().fillHeight(MIN_Y, GROUND_Y, Block.DIRT);
            unit.modifier().fillHeight(MIN_Y, MIN_Y + 1, Block.BEDROCK);
            unit.modifier().fillHeight(GROUND_Y, GROUND_Y + 1, Block.GRASS_BLOCK);
        };

        private static FlatWorld create(Registries registries) {
            GeneratorImpl.GenSection[] genSections = new GeneratorImpl.GenSection[SECTION_COUNT];
            for (int i = 0; i < SECTION_COUNT; i++) {
                Section section = new Section();
                genSections[i] = new GeneratorImpl.GenSection(section.blockPalette(), section.biomePalette());
            }
            GENERATOR.generate(GeneratorImpl.chunk(registries.biome(), genSections, 0, MIN_Y / 16, 0));

            final NetworkBuffer.Type<ChunkData.Section> serializer = ChunkData.Section.networkType(registries.biome().size());
            byte[] data = NetworkBuffer.makeArray(buffer -> {
                for (GeneratorImpl.GenSection section : genSections) {
                    final int blockCount = section.blocks().count();
                    buffer.write(serializer, new ChunkData.Section(
                            (short) blockCount,
                            (short) (blockCount > 0 ? 1 : 0),  // fluid count (26.1)
                            section.blocks(), section.biomes()
                    ));
                }
            }, registries);

            return new FlatWorld(data, createHeightmaps(), fullBrightLight());
        }

        private ChunkDataPacket chunk(int chunkX, int chunkZ) {
            return new ChunkDataPacket(chunkX, chunkZ,
                    new ChunkData(heightmaps, chunkData, Map.of()),
                    lightData
            );
        }

        private static Map<HeightmapType, long[]> createHeightmaps() {
            short[] heights = new short[16 * 16];
            Arrays.fill(heights, (short) (GROUND_Y - (MIN_Y - 1)));
            long[] packed = HeightmapType.encode(heights, MathUtils.bitsToRepresent(HEIGHT));
            return Map.of(
                    HeightmapType.MOTION_BLOCKING, packed,
                    HeightmapType.WORLD_SURFACE, packed
            );
        }

        private static LightData fullBrightLight() {
            BitSet mask = new BitSet();
            mask.set(1, SECTION_COUNT + 1);
            byte[] full = new byte[2048];
            Arrays.fill(full, (byte) 0xFF);
            return new LightData(mask, new BitSet(), new BitSet(), mask,
                    Collections.nCopies(SECTION_COUNT, full), List.of());
        }
    }
}
