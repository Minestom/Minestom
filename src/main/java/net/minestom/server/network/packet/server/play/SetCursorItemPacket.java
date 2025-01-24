package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.UnaryOperator;

public record SetCursorItemPacket(@NotNull ItemStack itemStack) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<SetCursorItemPacket> SERIALIZER = NetworkBufferTemplate.template(
            ItemStack.NETWORK_TYPE, SetCursorItemPacket::itemStack,
            SetCursorItemPacket::new);

    @Override
    public @NotNull Collection<Component> components() {
        return ItemStack.textComponents(itemStack);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SetCursorItemPacket(ItemStack.copyWithOperator(itemStack, operator));
    }
}
