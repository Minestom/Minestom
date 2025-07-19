package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.BlockBatch;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class TestInstabreakCommand extends Command {

    public TestInstabreakCommand() {
        super("testinstabreak");

        ArgumentInteger level = ArgumentType.Integer("level");
        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;

            int l = context.get(level);
            player.removeEffect(PotionEffect.HASTE);
            if (l != 0) {
                player.addEffect(new Potion(PotionEffect.HASTE, (byte) (l - 1), -1));
            }
        }, ArgumentType.Literal("haste"), level);
        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;

            int l = context.get(level);
            player.removeEffect(PotionEffect.CONDUIT_POWER);
            if (l != 0) {
                player.addEffect(new Potion(PotionEffect.CONDUIT_POWER, (byte) (l - 1), -1));
            }
        }, ArgumentType.Literal("conduit"), level);
        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;

            int l = context.get(level);
            player.removeEffect(PotionEffect.MINING_FATIGUE);
            if (l != 0) {
                player.addEffect(new Potion(PotionEffect.MINING_FATIGUE, (byte) (l - 1), -1));
            }
        }, ArgumentType.Literal("fatigue"), level);

        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;
            giveItems(player);
        }, ArgumentType.Literal("giveItems"));

        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;
            player.openInventory(new Inventory(InventoryType.ANVIL, Component.translatable("container.repair")));
        }, ArgumentType.Literal("anvil"));

        BlockBatch areaBatch = BlockBatch.unaligned(builder -> {
            for (int x = -20; x < 21; x++) {
                for (int z = -20; z < 21; z++) {
                    for (int y = -10; y < 0; y++) {
                        builder.setBlock(x, y, z, Block.WHITE_WOOL);
                    }
                }
            }
            builder.setBlock(2, 0, 0, Block.WATER);
            builder.setBlock(3, 0, 0, Block.WATER);
            builder.setBlock(2, 0, 1, Block.WATER);
            builder.setBlock(3, 0, 1, Block.WATER);
            builder.setBlock(5, 1, 0, Block.WATER);
            builder.setBlock(6, 1, 0, Block.WATER);
            builder.setBlock(5, 1, 1, Block.WATER);
            builder.setBlock(6, 1, 1, Block.WATER);
            builder.setBlock(8, 1, 1, Block.WATER.withProperty("level", "0"));
            builder.setBlock(10, 1, 1, Block.WATER.withProperty("level", "1"));
            builder.setBlock(8, 1, 3, Block.WATER.withProperty("level", "2"));
            builder.setBlock(10, 1, 3, Block.WATER.withProperty("level", "3"));
            builder.setBlock(8, 1, 5, Block.WATER.withProperty("level", "4"));
            builder.setBlock(10, 1, 5, Block.WATER.withProperty("level", "5"));
            builder.setBlock(8, 1, 7, Block.WATER.withProperty("level", "6"));
            builder.setBlock(10, 1, 7, Block.WATER.withProperty("level", "7"));
            builder.setBlock(8, 1, 9, Block.WATER.withProperty("level", "8"));
            builder.setBlock(10, 1, 9, Block.WATER.withProperty("level", "13"));
            for (int x = -3; x < 0; x++) {
                for (int z = -3; z < 0; z++) {
                    for (int y = 0; y < 4; y++) {
                        builder.setBlock(x, y, z, Block.WATER);
                    }
                }
            }
            for (int x = -9; x < -6; x++) {
                for (int z = -9; z < -6; z++) {
                    for (int y = 0; y < 3; y++) {
                        builder.setBlock(x, y, z, Block.BAMBOO);
                    }
                    builder.setBlock(x, 3, z, Block.BAMBOO_SAPLING);
                }
            }
        });


        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;
            final Point point = player.getPosition();
            player.getInstance().setBlockBatch(point, areaBatch);
        }, ArgumentType.Literal("placeArea"));

        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;
            boolean state = context.get("state");
            player.setInstantBreak(state);
        }, ArgumentType.Literal("instabreak"), ArgumentType.Boolean("state"));

        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            giveItems(player);
            final Point point = player.getPosition();
            player.getInstance().setBlockBatch(point, areaBatch);
        });
    }

    private void giveItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        items.add(ItemStack.builder(Material.SHEARS).set(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.EFFICIENCY, 5)).build());
        items.add(ItemStack.builder(Material.WHITE_WOOL).amount(64).build());
        items.add(ItemStack.builder(Material.STONE).amount(64).build());
        items.add(ItemStack.of(Material.DIAMOND_SWORD));
        items.add(ItemStack.of(Material.DIAMOND_PICKAXE));
        for (ItemStack item : items) {
            player.getInventory().addItemStack(item);
        }
    }
}
