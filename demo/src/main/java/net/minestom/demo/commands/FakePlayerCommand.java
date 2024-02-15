package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.fakeplayer.FakePlayerOption;

import java.util.UUID;

public class FakePlayerCommand extends Command {

    public FakePlayerCommand() {
        super("fakeplayer");
        setCondition(Conditions::playerOnly);
        var usernameArg = ArgumentType.String("username");
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /" + getName() + " <" + usernameArg.getId() + ">"));
        addSyntax((sender, context) -> {
            Player p = (Player) sender;
            String username = context.get(usernameArg);
            FakePlayer.initPlayer(
                    UUID.randomUUID(), username,
                    new FakePlayerOption().setInTabList(true).setRegistered(true),
                    fp -> p.sendMessage(Component.text(fp.getUsername() + " spawned!", NamedTextColor.GREEN))
            );
        }, usernameArg);
    }

}
