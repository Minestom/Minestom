package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record BossBarPacket(UUID uuid,
                            Action action) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<BossBarPacket> SERIALIZER = NetworkBufferTemplate.template(
            UUID, BossBarPacket::uuid,
  /*VarInt*/BYTE.unionType(Action::serializer, Action::id), BossBarPacket::action,
            BossBarPacket::new
    );

    @Override
    public Collection<Component> components() {
        return this.action instanceof ComponentHolder<?> holder
                ? holder.components()
                : List.of();
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        return this.action instanceof ComponentHolder<?> holder
                ? new BossBarPacket(this.uuid, (Action) holder.copyWithOperator(operator))
                : this;
    }

    public sealed interface Action {
        private static Type<? extends Action> serializer(byte id) {
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

        @ApiStatus.OverrideOnly
        byte id();
    }

    public record AddAction(Component title, float health, BossBar.Color color,
                            BossBar.Overlay overlay,
                            byte flags) implements Action, ComponentHolder<AddAction> {
        public AddAction(BossBar bar) {
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
        public byte id() {
            return 0;
        }

        @Override
        public Collection<Component> components() {
            return List.of(this.title);
        }

        @Override
        public AddAction copyWithOperator(UnaryOperator<Component> operator) {
            return new AddAction(operator.apply(this.title), this.health, this.color, this.overlay, this.flags);
        }
    }

    public record RemoveAction() implements Action {
        public static final RemoveAction INSTANCE = new RemoveAction();
        public static final NetworkBuffer.Type<RemoveAction> SERIALIZER = NetworkBufferTemplate.template(INSTANCE);

        @Override
        public byte id() {
            return 1;
        }
    }

    public record UpdateHealthAction(float health) implements Action {
        public UpdateHealthAction(BossBar bar) {
            this(bar.progress());
        }

        public static final NetworkBuffer.Type<UpdateHealthAction> SERIALIZER = NetworkBufferTemplate.template(
                FLOAT, UpdateHealthAction::health,
                UpdateHealthAction::new
        );

        @Override
        public byte id() {
            return 2;
        }
    }

    public record UpdateTitleAction(Component title) implements Action, ComponentHolder<UpdateTitleAction> {
        public UpdateTitleAction(BossBar bar) {
            this(bar.name());
        }

        public static final NetworkBuffer.Type<UpdateTitleAction> SERIALIZER = NetworkBufferTemplate.template(
                COMPONENT, UpdateTitleAction::title,
                UpdateTitleAction::new
        );

        @Override
        public byte id() {
            return 3;
        }

        @Override
        public Collection<Component> components() {
            return List.of(this.title);
        }

        @Override
        public UpdateTitleAction copyWithOperator(UnaryOperator<Component> operator) {
            return new UpdateTitleAction(operator.apply(this.title));
        }
    }

    public record UpdateStyleAction(BossBar.Color color,
                                    BossBar.Overlay overlay) implements Action {
        public UpdateStyleAction(BossBar bar) {
            this(bar.color(), bar.overlay());
        }

        public static final NetworkBuffer.Type<UpdateStyleAction> SERIALIZER = NetworkBufferTemplate.template(
                Enum(BossBar.Color.class), UpdateStyleAction::color,
                Enum(BossBar.Overlay.class), UpdateStyleAction::overlay,
                UpdateStyleAction::new
        );

        @Override
        public byte id() {
            return 4;
        }
    }

    public record UpdateFlagsAction(byte flags) implements Action {
        public UpdateFlagsAction(BossBar bar) {
            this(AdventurePacketConvertor.getBossBarFlagValue(bar.flags()));
        }

        public static final NetworkBuffer.Type<UpdateFlagsAction> SERIALIZER = NetworkBufferTemplate.template(
                BYTE, UpdateFlagsAction::flags,
                UpdateFlagsAction::new
        );

        @Override
        public byte id() {
            return 5;
        }
    }
}
