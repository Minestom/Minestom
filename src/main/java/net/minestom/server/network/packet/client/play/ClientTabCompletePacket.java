package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientTabCompletePacket extends ClientPlayPacket {

    public int transactionId;
    public String text = "";

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.transactionId = reader.readVarInt();
        this.text = reader.readSizedString(Short.MAX_VALUE);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        writer.writeSizedString(text);
    }
}
