package net.minestom.server.extras.bungee.messaging;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * BungeeCord protocol interface.
 * <p>
 * This interface contains the protocol information for the BungeeCord messaging system.
 * It includes the channel name, data types, and message types.
 * <p>
 * The protocol is used to communicate between the server and BungeeCord proxy.
 */
interface BungeeProtocol {
    Logger LOGGER = LoggerFactory.getLogger(BungeeProtocol.class);
    String CHANNEL = "BungeeCord"; // aka bungeecord:main
    NetworkBuffer.Type<List<String>> CSV_TYPE = NetworkBuffer.STRING_IO_UTF8.transform(
            string -> List.of(string.split(",")),
            stringList -> String.join(",", stringList)
    );
    // This type is awful, just to "save" 4 bytes.
    NetworkBuffer.Type<UUID> UUID_TYPE = NetworkBuffer.STRING_IO_UTF8
            .transform((string) -> new UUID(Long.parseUnsignedLong(string.substring(0, 16), 16),
                            Long.parseUnsignedLong(string.substring(16, 32), 16)
                    ),
                    uuid -> Long.toString(uuid.getMostSignificantBits()) + uuid.getLeastSignificantBits());
    NetworkBuffer.Type<byte[]> SHORT_BYTE_ARRAY_TYPE = new NetworkBuffer.Type<>() {
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

    // Reads the message from the buffer and checks if there are any leftover bytes
    static <T extends BungeeMessage> T read(NetworkBuffer buffer, NetworkBuffer.Type<T> type) {
        final T read = buffer.read(type);
        final long readableBytes = buffer.readableBytes();
        if (readableBytes > 0) {
            LOGGER.warn("`{}` message not fully read! {} bytes left over.", read.getClass().getName(), readableBytes);
        }
        return read;
    }

    enum Type {
        Connect(BungeeRequest.Connect.SERIALIZER, BungeeResponse.Connect.SERIALIZER),
        ConnectOther(BungeeRequest.ConnectOther.SERIALIZER, BungeeResponse.ConnectOther.SERIALIZER),
        IP(BungeeRequest.IP.SERIALIZER, BungeeResponse.IP.SERIALIZER),
        IPOther(BungeeRequest.IPOther.SERIALIZER, BungeeResponse.IPOther.SERIALIZER),
        PlayerCount(BungeeRequest.PlayerCount.SERIALIZER, BungeeResponse.PlayerCount.SERIALIZER),
        PlayerList(BungeeRequest.PlayerList.SERIALIZER, BungeeResponse.PlayerList.SERIALIZER),
        GetServers(BungeeRequest.GetServers.SERIALIZER, BungeeResponse.GetServers.SERIALIZER),
        Message(BungeeRequest.Message.SERIALIZER, BungeeResponse.Message.SERIALIZER),
        MessageRaw(BungeeRequest.MessageRaw.SERIALIZER, BungeeResponse.MessageRaw.SERIALIZER),
        GetServer(BungeeRequest.GetServer.SERIALIZER, BungeeResponse.GetServer.SERIALIZER),
        GetPlayerServer(BungeeRequest.GetPlayerServer.SERIALIZER, BungeeResponse.GetPlayerServer.SERIALIZER),
        UUID(BungeeRequest.UUID.SERIALIZER, BungeeResponse.UUID.SERIALIZER),
        UUIDOther(BungeeRequest.UUIDOther.SERIALIZER, BungeeResponse.UUIDOther.SERIALIZER),
        ServerIp(BungeeRequest.ServerIp.SERIALIZER, BungeeResponse.ServerIp.SERIALIZER),
        KickPlayer(BungeeRequest.KickPlayer.SERIALIZER, BungeeResponse.KickPlayer.SERIALIZER),
        KickPlayerRaw(BungeeRequest.KickPlayerRaw.SERIALIZER, BungeeResponse.KickPlayerRaw.SERIALIZER),
        Forward(BungeeRequest.Forward.SERIALIZER, BungeeResponse.Forward.SERIALIZER),
        ForwardToPlayer(BungeeRequest.ForwardToPlayer.SERIALIZER, BungeeResponse.ForwardToPlayer.SERIALIZER);

        public static final NetworkBuffer.Type<Type> SERIALIZER = NetworkBuffer.STRING_IO_UTF8
                .transform(Type::valueOf, Type::name);

        private final NetworkBuffer.Type<BungeeRequest> requestSerializer;
        private final NetworkBuffer.Type<BungeeResponse> responseSerializer;

        @SuppressWarnings("unchecked")
        Type(NetworkBuffer.Type<? extends BungeeRequest> requestSerializer, NetworkBuffer.Type<? extends BungeeResponse> responseSerializer) {
            this.requestSerializer = (NetworkBuffer.Type<BungeeRequest>) requestSerializer;
            this.responseSerializer = (NetworkBuffer.Type<BungeeResponse>) responseSerializer;
        }

        // Could probably use polymorphism here, but it makes the classes have less information about the serialization
        // See the entries in the regular ClientPacket and ServerPacket not caring about the id near the data structure.
        static Type toType(BungeeMessage message) {
            return switch (message) {
                // Requests
                case BungeeRequest.Connect ignored -> Connect;
                case BungeeRequest.ConnectOther ignored -> ConnectOther;
                case BungeeRequest.IP ignored -> IP;
                case BungeeRequest.IPOther ignored -> IPOther;
                case BungeeRequest.PlayerCount ignored -> PlayerCount;
                case BungeeRequest.PlayerList ignored -> PlayerList;
                case BungeeRequest.GetServers ignored -> GetServers;
                case BungeeRequest.Message ignored -> Message;
                case BungeeRequest.MessageRaw ignored -> MessageRaw;
                case BungeeRequest.GetServer ignored -> GetServer;
                case BungeeRequest.GetPlayerServer ignored -> GetPlayerServer;
                case BungeeRequest.UUID ignored -> UUID;
                case BungeeRequest.UUIDOther ignored -> UUIDOther;
                case BungeeRequest.ServerIp ignored -> ServerIp;
                case BungeeRequest.KickPlayer ignored -> KickPlayer;
                case BungeeRequest.KickPlayerRaw ignored -> KickPlayerRaw;
                case BungeeRequest.Forward ignored -> Forward;
                case BungeeRequest.ForwardToPlayer ignored -> ForwardToPlayer;
                // Responses
                case BungeeResponse.Connect ignored -> Connect;
                case BungeeResponse.ConnectOther ignored -> ConnectOther;
                case BungeeResponse.IP ignored -> IP;
                case BungeeResponse.IPOther ignored -> IPOther;
                case BungeeResponse.PlayerCount ignored -> PlayerCount;
                case BungeeResponse.PlayerList ignored -> PlayerList;
                case BungeeResponse.GetServers ignored -> GetServers;
                case BungeeResponse.Message ignored -> Message;
                case BungeeResponse.MessageRaw ignored -> MessageRaw;
                case BungeeResponse.GetServer ignored -> GetServer;
                case BungeeResponse.GetPlayerServer ignored -> GetPlayerServer;
                case BungeeResponse.UUID ignored -> UUID;
                case BungeeResponse.UUIDOther ignored -> UUIDOther;
                case BungeeResponse.ServerIp ignored -> ServerIp;
                case BungeeResponse.KickPlayer ignored -> KickPlayer;
                case BungeeResponse.KickPlayerRaw ignored -> KickPlayerRaw;
                case BungeeResponse.Forward ignored -> Forward;
                case BungeeResponse.ForwardToPlayer ignored -> ForwardToPlayer;
            };
        }

        public NetworkBuffer.Type<BungeeResponse> responseSerializer() {
            return responseSerializer;
        }

        public NetworkBuffer.Type<BungeeRequest> requestSerializer() {
            return requestSerializer;
        }
    }
}
