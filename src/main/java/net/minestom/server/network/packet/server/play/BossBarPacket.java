package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record BossBarPacket(@NotNull UUID uuid,
                            @NotNull Action action) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<BossBarPacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, BossBarPacket value) {
            buffer.write(NetworkBuffer.UUID, value.uuid);
            buffer.write(VAR_INT, value.action.id());
            buffer.write(value.action);
        }

        @Override
        public BossBarPacket read(@NotNull NetworkBuffer buffer) {
            return new BossBarPacket(buffer.read(NetworkBuffer.UUID), switch (buffer.read(VAR_INT)) {
                case 0 -> new AddAction(buffer);
                case 1 -> new RemoveAction();
                case 2 -> new UpdateHealthAction(buffer);
                case 3 -> new UpdateTitleAction(buffer);
                case 4 -> new UpdateStyleAction(buffer);
                case 5 -> new UpdateFlagsAction(buffer);
                default -> throw new RuntimeException("Unknown action id");
            });
        }
    };

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

    public sealed interface Action extends NetworkBuffer.Writer
            permits AddAction, RemoveAction, UpdateHealthAction, UpdateTitleAction, UpdateStyleAction, UpdateFlagsAction {
        int id();
    }

    public record AddAction(@NotNull Component title, float health, @NotNull BossBar.Color color,
                            @NotNull BossBar.Overlay overlay,
                            byte flags) implements Action, ComponentHolder<AddAction> {
        public AddAction(@NotNull BossBar bar) {
            this(bar.name(), bar.progress(), bar.color(), bar.overlay(),
                    AdventurePacketConvertor.getBossBarFlagValue(bar.flags()));
        }

        public AddAction(@NotNull NetworkBuffer reader) {
            this(reader.read(COMPONENT), reader.read(FLOAT),
                    BossBar.Color.values()[reader.read(VAR_INT)],
                    BossBar.Overlay.values()[reader.read(VAR_INT)], reader.read(BYTE));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(COMPONENT, title);
            writer.write(FLOAT, health);
            writer.write(VAR_INT, AdventurePacketConvertor.getBossBarColorValue(color));
            writer.write(VAR_INT, AdventurePacketConvertor.getBossBarOverlayValue(overlay));
            writer.write(BYTE, flags);
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
        public void write(@NotNull NetworkBuffer writer) {
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

        public UpdateHealthAction(@NotNull NetworkBuffer reader) {
            this(reader.read(FLOAT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(FLOAT, health);
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

        public UpdateTitleAction(@NotNull NetworkBuffer reader) {
            this(reader.read(COMPONENT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(COMPONENT, title);
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

        public UpdateStyleAction(@NotNull NetworkBuffer reader) {
            this(BossBar.Color.values()[reader.read(VAR_INT)], BossBar.Overlay.values()[reader.read(VAR_INT)]);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(VAR_INT, AdventurePacketConvertor.getBossBarColorValue(color));
            writer.write(VAR_INT, AdventurePacketConvertor.getBossBarOverlayValue(overlay));
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

        public UpdateFlagsAction(@NotNull NetworkBuffer reader) {
            this(reader.read(BYTE));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(BYTE, flags);
        }

        @Override
        public int id() {
            return 5;
        }
    }

}
