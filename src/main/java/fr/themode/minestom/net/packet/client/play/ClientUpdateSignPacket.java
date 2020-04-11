package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.BlockPosition;

public class ClientUpdateSignPacket extends ClientPlayPacket {

    public BlockPosition blockPosition;
    public String line1;
    public String line2;
    public String line3;
    public String line4;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readBlockPosition(blockPosition1 -> blockPosition = blockPosition1);
        reader.readSizedString((string, length) -> line1 = string);
        reader.readSizedString((string, length) -> line2 = string);
        reader.readSizedString((string, length) -> line3 = string);
        reader.readSizedString((string, length) -> {
            line4 = string;
            callback.run();
        });

    }
}
