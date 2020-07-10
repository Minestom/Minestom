package fr.themode.demo.commands;

import fr.themode.demo.entity.ChickenCreature;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

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

        ChickenCreature chickenCreature = new ChickenCreature(player.getPosition());
        chickenCreature.setInstance(instance);

        /*PFPathingEntity pathingEntity = new PFPathingEntity(chickenCreature);
        PFInstanceSpace instanceSpace = new PFInstanceSpace(instance);

        final HydrazinePathFinder pathFinder = new HydrazinePathFinder(pathingEntity, instanceSpace);

        final PathObject path = pathFinder.initiatePathTo(-10, 42, -10);

        System.out.println("path: "+path);

        for (Iterator<Vec3i> it = path.iterator(); it.hasNext(); ) {
            Vec3i ite = it.next();

            System.out.println("test: " + ite);

        }*/

        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }
}
