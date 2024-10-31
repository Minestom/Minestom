package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetPlayerInventorySlotPacket(int slot, @NotNull ItemStack itemStack) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetPlayerInventorySlotPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SetPlayerInventorySlotPacket::slot,
            ItemStack.NETWORK_TYPE, SetPlayerInventorySlotPacket::itemStack,
            SetPlayerInventorySlotPacket::new);
}
