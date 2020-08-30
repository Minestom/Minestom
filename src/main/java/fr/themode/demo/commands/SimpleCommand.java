package fr.themode.demo.commands;

import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.notifications.Notification;
import net.minestom.server.advancements.notifications.NotificationCenter;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;

public class SimpleCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"alias"};
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

        /*Instance instance = player.getInstance();

        ChickenCreature chickenCreature = new ChickenCreature(new Position(-10, 43, -10));
        chickenCreature.setInstance(instance);

        chickenCreature.setPathTo(player.getPosition());*/

        final Notification notification = new Notification(ColoredText.of(ChatColor.BRIGHT_GREEN + "Welcome to Minestom!"),
                FrameType.TASK, Material.APPLE);

        NotificationCenter.send(notification, player);
        NotificationCenter.send(notification, player);

        System.gc();

        player.getInstance().saveChunksToStorage(() -> System.out.println("end save"));

        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }

    @Override
    public boolean enableWritingTracking() {
        return true;
    }

    @Override
    public String[] onWrite(String text) {
        return new String[]{"Complete1", "Complete2"};
    }
}
