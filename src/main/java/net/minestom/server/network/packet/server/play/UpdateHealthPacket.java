package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class UpdateHealthPacket implements ServerPacket {

    public float health;
    public int food;
    public float foodSaturation;

    public UpdateHealthPacket(float health, int food, float foodSaturation) {
        this.health = health;
        this.food = food;
        this.foodSaturation = foodSaturation;
    }

    public UpdateHealthPacket() {
        this(0, 0, 0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeFloat(health);
        writer.writeVarInt(food);
        writer.writeFloat(foodSaturation);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        health = reader.readFloat();
        food = reader.readVarInt();
        foodSaturation = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_HEALTH;
    }
}
