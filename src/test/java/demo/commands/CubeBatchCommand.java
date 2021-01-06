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

public class CubeBatchCommand extends Command {

    public CubeBatchCommand() {
        super("cube");

        setDefaultExecutor(this::execute);
    }

    private void execute(CommandSender sender, Arguments args) {
        if (!sender.isPlayer()) {
            sender.sendMessage("This command may only be run by players.");
            return;
        }
        Player player = sender.asPlayer();

        BlockBatch batch = new BlockBatch((InstanceContainer) player.getInstance());

        int offset = 50;
        for (int x = 0; x < 50; x += 2) {
            for (int y = 0; y < 50; y += 2) {
                for (int z = 0; z < 50; z += 2) {
                    batch.setBlockStateId(x + offset, y + offset, z + offset, (short) ThreadLocalRandom.current().nextInt(500));
                }
            }
        }

        batch.flush(() -> sender.sendMessage(ColoredText.of(ChatColor.BRIGHT_GREEN, "Created cube.")));
    }
}
