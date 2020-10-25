package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientUpdateCommandBlockMinecartPacket extends ClientPlayPacket {

    public int entityId;
    public String command;
    public boolean trackOutput;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.entityId = reader.readVarInt();
        this.command = reader.readSizedString();
        this.trackOutput = reader.readBoolean();
    }
}
