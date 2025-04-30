package net.minestom.server.extras.bungee.messaging;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

// TODO: add javadocs
public sealed interface BungeeRequest extends BungeeProtocol {

    static byte @NotNull [] write(@NotNull BungeeRequest request) {
        return NetworkBuffer.makeArray(REQUEST_TYPE, request);
    }

    static @NotNull BungeeRequest read(@NotNull NetworkBuffer buffer) {
        return buffer.read(REQUEST_TYPE);
    }

    static @NotNull BungeeRequest read(byte @NotNull [] bytes) {
        return read(NetworkBuffer.wrap(bytes, 0, 0));
    }

    static @NotNull BungeeRequest read(@NotNull ClientPluginMessagePacket packet) {
        Check.argCondition(!packet.channel().equals(CHANNEL), "Channel is not the `{0}` channel!", CHANNEL);
        return read(packet.data());
    }

    static @NotNull BungeeRequest read(@NotNull PluginMessagePacket packet) {
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

    record Connect(@NotNull String serverName) implements BungeeRequest {
        public static final NetworkBuffer.Type<Connect> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Connect::serverName,
                Connect::new
        );

        public Connect {
            Check.notNull(serverName, "Server name cannot be null");
        }
        @Override
        public @NotNull String type() {
            return "Connect";
        }
    }

    record ConnectOther(@NotNull String playerName, @NotNull String serverName) implements BungeeRequest {
        public static final NetworkBuffer.Type<ConnectOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ConnectOther::playerName,
                NetworkBuffer.STRING_IO_UTF8, ConnectOther::serverName,
                ConnectOther::new
        );

        public ConnectOther {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(serverName, "Server name cannot be null");
        }

        public ConnectOther(@NotNull Pointered player, @NotNull String serverName) {
            this(player.get(Identity.NAME).orElseThrow(), serverName);
        }

