package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class DisplaynameCommand extends Command {

    public DisplaynameCommand(){
        super("displayname");

        addSyntax((sender, context) -> {
            setCondition(Conditions::playerOnly);
            Component displayname = context.get("displayname");
            ((Player)sender).setDisplayName(displayname);
            sender.sendMessage("DisplayName Updated.");
        }, ArgumentType.Component("displayname"));
    }
}
