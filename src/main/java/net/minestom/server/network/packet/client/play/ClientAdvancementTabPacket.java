package net.minestom.server.network.packet.client.play;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientAdvancementTabPacket(@NotNull AdvancementAction action,
                                         @Nullable String tabIdentifier) implements ClientPacket {
    public ClientAdvancementTabPacket {
        if (tabIdentifier != null && tabIdentifier.length() > 256) {
            throw new IllegalArgumentException("Tab identifier too long: " + tabIdentifier.length());
        }
    }

    public ClientAdvancementTabPacket(@NotNull NetworkBuffer reader) {
        this(read(reader));
    }

    private ClientAdvancementTabPacket(ClientAdvancementTabPacket packet) {
        this(packet.action, packet.tabIdentifier);
    }

    private static ClientAdvancementTabPacket read(@NotNull NetworkBuffer reader) {
        var action = reader.readEnum(AdvancementAction.class);
        var tabIdentifier = action == AdvancementAction.OPENED_TAB ? reader.read(STRING) : null;
        return new ClientAdvancementTabPacket(action, tabIdentifier);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(AdvancementAction.class, action);
        if (action == AdvancementAction.OPENED_TAB) {
            assert tabIdentifier != null;
            if (tabIdentifier.length() > 256) {
                throw new IllegalArgumentException("Tab identifier cannot be longer than 256 characters.");
            }
            writer.write(STRING, tabIdentifier);
        }
    }
}
