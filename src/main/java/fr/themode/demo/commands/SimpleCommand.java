package fr.themode.demo.commands;

import fr.themode.demo.generator.ChunkGeneratorDemo;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.Dimension;

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
        player.sendMessage("Everyone come at you!");

        Instance instance = player.getInstance();

        /*for (EntityCreature creature : instance.getCreatures()) {
            creature.setPathTo(player.getPosition());
        }*/

        Instance inst = MinecraftServer.getInstanceManager().createInstanceContainer(Dimension.NETHER);
        inst.enableAutoChunkLoad(true);
        inst.setChunkGenerator(new ChunkGeneratorDemo());

        player.setInstance(inst);

        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }
}
