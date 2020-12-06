package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.utils.Position;

import java.util.UUID;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        Argument test = ArgumentType.RelativeBlockPosition("test");

        setDefaultExecutor((source, args) -> {
            System.gc();
            source.sendMessage("Explicit GC executed!");
        });

        addSyntax((source, args) -> {
            System.out.println("hey");

            SpawnEntityPacket spawnEntityPacket = new SpawnEntityPacket();
            spawnEntityPacket.entityId = Entity.generateId();
            spawnEntityPacket.uuid = UUID.randomUUID();
            spawnEntityPacket.type = 41;
            spawnEntityPacket.position = new Position(0, 40, 0);

            source.asPlayer().getPlayerConnection().sendPacket(spawnEntityPacket);

        }, test);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
