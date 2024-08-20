package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.batch.RelativeBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemComponent;
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

        RelativeBlockBatch areaBatch = new RelativeBlockBatch();
        for (int x = -20; x < 21; x++) {
            for (int z = -20; z < 21; z++) {
                for (int y = -10; y < 0; y++) {
                    areaBatch.setBlock(x, y, z, Block.WHITE_WOOL);
                }
            }
        }
        areaBatch.setBlock(2, 0, 0, Block.WATER);
        areaBatch.setBlock(3, 0, 0, Block.WATER);
        areaBatch.setBlock(2, 0, 1, Block.WATER);
        areaBatch.setBlock(3, 0, 1, Block.WATER);
        for (int x = -3; x < 0; x++) {
            for (int z = -3; z < 0; z++) {
                for (int y = 0; y < 4; y++) {
                    areaBatch.setBlock(x, y, z, Block.WATER);
                }
            }
        }


        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
            Player player = (Player) sender;
            areaBatch.apply(player.getInstance(), player.getPosition(), null);
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
            areaBatch.apply(player.getInstance(), player.getPosition(), null);
        });
    }

    private void giveItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        items.add(ItemStack.builder(Material.SHEARS).set(ItemComponent.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.EFFICIENCY, 5)).build());
        items.add(ItemStack.builder(Material.WHITE_WOOL).amount(64).build());
        for (ItemStack item : items) {
            player.getInventory().addItemStack(item);
        }
    }
}
