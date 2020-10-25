package net.minestom.server.network.packet.client.play;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientAdvancementTabPacket extends ClientPlayPacket {

    public AdvancementAction action;
    public String tabIdentifier;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.action = AdvancementAction.values()[reader.readVarInt()];

        if (action == AdvancementAction.OPENED_TAB) {
            this.tabIdentifier = reader.readSizedString();
        }
    }

}
