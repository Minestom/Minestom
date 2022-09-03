package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public record SetSlotPacket(byte windowId, int stateId, short slot,
                            @NotNull ItemStack itemStack) implements ComponentHoldingServerPacket {
    public SetSlotPacket(BinaryReader reader) {
        this(reader.readByte(), reader.readVarInt(), reader.readShort(),
                reader.readItemStack());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeVarInt(stateId);
        writer.writeShort(slot);
        writer.writeItemStack(itemStack);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_SLOT;
    }

    @Override
    public @NotNull Collection<Component> components() {
        final var components = new ArrayList<>(this.itemStack.getLore());
        final var displayname = this.itemStack.getDisplayName();
        if (displayname != null) components.add(displayname);

        return List.copyOf(components);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SetSlotPacket(this.windowId, this.stateId, this.slot, this.itemStack.withDisplayName(operator).withLore(lines -> {
            final var translatedComponents = new ArrayList<Component>();
            lines.forEach(component -> translatedComponents.add(operator.apply(component)));
            return translatedComponents;
        }));
    }

    /**
     * Returns a {@link SetSlotPacket} used to change a player cursor item.
     *
     * @param cursorItem the cursor item
     * @return a set slot packet to change a player cursor item
     */
    public static @NotNull SetSlotPacket createCursorPacket(@NotNull ItemStack cursorItem) {
        return new SetSlotPacket((byte) -1, 0, (short) -1, cursorItem);
    }
}
