package net.minestom.server.inventory.type;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.server.play.TradeListPacket;
import net.minestom.server.utils.ArrayUtils;

public class VillagerInventory extends Inventory {

    protected TradeListPacket tradeListPacket;

    public VillagerInventory(String title) {
        super(InventoryType.MERCHANT, title);
        setupPacket();
    }

    public TradeListPacket.Trade[] getTrades() {
        return tradeListPacket.trades;
    }

    public void addTrade(TradeListPacket.Trade trade) {
        TradeListPacket.Trade[] oldTrades = getTrades();
        final int length = oldTrades.length + 1;
        TradeListPacket.Trade[] trades = new TradeListPacket.Trade[length];
        System.arraycopy(oldTrades, 0, trades, 0, oldTrades.length);
        trades[length] = trade;
        this.tradeListPacket.trades = trades;
        update();
    }

    public void removeTrade(int index) {
        TradeListPacket.Trade[] oldTrades = getTrades();
        final int length = oldTrades.length - 1;
        TradeListPacket.Trade[] trades = new TradeListPacket.Trade[length];
        ArrayUtils.removeElement(trades, index);
        this.tradeListPacket.trades = trades;
        update();
    }

    public int getVillagerLevel() {
        return tradeListPacket.villagerLevel;
    }

    public void setVillagerLevel(int level) {
        this.tradeListPacket.villagerLevel = level;
        update();
    }

    public int getExperience() {
        return tradeListPacket.experience;
    }

    public void setExperience(int experience) {
        this.tradeListPacket.experience = experience;
        update();
    }

    public boolean isRegularVillager() {
        return tradeListPacket.regularVillager;
    }

    public void setRegularVillager(boolean regularVillager) {
        this.tradeListPacket.regularVillager = regularVillager;
        update();
    }

    public boolean canRestock() {
        return tradeListPacket.canRestock;
    }

    public void setCanRestock(boolean canRestock) {
        this.tradeListPacket.canRestock = canRestock;
        update();
    }

    @Override
    public void update() {
        super.update();
        sendPacketToViewers(tradeListPacket); // Refresh window
    }

    private void setupPacket() {
        this.tradeListPacket = new TradeListPacket();
        this.tradeListPacket.windowId = getWindowId();
        this.tradeListPacket.trades = new TradeListPacket.Trade[0];
        this.tradeListPacket.villagerLevel = 0;
        this.tradeListPacket.experience = 0;
        this.tradeListPacket.regularVillager = false;
        this.tradeListPacket.canRestock = false;
    }
}
