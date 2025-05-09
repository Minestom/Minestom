package net.minestom.server.extras.bungee.messaging;

import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * BungeeCord response interface.
 * <p>
 * This interface holds all response data structures for the BungeeCord protocol.
 * It includes the request types, serializers, and data types.
 * <p>
 * The most common use cases are to deserialize the response from a {@link NetworkBuffer} or {@code byte[]}.
 * To do this, you can use the {@link BungeeMessage#readResponse(NetworkBuffer)} method.
 * There is also a shorthand version for events {@link BungeeMessage#readResponse(PlayerPluginMessageEvent)}
 */
public sealed interface BungeeResponse extends BungeeMessage {
    NetworkBuffer.Type<BungeeResponse> SERIALIZER = BungeeProtocol.Type.SERIALIZER
            .unionType(BungeeProtocol.Type::responseSerializer, BungeeProtocol.Type::toType);

    record Connect() implements BungeeResponse {
        public static final NetworkBuffer.Type<Connect> SERIALIZER = NetworkBufferTemplate.template(Connect::new);
    }

    record ConnectOther() implements BungeeResponse {
        public static final NetworkBuffer.Type<ConnectOther> SERIALIZER = NetworkBufferTemplate.template(ConnectOther::new);
    }

    record IP(@NotNull String ip, int port) implements BungeeResponse {
        public static final NetworkBuffer.Type<IP> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IP::ip,
                NetworkBuffer.INT, IP::port,
                IP::new
        );

        public IP {
            Check.notNull(ip, "IP cannot be null");
            Check.argCondition(port <= 0 || port > 65535, "Port must be greater than 0 and less than or equal to 65535");
        }
    }

    record IPOther(@NotNull String playerName, @NotNull String ip, int port) implements BungeeResponse {
        public static final NetworkBuffer.Type<IPOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IPOther::playerName,
                NetworkBuffer.STRING_IO_UTF8, IPOther::ip,
                NetworkBuffer.INT, IPOther::port,
                IPOther::new
        );

        public IPOther {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(ip, "IP cannot be null");
            Check.argCondition(port <= 0 || port > 65535, "Port must be greater than 0 and less than or equal to 65535");
        }
    }

    record PlayerCount(@NotNull String serverName, int playerCount) implements BungeeResponse {
        public static final NetworkBuffer.Type<PlayerCount> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerCount::serverName,
                NetworkBuffer.INT, PlayerCount::playerCount,
                PlayerCount::new
        );

        public PlayerCount {
            Check.notNull(serverName, "Server name cannot be null");
            Check.argCondition(playerCount <= 0, "Player count must be greater than 0");
        }
    }

    record PlayerList(@NotNull String serverName, @NotNull List<String> playerNameList) implements BungeeResponse {
        public static final NetworkBuffer.Type<PlayerList> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerList::serverName,
                BungeeProtocol.CSV_TYPE, PlayerList::playerNameList,
                PlayerList::new
        );

        public PlayerList {
            Check.notNull(serverName, "Server name cannot be null");
            Check.notNull(playerNameList, "Player name list cannot be null");
            playerNameList = List.copyOf(playerNameList);
        }
    }

    record GetServers(@NotNull List<String> serverNames) implements BungeeResponse {
        public static final NetworkBuffer.Type<GetServers> SERIALIZER = NetworkBufferTemplate.template(
                BungeeProtocol.CSV_TYPE, GetServers::serverNames,
                GetServers::new
        );

        public GetServers {
            Check.notNull(serverNames, "Server names cannot be null");
            serverNames = List.copyOf(serverNames);
        }
    }

    record Message() implements BungeeResponse {
        public static final NetworkBuffer.Type<Message> SERIALIZER = NetworkBufferTemplate.template(Message::new);
    }

    record MessageRaw() implements BungeeResponse {
        public static final NetworkBuffer.Type<MessageRaw> SERIALIZER = NetworkBufferTemplate.template(MessageRaw::new);
    }

    record GetServer(@NotNull String serverName) implements BungeeResponse {
        public static final NetworkBuffer.Type<GetServer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, GetServer::serverName,
                GetServer::new
        );

        public GetServer {
            Check.notNull(serverName, "Server name cannot be null");
        }
    }

    record GetPlayerServer(@NotNull String playerName, @NotNull String serverName) implements BungeeResponse {
        public static final NetworkBuffer.Type<GetPlayerServer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, GetPlayerServer::playerName,
                NetworkBuffer.STRING_IO_UTF8, GetPlayerServer::serverName,
                GetPlayerServer::new
        );

        public GetPlayerServer {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(serverName, "Server name cannot be null");
        }
    }

    record UUID(@NotNull java.util.UUID uuid) implements BungeeResponse {
        public static final NetworkBuffer.Type<UUID> SERIALIZER = NetworkBufferTemplate.template(
                BungeeProtocol.UUID_TYPE, UUID::uuid,
                UUID::new
        );

        public UUID {
            Check.notNull(uuid, "UUID cannot be null");
        }
    }

    record UUIDOther(@NotNull String playerName, @NotNull java.util.UUID uuid) implements BungeeResponse {
        public static final NetworkBuffer.Type<UUIDOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, UUIDOther::playerName,
                BungeeProtocol.UUID_TYPE, UUIDOther::uuid,
                UUIDOther::new
        );

        public UUIDOther {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(uuid, "UUID cannot be null");
        }
    }

    record ServerIp(@NotNull String serverName, @NotNull String ip, int port) implements BungeeResponse {
        public static final NetworkBuffer.Type<ServerIp> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ServerIp::serverName,
                NetworkBuffer.STRING_IO_UTF8, ServerIp::ip,
                NetworkBuffer.INT, ServerIp::port,
                ServerIp::new
        );

        public ServerIp {
            Check.notNull(serverName, "Server name cannot be null");
            Check.notNull(ip, "IP cannot be null");
            // Port is an unsigned short (so we use an int).
            Check.argCondition(port <= 0 || port > 65535, "Port must be greater than 0 and less than or equal to 65535");
        }
    }

    record KickPlayer() implements BungeeResponse {
        public static final NetworkBuffer.Type<KickPlayer> SERIALIZER = NetworkBufferTemplate.template(KickPlayer::new);
    }

    record KickPlayerRaw() implements BungeeResponse {
        public static final NetworkBuffer.Type<KickPlayerRaw> SERIALIZER = NetworkBufferTemplate.template(KickPlayerRaw::new);
    }

    record Forward(@NotNull String channel, byte @NotNull [] data) implements BungeeResponse {
        public static final NetworkBuffer.Type<Forward> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Forward::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, Forward::data,
                Forward::new
        );

        public Forward {
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            data = data.clone();
        }
    }

    record ForwardToPlayer(@NotNull String channel, byte @NotNull [] data) implements BungeeResponse {
        public static final NetworkBuffer.Type<ForwardToPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, ForwardToPlayer::data,
                ForwardToPlayer::new
        );

        public ForwardToPlayer {
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            data = data.clone();
        }
    }
}
