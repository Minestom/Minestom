package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class GiveCommand extends Command {
    public GiveCommand() {
        super("give");

        setDefaultExecutor((sender, context) ->
                sender.sendMessage(Component.text("Usage: /give <target> <item> [<count>]")));

        addSyntax((sender, context) -> {
            final EntityFinder entityFinder = context.get("target");
            int count = context.get("count");
            count = Math.min(count, PlayerInventoryUtils.INVENTORY_SIZE * 64);
            ItemStack itemStack = context.get("item");

            List<ItemStack> itemStacks;
            if (count <= 64) {
                itemStack = itemStack.withAmount(count);
                itemStacks = List.of(itemStack);
            } else {
                itemStacks = new ArrayList<>();
                while (count > 64) {
                    itemStacks.add(itemStack.withAmount(64));
                    count -= 64;
                }
                itemStacks.add(itemStack.withAmount(count));
            }

            final List<Entity> targets = entityFinder.find(sender);
            for (Entity target : targets) {
                if (target instanceof Player) {
                    Player player = (Player) target;
                    player.getInventory().addItemStacks(itemStacks, TransactionOption.ALL);
                }
            }

            sender.sendMessage(Component.text("Items have been given successfully!"));

        }, Entity("target").onlyPlayers(true), ItemStack("item"), Integer("count").setDefaultValue(() -> 1));

    }
}
