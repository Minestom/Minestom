package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class TradeListPacket implements ServerPacket {

    public int windowId;
    public Trade[] trades;
    public int villagerLevel;
    public int experience;
    public boolean regularVillager;
    public boolean canRestock;

    @Override
    public void write(BinaryWriter writer) {
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
    public int getId() {
        return ServerPacketIdentifier.TRADE_LIST;
    }

    public static class Trade {

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


        private void write(BinaryWriter writer) {
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

    }

}
