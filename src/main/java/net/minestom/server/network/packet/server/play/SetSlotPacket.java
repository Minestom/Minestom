package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetSlotPacket(int windowId, int stateId, short slot,
                            @NotNull ItemStack itemStack) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<SetSlotPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SetSlotPacket::windowId,
            VAR_INT, SetSlotPacket::stateId,
            SHORT, SetSlotPacket::slot,
            ItemStack.NETWORK_TYPE, SetSlotPacket::itemStack,
            SetSlotPacket::new);

    @Override
    public @NotNull Collection<Component> components() {
        return ItemStack.textComponents(itemStack);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SetSlotPacket(this.windowId, this.stateId, this.slot, ItemStack.copyWithOperator(this.itemStack, operator));
    }

}
