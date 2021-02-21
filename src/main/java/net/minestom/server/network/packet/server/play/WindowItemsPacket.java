package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.packet.server.ServerPlayerSpecificPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WindowItemsPacket implements ServerPlayerSpecificPacket {

    public byte windowId;
    public ItemStack[] items;

    @Override
    public void writeForSpecificPlayer(@NotNull BinaryWriter writer, @Nullable Player player) {
        writer.writeByte(windowId);

        if (items == null) {
            writer.writeShort((short) 0);
            return;
        }

        writer.writeShort((short) items.length);
        for (ItemStack item : items) {
            writer.writeItemStack(item, player);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_ITEMS;
    }
}
