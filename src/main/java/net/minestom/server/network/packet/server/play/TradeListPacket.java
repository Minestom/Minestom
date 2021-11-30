package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TradeListPacket(int windowId, @NotNull List<Trade> trades,
                              int villagerLevel, int experience,
                              boolean regularVillager, boolean canRestock) implements ServerPacket {
    public TradeListPacket {
        trades = List.copyOf(trades);
    }

    public TradeListPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readByteList(Trade::new),
                reader.readVarInt(), reader.readVarInt(),
                reader.readBoolean(), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(windowId);
        writer.writeByteList(trades, BinaryWriter::write);
        writer.writeVarInt(villagerLevel);
        writer.writeVarInt(experience);
        writer.writeBoolean(regularVillager);
        writer.writeBoolean(canRestock);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TRADE_LIST;
    }

    public record Trade(ItemStack inputItem1, ItemStack result,
                        ItemStack inputItem2, boolean tradeDisabled,
                        int tradeUsesNumber, int maxTradeUsesNumber, int exp,
                        int specialPrice, float priceMultiplier, int demand) implements Writeable {
        public Trade(BinaryReader reader) {
            this(reader.readItemStack(), reader.readItemStack(),
                    reader.readBoolean() ? reader.readItemStack() : null, reader.readBoolean(),
                    reader.readInt(), reader.readInt(), reader.readInt(),
                    reader.readInt(), reader.readFloat(), reader.readInt());
        }

        @Override
        public void write(BinaryWriter writer) {
            boolean hasSecondItem = inputItem2 != null;

            writer.writeItemStack(inputItem1);
            writer.writeItemStack(result);
            writer.writeBoolean(hasSecondItem);
            if (hasSecondItem) writer.writeItemStack(inputItem2);
            writer.writeBoolean(tradeDisabled);
            writer.writeInt(tradeUsesNumber);
            writer.writeInt(maxTradeUsesNumber);
            writer.writeInt(exp);
            writer.writeInt(specialPrice);
            writer.writeFloat(priceMultiplier);
            writer.writeInt(demand);
        }
    }
}
