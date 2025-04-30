package net.minestom.server.extras.bungee.messaging;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// TODO: add javadocs
public sealed interface BungeeResponse extends BungeeProtocol {
    static byte @NotNull [] write(@NotNull BungeeResponse response) {
        return NetworkBuffer.makeArray(RESPONSE_TYPE, response);
    }

    static @NotNull BungeeResponse read(@NotNull NetworkBuffer buffer) {
        return buffer.read(RESPONSE_TYPE);
    }

    static @NotNull BungeeResponse read(byte @NotNull [] bytes) {
        return read(NetworkBuffer.wrap(bytes, 0, 0));
    }

    static @NotNull BungeeResponse read(@NotNull ClientPluginMessagePacket packet) {
        Check.argCondition(!packet.channel().equals(CHANNEL), "Channel is not the `{0}` channel!", CHANNEL);
        return read(packet.data());
    }

    static @NotNull BungeeResponse read(@NotNull PluginMessagePacket packet) {
        Check.argCondition(!packet.channel().equals(CHANNEL), "Channel is not the `{0}` channel!", CHANNEL);
        return read(packet.data());
    }

    @Override
    default @NotNull ClientPluginMessagePacket toClientPacket() {
        return new ClientPluginMessagePacket(CHANNEL, write(this));
    }

    @Override
    default @NotNull PluginMessagePacket toServerPacket() {
        return new PluginMessagePacket(CHANNEL, write(this));
    }

    record Connect() implements BungeeResponse {
        public static final NetworkBuffer.Type<Connect> SERIALIZER = NetworkBufferTemplate.template(Connect::new);

        @Override
        public @NotNull String type() {
            return "Connect";
        }
    }

    record ConnectOther() implements BungeeResponse {
        public static final NetworkBuffer.Type<ConnectOther> SERIALIZER = NetworkBufferTemplate.template(ConnectOther::new);

        @Override
        public @NotNull String type() {
            return "ConnectOther";
        }
    }

    record IP(@NotNull String ip, int port) implements BungeeResponse {
        public static final NetworkBuffer.Type<IP> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IP::ip,
                NetworkBuffer.UNSIGNED_SHORT, IP::port,
                IP::new
        );

        public IP {
            Check.notNull(ip, "IP cannot be null");
            Check.argCondition(port <= 0 || port > 65535, "Port must be greater than 0 and less than or equal to 65535");
        }

