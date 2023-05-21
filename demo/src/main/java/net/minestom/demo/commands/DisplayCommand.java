package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class DisplayCommand extends Command {

    public DisplayCommand() {
        super("display");

        var follow = ArgumentType.Literal("follow");

        addSyntax(this::spawnItem, ArgumentType.Literal("item"));
        addSyntax(this::spawnBlock, ArgumentType.Literal("block"));
        addSyntax(this::spawnText, ArgumentType.Literal("text"));

        addSyntax(this::spawnItem, ArgumentType.Literal("item"), follow);
        addSyntax(this::spawnBlock, ArgumentType.Literal("block"), follow);
        addSyntax(this::spawnText, ArgumentType.Literal("text"), follow);
    }

    public void spawnItem(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player))
            return;

        var entity = new Entity(EntityType.ITEM_DISPLAY);
        var meta = (ItemDisplayMeta) entity.getEntityMeta();
        meta.setItemStack(ItemStack.of(Material.STICK));
        entity.setInstance(player.getInstance(), player.getPosition());

        if (context.has("follow")) {
            startSmoothFollow(entity, player);
        }
    }

    public void spawnBlock(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player))
            return;

        var entity = new Entity(EntityType.BLOCK_DISPLAY);
        var meta = (BlockDisplayMeta) entity.getEntityMeta();
        meta.setBlockState(Block.STONE_STAIRS.stateId());
        entity.setInstance(player.getInstance(), player.getPosition());

        if (context.has("follow")) {
            startSmoothFollow(entity, player);
        }
    }

    public void spawnText(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof Player player))
            return;

        var entity = new Entity(EntityType.TEXT_DISPLAY);
        var meta = (TextDisplayMeta) entity.getEntityMeta();
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setText(Component.text("Hello, world!"));
        entity.setInstance(player.getInstance(), player.getPosition());

        if (context.has("follow")) {
            startSmoothFollow(entity, player);
        }
    }

    private void startSmoothFollow(@NotNull Entity entity, @NotNull Player player) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            var meta = (AbstractDisplayMeta) entity.getEntityMeta();
            meta.setNotifyAboutChanges(false);
            meta.setInterpolationStartDelta(1);
            meta.setInterpolationDuration(20);
            meta.setTranslation(player.getPosition().sub(entity.getPosition()));
            meta.setNotifyAboutChanges(true);
        }).delay(20, TimeUnit.SERVER_TICK).repeat(20, TimeUnit.SERVER_TICK).schedule();
    }
}
