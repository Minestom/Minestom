package net.minestom.server.network.packet.client.play;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientAdvancementTabPacket(AdvancementAction action,
                                         @Nullable String tabIdentifier) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientAdvancementTabPacket> SERIALIZER = NetworkBuffer.Tagged(
            NetworkBuffer.Enum(AdvancementAction.class), ClientAdvancementTabPacket::action,
            Map.of(
                    AdvancementAction.OPENED_TAB, NetworkBufferTemplate.template(
                            STRING, ClientAdvancementTabPacket::tabIdentifier,
                            tabIdentifier -> new ClientAdvancementTabPacket(AdvancementAction.OPENED_TAB, tabIdentifier))
            ),
            NetworkBufferTemplate.template(new ClientAdvancementTabPacket(AdvancementAction.CLOSED_SCREEN, null))
    );

    public ClientAdvancementTabPacket {
        if (tabIdentifier != null && tabIdentifier.length() > 256) {
            throw new IllegalArgumentException("Tab identifier too long: " + tabIdentifier.length());
        }
    }
}
