package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientSetBeaconEffectPacket(int primaryEffect, int secondaryEffect) implements ClientPacket {
    public ClientSetBeaconEffectPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readVarInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(primaryEffect);
        writer.writeVarInt(secondaryEffect);
    }
}
