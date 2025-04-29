package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.type.VillagerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.TradeListPacket;

public class VillagerTradeCommand extends Command {
    public VillagerTradeCommand() {
        super("villagertrade");
        setCondition(Conditions::playerOnly);

        setDefaultExecutor(this::handleVillagerTrade);
    }

    private void handleVillagerTrade(CommandSender source, CommandContext context) {
        Player player = (Player) source;

        player.sendMessage("Opening Villager Inventory");

        VillagerInventory inventory = new VillagerInventory("Villager Inventory");
        inventory.addTrade(new TradeListPacket.Trade(ItemStack.of(Material.DIRT), ItemStack.of(Material.DIAMOND),
                ItemStack.AIR, false, 0, 1, 0, 0, 0, 0));
        inventory.addTrade(new TradeListPacket.Trade(ItemStack.of(Material.HONEY_BOTTLE), ItemStack.of(Material.SNIFFER_EGG),
                ItemStack.of(Material.MAGENTA_TERRACOTTA), true, 1, 5, 99, 0, 9.5f, 127));

        player.openInventory(inventory);
    }
}
