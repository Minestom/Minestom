package net.minestom.server.extras.bungee.messaging;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public sealed interface BungeeRequest extends BungeeMessage {
    NetworkBuffer.Type<BungeeRequest> SERIALIZER = BungeeProtocol.Type.SERIALIZER
            .unionType(BungeeProtocol.Type::requestSerializer, BungeeProtocol.Type::toType);

    record Connect(@NotNull String serverName) implements BungeeRequest {
        public static final NetworkBuffer.Type<Connect> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Connect::serverName,
                Connect::new
        );

        public Connect {
            Check.notNull(serverName, "Server name cannot be null");
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
    }

    record IP() implements BungeeRequest {
        public static final NetworkBuffer.Type<IP> SERIALIZER = NetworkBufferTemplate.template(IP::new);
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
    }

    record PlayerCount(@NotNull String serverName) implements BungeeRequest {
        public static final NetworkBuffer.Type<PlayerCount> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerCount::serverName,
                PlayerCount::new
        );

        public PlayerCount {
            Check.notNull(serverName, "Server name cannot be null");
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
    }

    record GetServers() implements BungeeRequest {
        public static final NetworkBuffer.Type<GetServers> SERIALIZER = NetworkBufferTemplate.template(GetServers::new);
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
    }

    record GetServer() implements BungeeRequest {
        public static final NetworkBuffer.Type<GetServer> SERIALIZER = NetworkBufferTemplate.template(GetServer::new);

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
    }

    record UUID() implements BungeeRequest {
        public static final NetworkBuffer.Type<UUID> SERIALIZER = NetworkBufferTemplate.template(UUID::new);
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
    }

    record ServerIp(@NotNull String serverName) implements BungeeRequest {
        public static final NetworkBuffer.Type<ServerIp> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ServerIp::serverName,
                ServerIp::new
        );

        public ServerIp {
            Check.notNull(serverName, "Server name cannot be null");
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
    }

    record Forward(@NotNull String server, @NotNull String channel, byte @NotNull [] data) implements BungeeRequest {
        public static final NetworkBuffer.Type<Forward> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Forward::server,
                NetworkBuffer.STRING_IO_UTF8, Forward::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, Forward::data,
                Forward::new
        );

        public Forward {
            Check.notNull(server, "Server cannot be null");
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            Check.argCondition(data.length > 65535, "Data cannot be more than a 65535 in length");
            data = data.clone();
        }
    }

    record ForwardToPlayer(@NotNull String playerName, @NotNull String channel,
                           byte @NotNull [] data) implements BungeeRequest {
        public static final NetworkBuffer.Type<ForwardToPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::playerName,
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, ForwardToPlayer::data,
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
    }
}
