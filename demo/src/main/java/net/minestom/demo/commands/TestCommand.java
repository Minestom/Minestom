package net.minestom.demo.commands;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        var block = ArgumentType.BlockState("block");
        block.setCallback((sender, exception) -> exception.printStackTrace());

        setDefaultExecutor((sender, context) -> {
            sender.playSound(Sound.sound(Key.key("item.trumpet.doot"), Sound.Source.PLAYER, 1, 1));
            if (sender instanceof Player player) {
                AdventurePacketConvertor.createSoundPacket(Sound.sound(Key.key(SoundEvent.BLOCK_ANVIL_HIT.name()), Sound.Source.HOSTILE, 1, 1), player);
            }
        });
        addSyntax((sender, context) -> System.out.println("executed"), block);

    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Incorrect usage"));
    }

}
