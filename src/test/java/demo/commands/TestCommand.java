package demo.commands;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.batch.BlockBatch;

import java.util.concurrent.ThreadLocalRandom;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        setDefaultExecutor((sender, args) -> {
            if (!sender.isPlayer()) {
                sender.sendMessage("This command may only be run by players.");
                return;
            }
            Player player = sender.asPlayer();

            BlockBatch batch = new BlockBatch((InstanceContainer) player.getInstance());

            int offset = 5;
            for (int x = 0; x < 50; x += 1) {
                for (int y = 0; y < 50; y += 1) {
                    for (int z = 0; z < 50; z += 1) {
                        batch.setBlockStateId(x + offset, y + offset+50, z + offset, (short) ThreadLocalRandom.current().nextInt(500));
                    }
                }
            }

            batch.flush(() -> sender.sendMessage(ColoredText.of(ChatColor.BRIGHT_GREEN, "Created cube.")));
        });
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }
}
