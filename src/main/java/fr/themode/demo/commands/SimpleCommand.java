package fr.themode.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.fakeplayer.FakePlayerController;
import net.minestom.server.timer.TaskRunnable;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;

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

        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
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
            }, new UpdateOption(15, TimeUnit.TICK));
        }

        //System.gc();
        //player.sendMessage("Garbage collector called");

        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }
}