        @Override
        public @NotNull String type() {
            return "IP";
        }

    }

    record IPOther(@NotNull String playerName, @NotNull String ip, int port) implements BungeeResponse {
        public static final NetworkBuffer.Type<IPOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IPOther::playerName,
                NetworkBuffer.STRING_IO_UTF8, IPOther::ip,
                NetworkBuffer.UNSIGNED_SHORT, IPOther::port,
                IPOther::new
        );

        public IPOther {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(ip, "IP cannot be null");
            Check.argCondition(port <= 0 || port > 65535, "Port must be greater than 0 and less than or equal to 65535");
        }

        @Override
        public @NotNull String type() {
            return "IPOther";
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

        @Override
        public @NotNull String type() {
            return "PlayerCount";
        }
    }

    record PlayerList(@NotNull String serverName, @NotNull List<String> playerNameList) implements BungeeResponse {
        public static final NetworkBuffer.Type<PlayerList> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerList::serverName,
                CSV_TYPE, PlayerList::playerNameList,
                PlayerList::new
        );

        public PlayerList {
            Check.notNull(serverName, "Server name cannot be null");
            Check.notNull(playerNameList, "Player name list cannot be null");
            playerNameList = List.copyOf(playerNameList);
        }

        @Override
        public @NotNull String type() {
            return "PlayerList";
        }
    }

    record GetServers(@NotNull List<String> serverNames) implements BungeeResponse {
        public static final NetworkBuffer.Type<GetServers> SERIALIZER = NetworkBufferTemplate.template(
                CSV_TYPE, GetServers::serverNames,
                GetServers::new
        );

        public GetServers {
            Check.notNull(serverNames, "Server names cannot be null");
            serverNames = List.copyOf(serverNames);
        }

        @Override
        public @NotNull String type() {
            return "GetServers";
        }
    }

    record Message() implements BungeeResponse {
        public static final NetworkBuffer.Type<Message> SERIALIZER = NetworkBufferTemplate.template(Message::new);

        @Override
        public @NotNull String type() {
            return "Message";
        }
    }

    record MessageRaw() implements BungeeResponse {
        public static final NetworkBuffer.Type<MessageRaw> SERIALIZER = NetworkBufferTemplate.template(MessageRaw::new);

        @Override
        public @NotNull String type() {
            return "MessageRaw";
        }
    }

    record GetServer(@NotNull String serverName) implements BungeeResponse {
        public static final NetworkBuffer.Type<GetServer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, GetServer::serverName,
                GetServer::new
        );

        public GetServer {
            Check.notNull(serverName, "Server name cannot be null");
        }

        @Override
        public @NotNull String type() {
            return "GetServer";
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

        @Override
        public @NotNull String type() {
            return "GetPlayerServer";
        }
    }

    record UUID(@NotNull java.util.UUID uuid) implements BungeeResponse {
        public static final NetworkBuffer.Type<UUID> SERIALIZER = NetworkBufferTemplate.template(
                UUID_TYPE, UUID::uuid,
                UUID::new
        );

        public UUID {
            Check.notNull(uuid, "UUID cannot be null");
        }

        @Override
        public @NotNull String type() {
            return "UUID";
        }
    }

    record UUIDOther(@NotNull String playerName, @NotNull java.util.UUID uuid) implements BungeeResponse {
        public static final NetworkBuffer.Type<UUIDOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, UUIDOther::playerName,
                UUID_TYPE, UUIDOther::uuid,
                UUIDOther::new
        );

        public UUIDOther {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(uuid, "UUID cannot be null");
        }

        @Override
        public @NotNull String type() {
            return "UUIDOther";
        }
    }

    record ServerIp(@NotNull String serverName, @NotNull String ip, int port) implements BungeeResponse {
        public static final NetworkBuffer.Type<ServerIp> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ServerIp::serverName,
                NetworkBuffer.STRING_IO_UTF8, ServerIp::ip,
                NetworkBuffer.UNSIGNED_SHORT, ServerIp::port,
                ServerIp::new
        );

        public ServerIp {
            Check.notNull(serverName, "Server name cannot be null");
            Check.notNull(ip, "IP cannot be null");
            // Port is an unsigned short (so we use an int).
            Check.argCondition(port <= 0 || port > 65535, "Port must be greater than 0 and less than or equal to 65535");
        }

        @Override
        public @NotNull String type() {
            return "ServerIp";
        }
    }

    record KickPlayer() implements BungeeResponse {
        public static final NetworkBuffer.Type<KickPlayer> SERIALIZER = NetworkBufferTemplate.template(KickPlayer::new);

        @Override
        public @NotNull String type() {
            return "KickPlayer";
        }
    }

    record KickPlayerRaw() implements BungeeResponse {
        public static final NetworkBuffer.Type<KickPlayerRaw> SERIALIZER = NetworkBufferTemplate.template(KickPlayerRaw::new);

        @Override
        public @NotNull String type() {
            return "KickPlayerRaw";
        }
    }

    record Forward(@NotNull String channel, byte @NotNull [] data) implements BungeeResponse {
        public static final NetworkBuffer.Type<Forward> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Forward::channel,
                SHORT_FIXED_BYTE_ARRAY_TYPE, Forward::data,
                Forward::new
        );

        public Forward {
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            data = data.clone();
        }

        @Override
        public @NotNull String type() {
            return "Forward";
        }
    }

    record ForwardToPlayer(@NotNull String channel, byte @NotNull [] data) implements BungeeResponse {
        public static final NetworkBuffer.Type<ForwardToPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::channel,
                SHORT_FIXED_BYTE_ARRAY_TYPE, ForwardToPlayer::data,
                ForwardToPlayer::new
        );

        public ForwardToPlayer {
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            data = data.clone();
        }

        @Override
        public @NotNull String type() {
            return "ForwardToPlayer";
        }
    }
}
