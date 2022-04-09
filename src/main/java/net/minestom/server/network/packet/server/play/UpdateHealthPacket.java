package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record UpdateHealthPacket(float health, int food, float foodSaturation) implements ServerPacket {
    public UpdateHealthPacket(BinaryReader reader) {
        this(reader.readFloat(), reader.readVarInt(), reader.readFloat());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(health);
        writer.writeVarInt(food);
        writer.writeFloat(foodSaturation);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_HEALTH;
    }
}
