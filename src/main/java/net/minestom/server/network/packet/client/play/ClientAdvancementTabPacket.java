package net.minestom.server.network.packet.client.play;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClientAdvancementTabPacket(@NotNull AdvancementAction action,
                                         @Nullable String tabIdentifier) implements ClientPacket {
    public ClientAdvancementTabPacket(BinaryReader reader) {
        this(read(reader));
    }

    private ClientAdvancementTabPacket(ClientAdvancementTabPacket packet) {
        this(packet.action, packet.tabIdentifier);
    }

    private static ClientAdvancementTabPacket read(BinaryReader reader) {
        var action = AdvancementAction.values()[reader.readVarInt()];
        var tabIdentifier = action == AdvancementAction.OPENED_TAB ? reader.readSizedString(256) : null;
        return new ClientAdvancementTabPacket(action, tabIdentifier);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        if (action == AdvancementAction.OPENED_TAB) {
            assert tabIdentifier != null;
            if (tabIdentifier.length() > 256) {
                throw new IllegalArgumentException("Tab identifier cannot be longer than 256 characters.");
            }
            writer.writeSizedString(tabIdentifier);
        }
    }
}
