package net.minestom.server.extras.bungee.messaging;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

sealed interface BungeeProtocol permits BungeeRequest, BungeeResponse {
    @NotNull String type();

    @NotNull ClientPluginMessagePacket toClientPacket();

    @NotNull PluginMessagePacket toServerPacket();
    default void send(@NotNull PlayerConnection connection) {
        connection.sendPacket(toServerPacket());
    }

    default void send(@NotNull Audience audience) {
        PacketSendingUtils.sendPacket(audience, toServerPacket());
    }

    // Protocol
    String CHANNEL = "BungeeCord";
    @SuppressWarnings("unchecked") NetworkBuffer.Type<BungeeRequest> REQUEST_TYPE = NetworkBuffer.STRING_IO_UTF8
            .unionType(string-> (NetworkBuffer.Type<BungeeRequest>) requestSerializers(string), BungeeProtocol::type);
    @SuppressWarnings("unchecked") NetworkBuffer.Type<BungeeResponse> RESPONSE_TYPE = NetworkBuffer.STRING_IO_UTF8
            .unionType(string-> (NetworkBuffer.Type<BungeeResponse>) responseSerializers(string), BungeeProtocol::type);
    NetworkBuffer.Type<List<String>> CSV_TYPE = NetworkBuffer.STRING_IO_UTF8.transform(
            string -> List.of(string.split(",")),
            stringList -> String.join(",", stringList)
    );
    NetworkBuffer.Type<UUID> UUID_TYPE = NetworkBuffer.STRING_IO_UTF8
            .transform(java.util.UUID::fromString, java.util.UUID::toString);
    NetworkBuffer.Type<byte[]> SHORT_FIXED_BYTE_ARRAY_TYPE = new NetworkBuffer.Type<>() {
        // Reminder that they use big endian for IO, so we should be good as the protocol uses it too
        @Override
        public void write(@NotNull NetworkBuffer buffer, byte[] value) {
            final int length = value.length;
            Check.argCondition(length > 65535, "Value too long");
            buffer.write(NetworkBuffer.UNSIGNED_SHORT, length);
            buffer.write(NetworkBuffer.FixedRawBytes(length), value);
        }

        @Override
        public byte[] read(@NotNull NetworkBuffer buffer) {
            final int length = buffer.read(NetworkBuffer.UNSIGNED_SHORT);
            Check.stateCondition(length > 65535, "Value too long");
            Check.stateCondition(buffer.readableBytes() > length, "Value too long to read");
            return buffer.read(NetworkBuffer.FixedRawBytes(length));
        }
    };

    // TODO Enum? (Maybe will fix weird generic issue)
    static NetworkBuffer.Type<? extends BungeeRequest> requestSerializers(@NotNull String type) {
        return switch (type) {
                    case "Connect" -> BungeeRequest.Connect.SERIALIZER;
                    case "ConnectOther" -> BungeeRequest.ConnectOther.SERIALIZER;
                    case "IP" -> BungeeRequest.IP.SERIALIZER;
                    case "IPOther" -> BungeeRequest.IPOther.SERIALIZER;
                    case "PlayerCount" -> BungeeRequest.PlayerCount.SERIALIZER;
                    case "PlayerList" -> BungeeRequest.PlayerList.SERIALIZER;
                    case "GetServers" -> BungeeRequest.GetServers.SERIALIZER;
                    case "Message" -> BungeeRequest.Message.SERIALIZER;
                    case "MessageRaw" -> BungeeRequest.MessageRaw.SERIALIZER;
                    case "GetServer" -> BungeeRequest.GetServer.SERIALIZER;
                    case "GetPlayerServer" -> BungeeRequest.GetPlayerServer.SERIALIZER;
                    case "UUID" -> BungeeRequest.UUID.SERIALIZER;
                    case "UUIDOther" -> BungeeRequest.UUIDOther.SERIALIZER;
                    case "ServerIp" -> BungeeRequest.ServerIp.SERIALIZER;
                    case "KickPlayer" -> BungeeRequest.KickPlayer.SERIALIZER;
                    case "KickPlayerRaw" -> BungeeRequest.KickPlayerRaw.SERIALIZER;
                    case "Forward" -> BungeeRequest.Forward.SERIALIZER;
                    case "ForwardToPlayer" -> BungeeRequest.ForwardToPlayer.SERIALIZER;
                    default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    // TODO Enum? (Maybe will fix weird generic issue)
    static NetworkBuffer.Type<? extends BungeeResponse> responseSerializers(@NotNull String type) {
        return switch (type) {
            case "Connect" -> BungeeResponse.Connect.SERIALIZER;
            case "ConnectOther" -> BungeeResponse.ConnectOther.SERIALIZER;
            case "IP" -> BungeeResponse.IP.SERIALIZER;
            case "IPOther" -> BungeeResponse.IPOther.SERIALIZER;
            case "PlayerCount" -> BungeeResponse.PlayerCount.SERIALIZER;
            case "PlayerList" -> BungeeResponse.PlayerList.SERIALIZER;
            case "GetServers" -> BungeeResponse.GetServers.SERIALIZER;
            case "Message" -> BungeeResponse.Message.SERIALIZER;
            case "MessageRaw" -> BungeeResponse.MessageRaw.SERIALIZER;
            case "GetServer" -> BungeeResponse.GetServer.SERIALIZER;
            case "GetPlayerServer" -> BungeeResponse.GetPlayerServer.SERIALIZER;
            case "UUID" -> BungeeResponse.UUID.SERIALIZER;
            case "UUIDOther" -> BungeeResponse.UUIDOther.SERIALIZER;
            case "ServerIp" -> BungeeResponse.ServerIp.SERIALIZER;
            case "KickPlayer" -> BungeeResponse.KickPlayer.SERIALIZER;
            case "KickPlayerRaw" -> BungeeResponse.KickPlayerRaw.SERIALIZER;
            case "Forward" -> BungeeResponse.Forward.SERIALIZER;
            case "ForwardToPlayer" -> BungeeResponse.ForwardToPlayer.SERIALIZER;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}
