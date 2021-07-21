package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientEditBookPacket extends ClientPlayPacket {

    public int slot;
    public String[] pages;
    public String title;

    public ClientEditBookPacket(int slot, String[] pages, String title) {
        this.slot = slot;
        this.pages = pages;
        this.title = title;
    }

    public ClientEditBookPacket() {
        this(0, null, "");
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.slot = reader.readVarInt();
        final int pageLength = reader.readVarInt();
        this.pages = new String[pageLength];
        for (int i = 0; i < pageLength; i++) {
            pages[i] = reader.readSizedString(8192);
        }
        this.title = reader.readBoolean() ? reader.readSizedString(128) : null;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(slot);
        writer.writeStringArray(pages);
        final boolean hasTitle = title != null;
        writer.writeBoolean(hasTitle);
        if (hasTitle) {
            writer.writeSizedString(title);
        }
    }
}
