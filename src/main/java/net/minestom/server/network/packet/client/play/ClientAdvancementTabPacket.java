package net.minestom.server.network.packet.client.play;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientAdvancementTabPacket(@NotNull AdvancementAction action,
                                         @Nullable String tabIdentifier) implements ClientPacket {
    public static NetworkBuffer.Type<ClientAdvancementTabPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, ClientAdvancementTabPacket value) {
            writer.writeEnum(AdvancementAction.class, value.action);
            if (value.action == AdvancementAction.OPENED_TAB) {
                assert value.tabIdentifier != null;
                writer.write(STRING, value.tabIdentifier);
            }
        }

        @Override
        public ClientAdvancementTabPacket read(@NotNull NetworkBuffer reader) {
            var action = reader.readEnum(AdvancementAction.class);
            var tabIdentifier = action == AdvancementAction.OPENED_TAB ? reader.read(STRING) : null;
            return new ClientAdvancementTabPacket(action, tabIdentifier);
        }
    };

    public ClientAdvancementTabPacket {
        if (tabIdentifier != null && tabIdentifier.length() > 256) {
            throw new IllegalArgumentException("Tab identifier too long: " + tabIdentifier.length());
        }
    }
}
