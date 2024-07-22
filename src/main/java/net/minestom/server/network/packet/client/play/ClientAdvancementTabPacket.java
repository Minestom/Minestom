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
        public void write(@NotNull NetworkBuffer buffer, ClientAdvancementTabPacket value) {
            buffer.writeEnum(AdvancementAction.class, value.action);
            if (value.action == AdvancementAction.OPENED_TAB) {
                assert value.tabIdentifier != null;
                buffer.write(STRING, value.tabIdentifier);
            }
        }

        @Override
        public ClientAdvancementTabPacket read(@NotNull NetworkBuffer buffer) {
            var action = buffer.readEnum(AdvancementAction.class);
            var tabIdentifier = action == AdvancementAction.OPENED_TAB ? buffer.read(STRING) : null;
            return new ClientAdvancementTabPacket(action, tabIdentifier);
        }
    };

    public ClientAdvancementTabPacket {
        if (tabIdentifier != null && tabIdentifier.length() > 256) {
            throw new IllegalArgumentException("Tab identifier too long: " + tabIdentifier.length());
        }
    }
}
