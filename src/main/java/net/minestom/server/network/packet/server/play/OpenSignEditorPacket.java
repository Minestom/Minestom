package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryWriter;

public class OpenSignEditorPacket implements ServerPacket {

    // WARNING: There must be a sign in this location (you can send a BlockChangePacket beforehand)
    public BlockPosition signPosition;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeBlockPosition(signPosition);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_SIGN_EDITOR;
    }
}
