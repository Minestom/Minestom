package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
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
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

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
        final Entity entity = commandContext.get(entityClass).instantiate(commandContext.get(this.entity));
        //noinspection ConstantConditions - One couldn't possibly execute a command without being in an instance
        entity.setInstance(((Player) commandSender).getInstance(), commandContext.get(pos).fromSender(commandSender));

        final Entity zombie = new Entity(EntityType.ZOMBIE);
        zombie.setInstance(((Player) commandSender).getInstance(), new Pos(0, 40, 0));
        ((Player) commandSender).getInstance().setBlock(1, 40, 1, Block.STONE);

        MinecraftServer.getSchedulerManager()
            .buildTask(() -> {
                zombie.setVelocity(new Vec(10, 0, 10));
            })
            .delay(5, TimeUnit.SECOND)
            .schedule();
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
