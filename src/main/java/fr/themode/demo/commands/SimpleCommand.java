package fr.themode.demo.commands;

import net.minestom.server.command.CommandProcessor;
import net.minestom.server.entity.Player;

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
    public boolean process(Player player, String command, String[] args) {

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

        System.gc();
        player.sendMessage("Garbage collector called");

        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }
}
