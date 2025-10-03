package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.minecart.AbstractMinecartMeta;
import net.minestom.server.instance.block.Block;

public class MinecartCommand extends Command {

    private final Argument<Type> type = ArgumentType.Enum("type", Type.class);
    private final Argument<Block> block = ArgumentType.BlockState("block").setDefaultValue(Block.AIR);
    private final Argument<Integer> offset = ArgumentType.Integer("offset").setDefaultValue(6);

    public MinecartCommand() {
        super("minecart");

        setCondition(Conditions::playerOnly);
        addSyntax(this::execute, type, block, offset);
    }

    private void execute(CommandSender sender, CommandContext context) {
        var player = (Player) sender;

        var minecart = new Entity(switch (context.get(type)) {
            case NORMAL -> EntityType.MINECART;
            case CHEST -> EntityType.CHEST_MINECART;
            case FURNACE -> EntityType.FURNACE_MINECART;
            case TNT -> EntityType.TNT_MINECART;
            case HOPPER -> EntityType.HOPPER_MINECART;
            case SPAWNER -> EntityType.SPAWNER_MINECART;
            case COMMAND_BLOCK -> EntityType.COMMAND_BLOCK_MINECART;
        });
        var meta = (AbstractMinecartMeta) minecart.getEntityMeta();
        meta.setCustomBlockState(context.get(block));
        meta.setCustomBlockYPosition(context.get(offset));

        minecart.setInstance(player.getInstance(), player.getPosition().withView(0f, 0f));
    }

    private enum Type {
        NORMAL,
        CHEST,
        FURNACE,
        TNT,
        HOPPER,
        SPAWNER,
        COMMAND_BLOCK,
    }
}
