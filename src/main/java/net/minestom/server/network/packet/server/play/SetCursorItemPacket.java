package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record SetCursorItemPacket(@NotNull ItemStack itemStack) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetCursorItemPacket> SERIALIZER = NetworkBufferTemplate.template(
            ItemStack.NETWORK_TYPE, SetCursorItemPacket::itemStack,
            SetCursorItemPacket::new);
}
