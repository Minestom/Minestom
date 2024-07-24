package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChestCommand extends Command {
    public ChestCommand() {
        super("menu");

        setDefaultExecutor((sender, context) -> {
            Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "Example Menu");
            ((Player) sender).openInventory(inventory);
        });
    }
}
