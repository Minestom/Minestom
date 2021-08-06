package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientUpdateCommandBlockMinecartPacket extends ClientPlayPacket {

    public int entityId;
    public String command = "";
    public boolean trackOutput;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.entityId = reader.readVarInt();
        this.command = reader.readSizedString(Short.MAX_VALUE);
        this.trackOutput = reader.readBoolean();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeSizedString(command);
        writer.writeBoolean(trackOutput);
    }
}
