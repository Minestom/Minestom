package net.minestom.server.network.packet.client.play;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public final class ClientAdvancementTabPacket implements ClientPacket {
    public final AdvancementAction action;
    public final String tabIdentifier;

    public ClientAdvancementTabPacket(AdvancementAction action, String tabIdentifier) {
        this.action = action;
        this.tabIdentifier = tabIdentifier;
    }

    public ClientAdvancementTabPacket(BinaryReader reader) {
        this.action = AdvancementAction.values()[reader.readVarInt()];
        if (action == AdvancementAction.OPENED_TAB) {
            this.tabIdentifier = reader.readSizedString(256);
        } else {
            this.tabIdentifier = null;
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        if (action == AdvancementAction.OPENED_TAB) {
            if (tabIdentifier.length() > 256) {
                throw new IllegalArgumentException("Tab identifier cannot be longer than 256 characters.");
            }
            writer.writeSizedString(tabIdentifier);
        }
    }
}
