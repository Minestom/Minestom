package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientUpdateCommandBlockMinecartPacket(int entityId, @NotNull String command,
                                                     boolean trackOutput) implements ClientPacket {
    public ClientUpdateCommandBlockMinecartPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readSizedString(Short.MAX_VALUE), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeSizedString(command);
        writer.writeBoolean(trackOutput);
    }
}
