package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class BossBarPacket implements ComponentHoldingServerPacket {

    public UUID uuid = new UUID(0, 0);
    public Action action = Action.ADD;

    public Component title = Component.empty(); // Only text
    public float health;
    public BossBar.Color color = BossBar.Color.BLUE;
    public BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
    public byte flags;

    public BossBarPacket() {
    }

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
    public void read(@NotNull BinaryReader reader) {
        uuid = reader.readUuid();
        action = Action.values()[reader.readVarInt()];

        switch (action) {
            case ADD:
                title = reader.readComponent();
                health = reader.readFloat();
                color = BossBar.Color.values()[reader.readVarInt()];
                overlay = BossBar.Overlay.values()[reader.readVarInt()];
                flags = reader.readByte();
                break;
            case REMOVE:

                break;
            case UPDATE_HEALTH:
                health = reader.readFloat();
                break;
            case UPDATE_TITLE:
                title = reader.readComponent();
                break;
            case UPDATE_STYLE:
                color = BossBar.Color.values()[reader.readVarInt()];
                overlay = BossBar.Overlay.values()[reader.readVarInt()];
                break;
            case UPDATE_FLAGS:
                flags = reader.readByte();
                break;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BOSS_BAR;
    }

    @Override
    public @NotNull Collection<Component> components() {
        if (title != null) {
            return Collections.singleton(title);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        switch (action) {
            case UPDATE_TITLE: {
                BossBarPacket packet = new BossBarPacket();
                packet.action = action;
                packet.uuid = uuid;
                packet.title = operator.apply(title);
                return packet;
            }
            case ADD: {
                BossBarPacket packet = new BossBarPacket();
                packet.action = action;
                packet.uuid = uuid;
                packet.title = operator.apply(title);
                packet.health = health;
                packet.overlay = overlay;
                packet.color = color;
                packet.flags = flags;
                return packet;
            }
            default:
                return this;
        }
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
