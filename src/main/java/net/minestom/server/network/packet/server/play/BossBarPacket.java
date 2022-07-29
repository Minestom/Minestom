package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public record BossBarPacket(@NotNull UUID uuid, @NotNull Action action) implements ComponentHoldingServerPacket {
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
        writer.write(action);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return this.action instanceof ComponentHolder<?> holder
                ? holder.components()
                : List.of();
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return this.action instanceof ComponentHolder<?> holder
                ? new BossBarPacket(this.uuid, (Action) holder.copyWithOperator(operator))
                : this;
    }

    public sealed interface Action extends Writeable
            permits AddAction, RemoveAction, UpdateHealthAction, UpdateTitleAction, UpdateStyleAction, UpdateFlagsAction {
        int id();
    }

    public record AddAction(@NotNull Component title, float health, @NotNull BossBar.Color color,
                            @NotNull BossBar.Overlay overlay, byte flags) implements Action, ComponentHolder<AddAction> {
        public AddAction(@NotNull BossBar bar) {
            this(bar.name(), bar.progress(), bar.color(), bar.overlay(),
                    AdventurePacketConvertor.getBossBarFlagValue(bar.flags()));
        }

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

        @Override
        public @NotNull Collection<Component> components() {
            return List.of(this.title);
        }

        @Override
        public @NotNull AddAction copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return new AddAction(operator.apply(this.title), this.health, this.color, this.overlay, this.flags);
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
        public UpdateHealthAction(@NotNull BossBar bar) {
            this(bar.progress());
        }

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

    public record UpdateTitleAction(@NotNull Component title) implements Action, ComponentHolder<UpdateTitleAction> {
        public UpdateTitleAction(@NotNull BossBar bar) {
            this(bar.name());
        }

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

        @Override
        public @NotNull Collection<Component> components() {
            return List.of(this.title);
        }

        @Override
        public @NotNull UpdateTitleAction copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return new UpdateTitleAction(operator.apply(this.title));
        }
    }

    public record UpdateStyleAction(@NotNull BossBar.Color color,
                                    @NotNull BossBar.Overlay overlay) implements Action {
        public UpdateStyleAction(@NotNull BossBar bar) {
            this(bar.color(), bar.overlay());
        }

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
        public UpdateFlagsAction(@NotNull BossBar bar) {
            this(AdventurePacketConvertor.getBossBarFlagValue(bar.flags()));
        }

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
