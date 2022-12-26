package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.LeatherArmorMeta;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;

import java.util.HashMap;

public class RainbowCommand extends Command {


    public RainbowCommand() {
        super("rainbow");

        setCondition(Conditions::playerOnly);
        setDefaultExecutor((sender, context) -> {
            if (users.containsKey(sender)) {
                users.get(sender).stop();
                sender.sendMessage("Rainbow is Off");
            } else {
                new RainbowUser((Player) sender);
                sender.sendMessage("Rainbow is On");
            }
        });
    }

    private static final HashMap<Player, RainbowUser> users = new HashMap<>();


    public static class RainbowUser {

        private Player player;
        private Task task;
        private Entity horse;

        public RainbowUser(Player player) {
            users.put(player, this);
            this.player = player;
            this.horse = new Entity(EntityType.HORSE);
            assert player.getInstance() != null;
            this.horse.setInstance(player.getInstance(), player.getPosition());
            this.horse.spawn();
            this.horse.addPassenger(this.player);


            this.task = MinecraftServer.getSchedulerManager().buildTask(() -> {
                player.getInventory().setItemInOffHand(ItemStack.builder(Material.LEATHER_HORSE_ARMOR)
                        .meta(LeatherArmorMeta.class, meta -> meta.color(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255))))
                        .build());
                player.getInventory().setHelmet(ItemStack.builder(Material.LEATHER_HELMET)
                        .meta(LeatherArmorMeta.class, meta -> meta.color(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255))))
                        .build());
                player.getInventory().setChestplate(ItemStack.builder(Material.LEATHER_CHESTPLATE)
                        .meta(LeatherArmorMeta.class, meta -> meta.color(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255))))
                        .build());
                player.getInventory().setLeggings(ItemStack.builder(Material.LEATHER_LEGGINGS)
                        .meta(LeatherArmorMeta.class, meta -> meta.color(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255))))
                        .build());
                player.getInventory().setBoots(ItemStack.builder(Material.LEATHER_BOOTS)
                        .meta(LeatherArmorMeta.class, meta -> meta.color(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255))))
                        .build());
            }).repeat(1, TimeUnit.CLIENT_TICK).schedule();
        }


        public void stop() {
            task.cancel();
            users.remove(this.player);
            horse.remove();
        }
    }
}
