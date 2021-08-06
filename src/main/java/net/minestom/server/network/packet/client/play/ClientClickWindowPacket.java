package net.minestom.server.network.packet.client.play;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientClickWindowPacket extends ClientPlayPacket {

    public byte windowId;
    public int stateId;
    public short slot;
    public byte button;
    public ClickType clickType = ClickType.PICKUP;
    public Short2ObjectMap<ItemStack> changedSlots = new Short2ObjectOpenHashMap<>();
    public ItemStack item = ItemStack.AIR;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.windowId = reader.readByte();
        this.stateId = reader.readVarInt();
        this.slot = reader.readShort();
        this.button = reader.readByte();
        this.clickType = ClickType.values()[reader.readVarInt()];

        final int length = reader.readVarInt();
        this.changedSlots = new Short2ObjectOpenHashMap<>(length);
        for (int i = 0; i < length; i++) {
            short slot = reader.readShort();
            ItemStack item = reader.readItemStack();
            changedSlots.put(slot, item);
        }
        this.item = reader.readItemStack();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeVarInt(stateId);
        writer.writeShort(slot);
        writer.writeByte(button);
        writer.writeVarInt(clickType.ordinal());

        writer.writeVarInt(changedSlots.size());
        changedSlots.forEach((slot, itemStack) -> {
            writer.writeShort(slot);
            writer.writeItemStack(itemStack);
        });
        writer.writeItemStack(item);
    }

    public enum ClickType {
        PICKUP,
        QUICK_MOVE,
        SWAP,
        CLONE,
        THROW,
        QUICK_CRAFT,
        PICKUP_ALL
    }
}