        @Override
        public @NotNull String type() {
            return "ConnectOther";
        }
    }

    record IP() implements BungeeRequest {
        public static final NetworkBuffer.Type<IP> SERIALIZER = NetworkBufferTemplate.template(IP::new);

        @Override
        public @NotNull String type() {
            return "IP";
        }
    }

    record IPOther(@NotNull String playerName) implements BungeeRequest {
        public static final NetworkBuffer.Type<IPOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IPOther::playerName,
                IPOther::new
        );

        public IPOther {
            Check.notNull(playerName, "Player name cannot be null");
        }

        public IPOther(@NotNull Pointered player) {
            this(player.get(Identity.NAME).orElseThrow());
        }

        @Override
        public @NotNull String type() {
            return "IPOther";
        }
    }

    record PlayerCount(@NotNull String serverName) implements BungeeRequest {
        public static final NetworkBuffer.Type<PlayerCount> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerCount::serverName,
                PlayerCount::new
        );

        public PlayerCount {
            Check.notNull(serverName, "Server name cannot be null");
        }

        @Override
        public @NotNull String type() {
            return "PlayerCount";
        }
    }

    record PlayerList(@NotNull String serverName) implements BungeeRequest {
        public static final NetworkBuffer.Type<PlayerList> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerList::serverName,
                PlayerList::new
        );

        public PlayerList {
            Check.notNull(serverName, "Server name cannot be null");
        }

        @Override
        public @NotNull String type() {
            return "PlayerList";
        }
    }

    record GetServers() implements BungeeRequest {
        public static final NetworkBuffer.Type<GetServers> SERIALIZER = NetworkBufferTemplate.template(GetServers::new);

        @Override
        public @NotNull String type() {
            return "GetServers";
        }
    }

    record Message(@NotNull String playerName, @NotNull String message) implements BungeeRequest {
        public static final NetworkBuffer.Type<Message> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Message::playerName,
                NetworkBuffer.STRING_IO_UTF8, Message::message,
                Message::new
        );

        public Message {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(message, "Message cannot be null");
        }

        public Message(@NotNull Pointered player, @NotNull String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        @Override
        public @NotNull String type() {
            return "Message";
        }
    }

    record MessageRaw(@NotNull String playerName, @NotNull String message) implements BungeeRequest {
        public static final NetworkBuffer.Type<MessageRaw> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, MessageRaw::playerName,
                NetworkBuffer.STRING_IO_UTF8, MessageRaw::message,
                MessageRaw::new
        );

        public MessageRaw {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(message, "Message cannot be null");
        }

        public MessageRaw(@NotNull Pointered player, @NotNull String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        public MessageRaw(@NotNull String playerName, @NotNull Component message) {
            this(playerName, GsonComponentSerializer.gson().serialize(message));
        }

        public MessageRaw(@NotNull Pointered player, @NotNull Component message) {
            this(player.get(Identity.NAME).orElseThrow(), GsonComponentSerializer.gson().serialize(message));
        }

        @Override
        public @NotNull String type() {
            return "MessageRaw";
        }
    }

    record GetServer() implements BungeeRequest {
        public static final NetworkBuffer.Type<GetServer> SERIALIZER = NetworkBufferTemplate.template(GetServer::new);

        @Override
        public @NotNull String type() {
            return "GetServer";
        }
    }

    record GetPlayerServer(@NotNull String playerName) implements BungeeRequest {
        public static final NetworkBuffer.Type<GetPlayerServer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, GetPlayerServer::playerName,
                GetPlayerServer::new
        );

        public GetPlayerServer {
            Check.notNull(playerName, "Player name cannot be null");
        }

        public GetPlayerServer(@NotNull Pointered player) {
            this(player.get(Identity.NAME).orElseThrow());
        }

        @Override
        public @NotNull String type() {
            return "GetPlayerServer";
        }
    }

    record UUID() implements BungeeRequest {
        public static final NetworkBuffer.Type<UUID> SERIALIZER = NetworkBufferTemplate.template(UUID::new);

        @Override
        public @NotNull String type() {
            return "UUID";
        }
    }

    record UUIDOther(@NotNull String playerName) implements BungeeRequest {
        public static final NetworkBuffer.Type<UUIDOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, UUIDOther::playerName,
                UUIDOther::new
        );

        public UUIDOther {
            Check.notNull(playerName, "Player name cannot be null");
        }

        public UUIDOther(@NotNull Pointered player) {
            this(player.get(Identity.NAME).orElseThrow());
        }

        @Override
        public @NotNull String type() {
            return "UUIDOther";
        }
    }

    record ServerIp(@NotNull String serverName) implements BungeeRequest {
        public static final NetworkBuffer.Type<ServerIp> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ServerIp::serverName,
                ServerIp::new
        );

        public ServerIp {
            Check.notNull(serverName, "Server name cannot be null");
        }

        @Override
        public @NotNull String type() {
            return "ServerIp";
        }
    }

    record KickPlayer(@NotNull String playerName, @NotNull String reason) implements BungeeRequest {
        public static final NetworkBuffer.Type<KickPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, KickPlayer::playerName,
                NetworkBuffer.STRING_IO_UTF8, KickPlayer::reason,
                KickPlayer::new
        );

        public KickPlayer {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(reason, "Reason cannot be null");
        }

        public KickPlayer(@NotNull Pointered player, @NotNull String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        @Override
        public @NotNull String type() {
            return "KickPlayer";
        }
    }

    record KickPlayerRaw(@NotNull String playerName, @NotNull String reason) implements BungeeRequest {
        public static final NetworkBuffer.Type<KickPlayerRaw> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, KickPlayerRaw::playerName,
                NetworkBuffer.STRING_IO_UTF8, KickPlayerRaw::reason,
                KickPlayerRaw::new
        );

        public KickPlayerRaw {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(reason, "Reason cannot be null");
        }

        public KickPlayerRaw(@NotNull Pointered player, @NotNull String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        public KickPlayerRaw(@NotNull String playerName, @NotNull Component message) {
            this(playerName, GsonComponentSerializer.gson().serialize(message));
        }

        public KickPlayerRaw(@NotNull Pointered player, @NotNull Component message) {
            this(player.get(Identity.NAME).orElseThrow(), GsonComponentSerializer.gson().serialize(message));
        }

        @Override
        public @NotNull String type() {
            return "KickPlayerRaw";
        }
    }

    record Forward(@NotNull String server, @NotNull String channel, byte @NotNull [] data) implements BungeeRequest {
        public static final NetworkBuffer.Type<Forward> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Forward::server,
                NetworkBuffer.STRING_IO_UTF8, Forward::channel,
                SHORT_FIXED_BYTE_ARRAY_TYPE, Forward::data,
                Forward::new
        );

        public Forward {
            Check.notNull(server, "Server cannot be null");
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            Check.argCondition(data.length > 65535, "Data cannot be more than a 65535 in length");
            data = data.clone();
        }

        @Override
        public @NotNull String type() {
            return "Forward";
        }
    }

    record ForwardToPlayer(@NotNull String playerName, @NotNull String channel,
                           byte @NotNull [] data) implements BungeeRequest {
        public static final NetworkBuffer.Type<ForwardToPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::playerName,
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::channel,
                SHORT_FIXED_BYTE_ARRAY_TYPE, ForwardToPlayer::data,
                ForwardToPlayer::new
        );

        public ForwardToPlayer {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            Check.argCondition(data.length > 65535, "Data cannot be more than a 65535 in length");
            data = data.clone();
        }

        public ForwardToPlayer(@NotNull Pointered player, @NotNull String channel, byte @NotNull [] data) {
            this(player.get(Identity.NAME).orElseThrow(), channel, data);
        }

        @Override
        public @NotNull String type() {
            return "ForwardToPlayer";
        }
    }
}
