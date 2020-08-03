package fr.themode.demo.commands;

import fr.themode.demo.entity.ChickenCreature;
import net.minestom.server.advancements.notifications.AdvancementNotification;
import net.minestom.server.advancements.notifications.AdvancementNotificationManager;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.AdvancementsPacket;
import net.minestom.server.utils.Position;

public class SimpleCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean process(CommandSender sender, String command, String[] args) {

        if (!sender.isPlayer())
            return false;
        Player player = (Player) sender;

        /*for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (!(p instanceof FakePlayer))
                continue;
            FakePlayer fakePlayer = (FakePlayer) p;
            FakePlayerController controller = fakePlayer.getController();
            BlockPosition blockPosition = new BlockPosition(0, 39, 0);
            controller.startDigging(blockPosition);

            MinecraftServer.getSchedulerManager().addDelayedTask(new TaskRunnable() {
                @Override
                public void run() {
                    controller.stopDigging(blockPosition);
                }
            }, new UpdateOption(7, TimeUnit.TICK));

            break;
        }*/

        /*for (EntityCreature entityCreature : player.getInstance().getCreatures()) {
            entityCreature.setPathTo(player.getPosition().clone());
            //entityCreature.jump(1);
        }

        System.gc();
        player.sendMessage("Garbage collector called");*/

        Instance instance = player.getInstance();

        ChickenCreature chickenCreature = new ChickenCreature(new Position(-10, 43, -10));
        chickenCreature.setInstance(instance);

        chickenCreature.setPathTo(player.getPosition());

        new AdvancementNotificationManager().sendAdvancementNotification(
                new AdvancementNotification(
                        ColoredText.of(ChatColor.BRIGHT_GREEN + "Welcome to Minestom!"),
                        new ItemStack(Material.ACACIA_BOAT, (byte) 1), AdvancementsPacket.FrameType.GOAL)
                , player
        );
        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }
}
