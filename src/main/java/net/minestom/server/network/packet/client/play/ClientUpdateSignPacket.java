package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientUpdateSignPacket extends ClientPlayPacket {

    public BlockPosition blockPosition;
    public String line1;
    public String line2;
    public String line3;
    public String line4;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.blockPosition = reader.readBlockPosition();
        this.line1 = reader.readSizedString();
        this.line2 = reader.readSizedString();
        this.line3 = reader.readSizedString();
        this.line4 = reader.readSizedString();

    }
}
