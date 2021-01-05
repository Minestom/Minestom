package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientQueryEntityNbtPacket extends ClientPlayPacket {

    public int transactionId;
    public int entityId;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.transactionId = reader.readVarInt();
        this.entityId = reader.readVarInt();
    }
}
