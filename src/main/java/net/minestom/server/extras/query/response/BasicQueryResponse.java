package net.minestom.server.extras.query.response;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.STRING_TERMINATED;

/**
 * A basic query response containing a fixed set of responses.
 */
public record BasicQueryResponse(String motd, String gameType,
                                 String map,
                                 String numPlayers, String maxPlayers,
                                 short port, String address) {
    /**
     * Creates a new basic query response with pre-filled default values.
     */
    public BasicQueryResponse() {
        this(
                "A Minestom Server",
                "SMP",
                "world",
                String.valueOf(MinecraftServer.getConnectionManager().getOnlinePlayerCount()),
                "9999",
                (short) MinecraftServer.getServer().getPort(),
                Objects.requireNonNullElse(MinecraftServer.getServer().getAddress(), "")
        );
    }

    public static final NetworkBuffer.Type<BasicQueryResponse> SERIALIZER = NetworkBufferTemplate.template(
            STRING_TERMINATED, BasicQueryResponse::motd,
            STRING_TERMINATED, BasicQueryResponse::gameType,
            STRING_TERMINATED, BasicQueryResponse::map,
            STRING_TERMINATED, BasicQueryResponse::numPlayers,
            STRING_TERMINATED, BasicQueryResponse::maxPlayers,
            SHORT, BasicQueryResponse::port, // TODO little endian?
            STRING_TERMINATED, BasicQueryResponse::address,
            BasicQueryResponse::new
    );
}
