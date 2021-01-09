package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.batch.BatchOption;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.batch.RelativeBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.time.TimeUnit;

import java.util.concurrent.ThreadLocalRandom;

public class CubeBatchCommand extends Command {

    public CubeBatchCommand() {
        super("cube");

        Argument<String> subcommand = ArgumentType.Word("sub").from("create", "undo");

        setDefaultExecutor(this::execute);

        addSyntax(this::handleSubcommand, subcommand);
    }

    private void execute(CommandSender sender, Arguments args) {
        if (!sender.isPlayer()) {
            sender.sendMessage("This command may only be run by players.");
            return;
        }
        Player player = sender.asPlayer();
        InstanceContainer instance = (InstanceContainer) player.getInstance();

//        applyChunkShape(instance);
        applyBlockShape(instance);

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

//        RelativeBlockBatch batch = new RelativeBlockBatch();
//        for (int x = 0; x < 50; x += 2) {
//            for (int y = 0; y < 50; y += 2) {
//                for (int z = 0; z < 50; z += 2) {
//                    batch.setBlockStateId(x, y, z, Block.STONE.getBlockId());
//                }
//            }
//        }
//        batch.apply(instance, 0, 50, 0, () -> sender.sendMessage(ColoredText.of(ChatColor.BRIGHT_GREEN, "Created cube.")));


    }

    private volatile ChunkBatch inverse = null;
    private volatile AbsoluteBlockBatch absInverse = null;

    private void handleSubcommand(CommandSender sender, Arguments args) {
        if (!sender.isPlayer()) {
            sender.sendMessage("This command may only be run by players.");
            return;
        }
        Player player = sender.asPlayer();
        InstanceContainer instance = (InstanceContainer) player.getInstance();

        String sub = args.getWord("sub");
        if (sub.equalsIgnoreCase("create")) {
            final ChunkBatch batch = new ChunkBatch(new BatchOption().setCalculateInverse(true));
            for (int x = 0; x < 16; x += 2) {
                for (int y = 0; y < 50; y += 2) {
                    for (int z = 0; z < 16; z += 2) {
                        batch.setBlockStateId(x, y + 50, z, Block.STONE.getBlockId());
                    }
                }
            }
            inverse = batch.apply(instance, 1, 1, c -> sender.sendMessage("Applied batch"));

            final AbsoluteBlockBatch absBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));
            for (int x = 0; x < 50; x += 2) {
                for (int y = 0; y < 50; y += 2) {
                    for (int z = 0; z < 50; z += 2) {
                        absBatch.setBlockStateId(x - 100, y + 50, z + 50, Block.STONE.getBlockId());
                    }
                }
            }
            absInverse = absBatch.apply(instance, () -> sender.sendMessage("Applied batch 2"));
        } else if (sub.equalsIgnoreCase("undo")) {
            final ChunkBatch inv = inverse;
            if (inv == null) {
                sender.sendMessage("No inverse set.");
                return;
            }

            inv.apply(instance, 1, 1, c -> sender.sendMessage("Applied inverse"));
            absInverse.apply(instance, () -> sender.sendMessage("Applied inverse 2"));
        } else sender.sendMessage("Unknown subcommand '" + sub + "'");
    }

    private void applyBlockShape(InstanceContainer instance) {
        for (int i = 0; i < 5; i++) {
            AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
            for (int x = -100; x < 101; x++) {
                for (int z = -100; z < 101; z++) {
                    batch.setBlockStateId(x, 50, z, (short) (Block.BLUE_CONCRETE.getBlockId() + i));
                }
            }
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                batch.apply(instance, null);
            }).delay(i * 3, TimeUnit.TICK).repeat(15, TimeUnit.TICK).schedule();
        }


        // Bad
//        for (int i = 0; i < 5; i++) {
//            RelativeBlockBatch batch = makeBatch((short) (Block.BLUE_CONCRETE.getBlockId() + i));
//            MinecraftServer.getSchedulerManager().buildTask(() -> {
//                batch.apply(instance,
//                        ThreadLocalRandom.current().nextInt(100) - 100,
//                        50,
//                        ThreadLocalRandom.current().nextInt(100) - 100,
//                        null);
//            }).delay(10, TimeUnit.TICK).repeat(1, TimeUnit.TICK).schedule();
//        }


//        for (int i = 0; i < 5; i++) {
//            RelativeBlockBatch batch = makeBatch((short) 0);
//            MinecraftServer.getSchedulerManager().buildTask(() -> {
//                batch.apply(instance,
//                        ThreadLocalRandom.current().nextInt(50) - 50,
//                        ThreadLocalRandom.current().nextInt(50) + 50,
//                        ThreadLocalRandom.current().nextInt(50) - 50,
//                        null);
//            }).delay(10, TimeUnit.TICK).repeat(1, TimeUnit.TICK).schedule();
//        }
//        for (int i = 0; i < 5; i++) {
//            RelativeBlockBatch batch = makeBatch((short) (Block.STONE.getBlockId() + i));
//            MinecraftServer.getSchedulerManager().buildTask(() -> {
//                batch.apply(instance,
//                        ThreadLocalRandom.current().nextInt(50) - 50,
//                        ThreadLocalRandom.current().nextInt(50) + 50,
//                        ThreadLocalRandom.current().nextInt(50) - 50,
//                        null);
//            }).delay(10, TimeUnit.TICK).repeat(1, TimeUnit.TICK).schedule();
//        }
    }

    private RelativeBlockBatch makeBatch(short block) {
        final RelativeBlockBatch batch = new RelativeBlockBatch();
        for (int x = 0; x < 100; x += 2) {
//            for (int y = 0; y < 50; y += 2) {
                for (int z = 0; z < 100; z += 2) {
                    batch.setBlockStateId(x, 0, z, block);
                }
//            }
        }
        return batch;
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
