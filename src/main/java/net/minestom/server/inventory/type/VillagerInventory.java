package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.ContainerInventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.TradeListPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VillagerInventory extends ContainerInventory {
    private final CachedPacket tradeCache = new CachedPacket(this::createTradePacket);
    private final List<TradeListPacket.Trade> trades = new ArrayList<>();
    private int villagerLevel;
    private int experience;
    private boolean regularVillager;
    private boolean canRestock;

    public VillagerInventory(@NotNull Component title) {
        super(InventoryType.MERCHANT, title);
    }

    public VillagerInventory(@NotNull String title) {
        super(InventoryType.MERCHANT, title);
    }

    public List<TradeListPacket.Trade> getTrades() {
        return Collections.unmodifiableList(trades);
    }

    public void addTrade(TradeListPacket.Trade trade) {
        this.trades.add(trade);
        update();
    }

    public void removeTrade(int index) {
        this.trades.remove(index);
        update();
    }

    public int getVillagerLevel() {
        return villagerLevel;
    }

    public void setVillagerLevel(int level) {
        this.villagerLevel = level;
        update();
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
        update();
    }

    public boolean isRegularVillager() {
        return regularVillager;
    }

    public void setRegularVillager(boolean regularVillager) {
        this.regularVillager = regularVillager;
        update();
    }

    public boolean canRestock() {
        return canRestock;
    }

    public void setCanRestock(boolean canRestock) {
        this.canRestock = canRestock;
        update();
    }

    @Override
    public void update() {
        super.update();
        this.tradeCache.invalidate();
    }

    @Override
    public void update(@NotNull Player player) {
        super.update(player);
        player.sendPacket(tradeCache);
    }

    private TradeListPacket createTradePacket() {
        return new TradeListPacket(getWindowId(), trades, villagerLevel, experience, regularVillager, canRestock);
    }
}
