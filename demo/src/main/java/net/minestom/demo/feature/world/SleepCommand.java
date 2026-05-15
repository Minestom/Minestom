package net.minestom.demo.feature.world;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class SleepCommand extends Command {

    public SleepCommand() {
        super("sleep");

        setCondition(Conditions::playerOnly);
        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            player.enterBed(player.getPosition());
        });

    }
}
