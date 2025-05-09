package net.minestom.server.network.packet.client.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClientTestInstanceBlockActionPacket(
        @NotNull Point blockPosition,
        @NotNull Action action,
        @NotNull Data data
) implements ClientPacket {

    public static final @NotNull NetworkBuffer.Type<ClientTestInstanceBlockActionPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.BLOCK_POSITION, ClientTestInstanceBlockActionPacket::blockPosition,
            Action.NETWORK_TYPE, ClientTestInstanceBlockActionPacket::action,
            Data.NETWORK_TYPE, ClientTestInstanceBlockActionPacket::data,
            ClientTestInstanceBlockActionPacket::new);

    public record Data(
            @Nullable String test,
            @NotNull Point size,
            int rotation,
            boolean ignoreEntities,
            @NotNull Status status,
            @Nullable Component errorMessage
    ) {
        public static final @NotNull NetworkBuffer.Type<Data> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.STRING.optional(), Data::test,
                NetworkBuffer.BLOCK_POSITION, Data::size,
                NetworkBuffer.VAR_INT, Data::rotation,
                NetworkBuffer.BOOLEAN, Data::ignoreEntities,
                Status.NETWORK_TYPE, Data::status,
                NetworkBuffer.COMPONENT.optional(), Data::errorMessage,
                Data::new);
    }

    public enum Action {
        INIT,
        QUERY,
        SET,
        RESET,
        SAVE,
        EXPORT,
        RUN;

        public static final NetworkBuffer.Type<Action> NETWORK_TYPE = NetworkBuffer.Enum(Action.class);
    }

    public enum Status {
        CLEARED,
        RUNNING,
        FINISHED;

        public static final NetworkBuffer.Type<Status> NETWORK_TYPE = NetworkBuffer.Enum(Status.class);
    }
}
