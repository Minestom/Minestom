package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class UpdateScorePacket implements ServerPacket {
    private final String entityName;
    private final byte action;
    private final String objectiveName;
    private final int value;

    public UpdateScorePacket(String entityName, byte action, String objectiveName, int value) {
        this.entityName = entityName;
        this.action = action;
        this.objectiveName = objectiveName;
        this.value = value;
    }

    public UpdateScorePacket(BinaryReader reader) {
        this.entityName = reader.readSizedString();
        this.action = reader.readByte();
        this.objectiveName = reader.readSizedString();
        this.value = action != 1 ? reader.readVarInt() : 0;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(entityName);
        writer.writeByte(action);
        writer.writeSizedString(objectiveName);
        if (action != 1) {
            writer.writeVarInt(value);
        }
    }

    public String entityName() {
        return entityName;
    }

    public byte action() {
        return action;
    }

    public String objectiveName() {
        return objectiveName;
    }

    public int value() {
        return value;
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_SCORE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpdateScorePacket that)) return false;
        return action == that.action && value == that.value &&
                Objects.equals(entityName, that.entityName) && Objects.equals(objectiveName, that.objectiveName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityName, action, objectiveName, value);
    }
}
