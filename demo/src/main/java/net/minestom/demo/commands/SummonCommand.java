package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SummonCommand extends Command {

    private final ArgumentEntityType entity;
    private final Argument<RelativeVec> pos;
    private final Argument<EntityClass> entityClass;

    public SummonCommand() {
        super("summon");
        setCondition(Conditions::playerOnly);

        entity = ArgumentType.EntityType("entity type");
        pos = ArgumentType.RelativeVec3("pos").setDefaultValue(() -> new RelativeVec(
                new Vec(0, 0, 0),
                RelativeVec.CoordinateType.RELATIVE,
                true, true, true
        ));
        entityClass = ArgumentType.Enum("class", EntityClass.class)
                .setFormat(ArgumentEnum.Format.LOWER_CASED)
                .setDefaultValue(EntityClass.CREATURE);
        addSyntax(this::execute, entity, pos, entityClass);
        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /summon <type> <x> <y> <z> <class>"));
    }

    private void execute(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        var instance = ((Player) commandSender).getInstance();
        instance.setBlock(1, 42, -1, Block.STONE);
        var entity = new Entity(EntityType.ZOMBIE);

        entity.setInstance(instance, new Pos(-0.3, 42, -0.3)).join();
        entity.setVelocity(new Vec(10, 0, -10));

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            System.out.println(entity.getPosition());
        }).repeat(60, TimeUnit.SERVER_TICK).schedule();
    }

    @SuppressWarnings("unused")
    enum EntityClass {
        BASE(Entity::new),
        LIVING(LivingEntity::new),
        CREATURE(EntityCreature::new);
        private final EntityFactory factory;

        EntityClass(EntityFactory factory) {
            this.factory = factory;
        }

        public Entity instantiate(EntityType type) {
            return factory.newInstance(type);
        }
    }

    interface EntityFactory {
        Entity newInstance(EntityType type);
    }
}
