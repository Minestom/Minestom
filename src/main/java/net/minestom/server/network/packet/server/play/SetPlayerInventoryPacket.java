package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetPlayerInventoryPacket(int slot, @NotNull ItemStack itemStack) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetPlayerInventoryPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SetPlayerInventoryPacket::slot,
            ItemStack.NETWORK_TYPE, SetPlayerInventoryPacket::itemStack,
            SetPlayerInventoryPacket::new);
}
