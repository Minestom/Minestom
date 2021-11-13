package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record BossBarPacket(UUID uuid, Action action) implements ServerPacket {
    public BossBarPacket(BinaryReader reader) {
        this(reader.readUuid(), switch (reader.readVarInt()) {
            case 0 -> new AddAction(reader);
            case 1 -> new RemoveAction();
            case 2 -> new UpdateHealthAction(reader);
            case 3 -> new UpdateTitleAction(reader);
            case 4 -> new UpdateStyleAction(reader);
            case 5 -> new UpdateFlagsAction(reader);
            default -> throw new RuntimeException("Unknown action id");
        });
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeUuid(uuid);
        writer.writeVarInt(action.id());
        this.action.write(writer);
    }

    public sealed interface Action extends Writeable
            permits AddAction, RemoveAction, UpdateHealthAction, UpdateTitleAction, UpdateStyleAction, UpdateFlagsAction {
        int id();
    }

    public record AddAction(Component title, float health, BossBar.Color color,
                            BossBar.Overlay overlay, byte flags) implements Action {
        public AddAction(BinaryReader reader) {
            this(reader.readComponent(), reader.readFloat(),
                    BossBar.Color.values()[reader.readVarInt()],
                    BossBar.Overlay.values()[reader.readVarInt()], reader.readByte());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeComponent(title);
            writer.writeFloat(health);
            writer.writeVarInt(AdventurePacketConvertor.getBossBarColorValue(color));
            writer.writeVarInt(AdventurePacketConvertor.getBossBarOverlayValue(overlay));
            writer.writeByte(flags);
        }

        @Override
        public int id() {
            return 0;
        }
    }

    public record RemoveAction() implements Action {
        @Override
        public void write(@NotNull BinaryWriter writer) {
        }

        @Override
        public int id() {
            return 1;
        }
    }

    public record UpdateHealthAction(float health) implements Action {
        public UpdateHealthAction(BinaryReader reader) {
            this(reader.readFloat());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeFloat(health);
        }

        @Override
        public int id() {
            return 2;
        }
    }

    public record UpdateTitleAction(Component title) implements Action {
        public UpdateTitleAction(BinaryReader reader) {
            this(reader.readComponent());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeComponent(title);
        }

        @Override
        public int id() {
            return 3;
        }
    }

    public record UpdateStyleAction(BossBar.Color color, BossBar.Overlay overlay) implements Action {
        public UpdateStyleAction(BinaryReader reader) {
            this(BossBar.Color.values()[reader.readVarInt()], BossBar.Overlay.values()[reader.readVarInt()]);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(AdventurePacketConvertor.getBossBarColorValue(color));
            writer.writeVarInt(AdventurePacketConvertor.getBossBarOverlayValue(overlay));
        }

        @Override
        public int id() {
            return 4;
        }
    }

    public record UpdateFlagsAction(byte flags) implements Action {
        public UpdateFlagsAction(BinaryReader reader) {
            this(reader.readByte());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeByte(flags);
        }

        @Override
        public int id() {
            return 5;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BOSS_BAR;
    }
}
