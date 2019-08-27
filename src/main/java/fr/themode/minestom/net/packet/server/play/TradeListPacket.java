package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class TradeListPacket implements ServerPacket {

    public int windowId;
    public Trade[] trades;
    public int villagerLevel;
    public int experience;
    public boolean regularVillager;
    public boolean canRestock;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, windowId);
        buffer.putByte((byte) trades.length);
        for (Trade trade : trades) {
            trade.write(buffer);
        }
        Utils.writeVarInt(buffer, villagerLevel);
        Utils.writeVarInt(buffer, experience);
        buffer.putBoolean(regularVillager);
        buffer.putBoolean(canRestock);
    }

    @Override
    public int getId() {
        return 0x27;
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


        private void write(Buffer buffer) {
            Utils.writeItemStack(buffer, inputItem1);
            Utils.writeItemStack(buffer, result);
            buffer.putBoolean(inputItem2 != null);
            if (inputItem2 != null)
                Utils.writeItemStack(buffer, inputItem2);
            buffer.putBoolean(tradeDisabled);
            buffer.putInt(tradeUsesNumber);
            buffer.putInt(maxTradeUsesNumber);
            buffer.putInt(exp);
            buffer.putInt(specialPrice);
            buffer.putFloat(priceMultiplier);
            buffer.putInt(demand);
        }

    }

}
