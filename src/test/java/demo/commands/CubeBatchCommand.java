package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.time.TimeUnit;

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
        InstanceContainer instance = (InstanceContainer) player.getInstance();

        applyChunkShape(instance);

//        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
//
//        int offset = 50;
//        for (int x = 0; x < 50; x += 2) {
//            for (int y = 0; y < 50; y += 2) {
//                for (int z = 0; z < 50; z += 2) {
//                    batch.setBlockStateId(x + offset, y + offset, z + offset, Block.STONE.getBlockId());
//                }
//            }
//        }
//
//        batch.apply(instance, () -> sender.sendMessage(ColoredText.of(ChatColor.BRIGHT_GREEN, "Created cube.")));
    }

    private void applyChunkShape(InstanceContainer instance) {


        for (int i = 0; i < 20; i++) {
            final ChunkBatch relBatch = new ChunkBatch();

            for (int x = 0; x < 16; x += 2) {
                for (int y = 0; y < 50; y += 2) {
                    for (int z = 0; z < 16; z += 2) {
                        relBatch.setBlockStateId(x, y + 50, z, (short) i);
                    }
                }
            }
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                relBatch.apply(instance,
                        ThreadLocalRandom.current().nextInt(10) - 5,
                        ThreadLocalRandom.current().nextInt(10) - 5,
                        null);
            }).delay(10, TimeUnit.TICK).repeat(1, TimeUnit.TICK).schedule();
        }


    }
}
