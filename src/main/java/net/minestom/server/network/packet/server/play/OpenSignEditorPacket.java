package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public class OpenSignEditorPacket implements ServerPacket {

    /**
     * WARNING: There must be a sign in this location (you can send a BlockChangePacket beforehand)
     */
    public Point signPosition;

    public OpenSignEditorPacket() {
        signPosition = Vec.ZERO;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(signPosition);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.signPosition = reader.readBlockPosition();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_SIGN_EDITOR;
    }
}
