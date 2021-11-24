package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientSpectatePacket(@NotNull UUID target) implements ClientPacket {
    public ClientSpectatePacket(BinaryReader reader) {
        this(reader.readUuid());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeUuid(target);
    }
}
