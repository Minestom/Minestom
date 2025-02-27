package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.instance.block.Block;

public class EntitySharedInstanceCommand extends Command {
    private static final Point SPAWN_POINT = new Vec(0, 42, 0);

    private InstanceContainer container;
    private Entity mob;

    public EntitySharedInstanceCommand() {
        super("entityshared");
        setCondition((sender, commandString) -> sender instanceof Player);
        setDefaultExecutor((sender, context) -> sender.sendMessage("/entityshared <init/create_shared/goto_original_container/spawn_mob/push_mob/remove_mob>"));
        Argument<Option> optionArgument = new ArgumentEnum<>("option", Option.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            switch (context.get(optionArgument)) {
                case INIT -> {
                    if (container != null) {
                        sender.sendMessage("Original container has already been initialized!");
                        return;
                    }
                    container = MinecraftServer.getInstanceManager().createInstanceContainer();
                    container.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
                    container.setChunkSupplier(LightingChunk::new);
                    sender.sendMessage("Original container has been initialized successfully.");
                }
                case CREATE_SHARED -> {
                    if (container == null) {
                        sender.sendMessage("There is no original container! Run /entityshared init");
                        return;
                    }
                    SharedInstance sharedInstance = MinecraftServer.getInstanceManager().createSharedInstance(container);
                    sharedInstance.setSharesEntities(true);
                    player.setInstance(sharedInstance, SPAWN_POINT).whenComplete((unused, throwable) -> {
                        if (throwable != null) return;
                        sender.sendMessage("You are now in an entity shared instance!");
                    });
                }
                case GOTO_ORIGINAL_CONTAINER -> {
                    if (container == null) {
                        sender.sendMessage("There is no original container! Run /entityshared init");
                        return;
                    }
                    player.setInstance(container, SPAWN_POINT).whenComplete((unused, throwable) -> {
                        if (throwable != null) return;
                        sender.sendMessage("You are now in the original container!");
                    });
                }
                case SPAWN_MOB -> {
                    if (mob != null && !mob.isRemoved()) {
                        sender.sendMessage("There is already a mob! Run /entityshared remove_mob");
                        return;
                    }
                    mob = new Entity(EntityType.PIG);
                    mob.setInstance(container, SPAWN_POINT).whenComplete((unused, throwable) -> {
                        if (throwable != null) return;
                        sender.sendMessage("A mob has been spawned.");
                    });
                }
                case PUSH_MOB -> {
                    if (mob == null || mob.isRemoved()) {
                        sender.sendMessage("There is no mob in the original container! Run /entityshared spawn_mob");
                        return;
                    }
                    mob.setVelocity(new Vec(3, 6));
                    sender.sendMessage("The mob has been pushed.");
                }
                case REMOVE_MOB -> {
                    if (mob == null) {
                        sender.sendMessage("There is no mob in the original container! Run /entityshared spawn_mob");
                        return;
                    }
                    mob.remove();
                    sender.sendMessage("The mob has been removed from the original container.");
                }
            }
        }, optionArgument);
    }

    enum Option {
        INIT,
        CREATE_SHARED,
        GOTO_ORIGINAL_CONTAINER,
        SPAWN_MOB,
        PUSH_MOB,
        REMOVE_MOB
    }
}
