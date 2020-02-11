package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.utils.BlockPosition;

public class OpenSignEditorPacket implements ServerPacket {

    // WARNING: There must be a sign in this location (you can send a BlockChangePacket beforehand)
    public BlockPosition signPosition;

    @Override
    public void write(PacketWriter writer) {
        writer.writeBlockPosition(signPosition);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_SIGN_EDITOR;
    }
}
