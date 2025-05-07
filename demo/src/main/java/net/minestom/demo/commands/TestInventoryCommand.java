package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class TestInventoryCommand extends Command {

    private static final ItemStack UNSELECTED_SLOT = ItemStack.of(Material.GRAY_CONCRETE);
    private static final ItemStack SELECTED_SLOT = ItemStack.of(Material.GREEN_CONCRETE);

    private final Inventory testInventory;
    private int selectedSlot = 4;

    public TestInventoryCommand() {
        super("testinventory");

        testInventory = new Inventory(InventoryType.CHEST_1_ROW, "Test Inventory");

        for (int i = 0; i < testInventory.getSize(); i++) {
            testInventory.setItemStack(i, UNSELECTED_SLOT);
        }
        testInventory.setItemStack(selectedSlot, SELECTED_SLOT);

        testInventory.eventNode().addListener(InventoryPreClickEvent.class, event -> {
            int newSlot = event.getSlot();
            testInventory.setItemStack(selectedSlot, UNSELECTED_SLOT);
            testInventory.setItemStack(newSlot, SELECTED_SLOT);
            selectedSlot = newSlot;
            event.setCancelled(true);
        });

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.openInventory(testInventory);
            } else {
                sender.sendMessage("Only players can execute this command");
            }
        });
    }

}
