package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetPlayerInventorySlotPacket(int slot, @NotNull ItemStack itemStack) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<SetPlayerInventorySlotPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SetPlayerInventorySlotPacket::slot,
            ItemStack.NETWORK_TYPE, SetPlayerInventorySlotPacket::itemStack,
            SetPlayerInventorySlotPacket::new);

    @Override
    public @NotNull Collection<Component> components() {
        return ItemStack.textComponents(itemStack);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SetPlayerInventorySlotPacket(slot, ItemStack.copyWithOperator(itemStack, operator));
    }
}
