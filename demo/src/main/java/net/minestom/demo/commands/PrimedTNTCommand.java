package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.block.Block;

public class PrimedTNTCommand extends Command {
    public PrimedTNTCommand() {
        super("primedtnt");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            Entity entity = new Entity(EntityType.TNT);
            entity.editEntityMeta(PrimedTntMeta.class, meta -> {
                meta.setFuseTime(60);
                meta.setBlockState(Block.STONE);
            });

            entity.setInstance(player.getInstance(), player.getPosition());
        });

    }
}
