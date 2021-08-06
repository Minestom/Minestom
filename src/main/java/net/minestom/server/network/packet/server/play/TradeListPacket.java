package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public class TradeListPacket implements ServerPacket {

    public int windowId;
    public Trade[] trades;
    public int villagerLevel;
    public int experience;
    public boolean regularVillager;
    public boolean canRestock;

    /**
     * Default constructor, required for reflection operations.
     */
    public TradeListPacket() {
        trades = new Trade[0];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(windowId);
        writer.writeByte((byte) trades.length);
        for (Trade trade : trades) {
            trade.write(writer);
        }
        writer.writeVarInt(villagerLevel);
        writer.writeVarInt(experience);
        writer.writeBoolean(regularVillager);
        writer.writeBoolean(canRestock);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readVarInt();
        byte tradeCount = reader.readByte();

        trades = new Trade[tradeCount];
        for (int i = 0; i < tradeCount; i++) {
            trades[i] = new Trade();
            trades[i].read(reader);
        }
        villagerLevel = reader.readVarInt();
        experience = reader.readVarInt();
        regularVillager = reader.readBoolean();
        canRestock = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TRADE_LIST;
    }

    public static class Trade implements Writeable, Readable {

        public ItemStack inputItem1;
        public ItemStack result;
        public ItemStack inputItem2;
        public boolean tradeDisabled;
        public int tradeUsesNumber;
        public int maxTradeUsesNumber;
        public int exp;
        public int specialPrice;
        public float priceMultiplier;
        public int demand;

        @Override
        public void write(BinaryWriter writer) {
            boolean hasSecondItem = inputItem2 != null;

            writer.writeItemStack(inputItem1);
            writer.writeItemStack(result);
            writer.writeBoolean(hasSecondItem);
            if (hasSecondItem)
                writer.writeItemStack(inputItem2);
            writer.writeBoolean(tradeDisabled);
            writer.writeInt(tradeUsesNumber);
            writer.writeInt(maxTradeUsesNumber);
            writer.writeInt(exp);
            writer.writeInt(specialPrice);
            writer.writeFloat(priceMultiplier);
            writer.writeInt(demand);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            inputItem1 = reader.readItemStack();
            result = reader.readItemStack();

            boolean hasSecondItem = reader.readBoolean();
            if (hasSecondItem) {
                inputItem2 = reader.readItemStack();
            } else {
                inputItem2 = null;
            }

            tradeDisabled = reader.readBoolean();
            tradeUsesNumber = reader.readInt();
            maxTradeUsesNumber = reader.readInt();
            exp = reader.readInt();
            specialPrice = reader.readInt();
            priceMultiplier = reader.readFloat();
            demand = reader.readInt();
        }
    }

}
