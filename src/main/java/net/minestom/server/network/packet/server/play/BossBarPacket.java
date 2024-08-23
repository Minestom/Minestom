package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
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
            @SuppressWarnings("unchecked") final Type<Action> serializer = (Type<Action>) actionSerializer(value.action.id());
            buffer.write(serializer, value.action);
        }

        @Override
        public BossBarPacket read(@NotNull NetworkBuffer buffer) {
            final UUID uuid = buffer.read(NetworkBuffer.UUID);
            final int id = buffer.read(VAR_INT);
            final Type<? extends Action> serializer = actionSerializer(id);
            return new BossBarPacket(uuid, serializer.read(buffer));
        }
    };

    @Override
    public @NotNull Collection<Component> components() {
        return this.action instanceof ComponentHolder<?> holder
                ? holder.components()
                : List.of();
    }

    private static Type<? extends Action> actionSerializer(int id) {
        return switch (id) {
            case 0 -> AddAction.SERIALIZER;
            case 1 -> RemoveAction.SERIALIZER;
            case 2 -> UpdateHealthAction.SERIALIZER;
            case 3 -> UpdateTitleAction.SERIALIZER;
            case 4 -> UpdateStyleAction.SERIALIZER;
            case 5 -> UpdateFlagsAction.SERIALIZER;
            default -> throw new RuntimeException("Unknown action id");
        };
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return this.action instanceof ComponentHolder<?> holder
                ? new BossBarPacket(this.uuid, (Action) holder.copyWithOperator(operator))
                : this;
    }

    public sealed interface Action permits
            AddAction, RemoveAction, UpdateHealthAction,
            UpdateTitleAction, UpdateStyleAction, UpdateFlagsAction {
        int id();
    }

    public record AddAction(@NotNull Component title, float health, @NotNull BossBar.Color color,
                            @NotNull BossBar.Overlay overlay,
                            byte flags) implements Action, ComponentHolder<AddAction> {
        public AddAction(@NotNull BossBar bar) {
            this(bar.name(), bar.progress(), bar.color(), bar.overlay(),
                    AdventurePacketConvertor.getBossBarFlagValue(bar.flags()));
        }

        public static final NetworkBuffer.Type<AddAction> SERIALIZER = NetworkBufferTemplate.template(
                COMPONENT, AddAction::title,
                FLOAT, AddAction::health,
                Enum(BossBar.Color.class), AddAction::color,
                Enum(BossBar.Overlay.class), AddAction::overlay,
                BYTE, AddAction::flags,
                AddAction::new
        );

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
        public static final NetworkBuffer.Type<RemoveAction> SERIALIZER = NetworkBufferTemplate.template(RemoveAction::new);

        @Override
        public int id() {
            return 1;
        }
    }

    public record UpdateHealthAction(float health) implements Action {
        public UpdateHealthAction(@NotNull BossBar bar) {
            this(bar.progress());
        }

        public static final NetworkBuffer.Type<UpdateHealthAction> SERIALIZER = NetworkBufferTemplate.template(
                FLOAT, UpdateHealthAction::health,
                UpdateHealthAction::new
        );

        @Override
        public int id() {
            return 2;
        }
    }

    public record UpdateTitleAction(@NotNull Component title) implements Action, ComponentHolder<UpdateTitleAction> {
        public UpdateTitleAction(@NotNull BossBar bar) {
            this(bar.name());
        }

        public static final NetworkBuffer.Type<UpdateTitleAction> SERIALIZER = NetworkBufferTemplate.template(
                COMPONENT, UpdateTitleAction::title,
                UpdateTitleAction::new
        );

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

        public static final NetworkBuffer.Type<UpdateStyleAction> SERIALIZER = NetworkBufferTemplate.template(
                Enum(BossBar.Color.class), UpdateStyleAction::color,
                Enum(BossBar.Overlay.class), UpdateStyleAction::overlay,
                UpdateStyleAction::new
        );

        @Override
        public int id() {
            return 4;
        }
    }

    public record UpdateFlagsAction(byte flags) implements Action {
        public UpdateFlagsAction(@NotNull BossBar bar) {
            this(AdventurePacketConvertor.getBossBarFlagValue(bar.flags()));
        }

        public static final NetworkBuffer.Type<UpdateFlagsAction> SERIALIZER = NetworkBufferTemplate.template(
                BYTE, UpdateFlagsAction::flags,
                UpdateFlagsAction::new
        );

        @Override
        public int id() {
            return 5;
        }
    }
}
