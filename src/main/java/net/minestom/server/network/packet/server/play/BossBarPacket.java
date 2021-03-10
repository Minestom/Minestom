package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BossBarPacket implements ServerPacket {

    public UUID uuid;
    public Action action;

    public Component title; // Only text
    public float health;
    public BossBar.Color color;
    public BossBar.Overlay overlay;
    public byte flags;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeUuid(uuid);
        writer.writeVarInt(action.ordinal());

        switch (action) {
            case ADD:
                writer.writeComponent(title);
                writer.writeFloat(health);
                writer.writeVarInt(AdventurePacketConvertor.getBossBarColorValue(color));
                writer.writeVarInt(AdventurePacketConvertor.getBossBarOverlayValue(overlay));
                writer.writeByte(flags);
                break;
            case REMOVE:

                break;
            case UPDATE_HEALTH:
                writer.writeFloat(health);
                break;
            case UPDATE_TITLE:
                writer.writeComponent(title);
                break;
            case UPDATE_STYLE:
                writer.writeVarInt(AdventurePacketConvertor.getBossBarColorValue(color));
                writer.writeVarInt(AdventurePacketConvertor.getBossBarOverlayValue(overlay));
                break;
            case UPDATE_FLAGS:
                writer.writeByte(flags);
                break;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BOSS_BAR;
    }

    public enum Action {
        ADD,
        REMOVE,
        UPDATE_HEALTH,
        UPDATE_TITLE,
        UPDATE_STYLE,
        UPDATE_FLAGS
    }

}
