package demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.TradeListPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class MerchantCommand extends Command {
    public MerchantCommand() {
        super("merchant");

        this.setDefaultExecutor((sender, context) -> {
            Player player = sender.asPlayer();
            Inventory inventory = new Inventory(InventoryType.MERCHANT, "Minestom Merchant");

            TradeListPacket packet = generatePacket(inventory.getWindowId());
            player.openInventory(inventory);
            player.getPlayerConnection().sendPacket(packet);
        });
    }

    private TradeListPacket generatePacket(int windowID) {
        TradeListPacket packet = new TradeListPacket();

        packet.windowId = windowID;
        packet.canRestock = true;
        packet.experience = 0;
        packet.regularVillager = true;
        packet.villagerLevel = 1;

        List<TradeListPacket.Trade> trades = new ArrayList<>();

        List<Material> materials = new ArrayList<>(Material.values());

        Random random = new Random();

        Supplier<Material> randomMat = () -> materials.get(random.nextInt(materials.size()));

        for (int i = 0; i < 10; i++) {

            TradeListPacket.Trade trade = new TradeListPacket.Trade();

            trade.inputItem1 = ItemStack.of(randomMat.get());
            trade.result = ItemStack.of(randomMat.get());
            trade.inputItem2 = ItemStack.of(randomMat.get());
            trade.demand = 0;
            trade.exp = 23;
            trade.maxTradeUsesNumber = Integer.MAX_VALUE;
            trade.tradeDisabled = false;

            trades.add(trade);
        }

        packet.trades = trades.toArray(TradeListPacket.Trade[]::new);

        return packet;
    }
}
