package net.minestom.server.network.packet.client.play;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientAdvancementTabPacket extends ClientPlayPacket {

    public AdvancementAction action = AdvancementAction.OPENED_TAB;
    public String tabIdentifier = "";

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.action = AdvancementAction.values()[reader.readVarInt()];

        if (action == AdvancementAction.OPENED_TAB) {
            this.tabIdentifier = reader.readSizedString(256);
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());

        if(action == AdvancementAction.OPENED_TAB) {
            if(tabIdentifier.length() > 256) {
                throw new IllegalArgumentException("Tab identifier cannot be longer than 256 characters.");
            }
            writer.writeSizedString(tabIdentifier);
        }
    }
}
