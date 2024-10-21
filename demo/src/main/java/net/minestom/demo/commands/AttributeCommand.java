package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentResource;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.identity.NamedAndIdentified;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class AttributeCommand extends Command {
    public AttributeCommand() {
        super("attribute");

        ArgumentEntity target = ArgumentType.Entity("target").singleEntity(true);
        ArgumentResource attribute = ArgumentType.Resource("attribute", "minecraft:attribute");
        ArgumentLiteral base = ArgumentType.Literal("base");
        ArgumentLiteral get = ArgumentType.Literal("get");
        ArgumentLiteral set = ArgumentType.Literal("set");
        ArgumentDouble value = ArgumentType.Double("value");

        addSyntax(this::get, target, attribute, get);
        addSyntax(this::setBase, target, attribute, base, set, value);
        addSyntax(this::getBase, target, attribute, base, get);
    }

    private void setBase(CommandSender sender, CommandContext ctx) {
        LivingEntity target = target(sender, ctx);
        if (check(target, ctx, sender)) return;
        Attribute attribute = attribute(ctx);
        if (check(attribute, ctx, sender)) return;
        double value = value(ctx);
        target.getAttribute(attribute).setBaseValue(value);
        sender.sendMessage(translatable("commands.attribute.base_value.set.success").arguments(description(attribute), name(target), text(value)));
    }

    private void getBase(CommandSender sender, CommandContext ctx) {
        LivingEntity target = target(sender, ctx);
        if (check(target, ctx, sender)) return;
        Attribute attribute = attribute(ctx);
        if (check(attribute, ctx, sender)) return;
        double value = target.getAttribute(attribute).getBaseValue();
        sender.sendMessage(translatable("commands.attribute.base_value.get.success").arguments(description(attribute), name(target), text(value)));
    }

    private void get(CommandSender sender, CommandContext ctx) {
        LivingEntity target = target(sender, ctx);
        if (check(target, ctx, sender)) return;
        Attribute attribute = attribute(ctx);
        if (check(attribute, ctx, sender)) return;
        double value = target.getAttributeValue(attribute);
        sender.sendMessage(translatable("commands.attribute.value.get.success").arguments(description(attribute), name(target), text(value)));
    }

    private Component description(Attribute attribute) {
        return translatable(attribute.registry().translationKey());
    }

    private double value(CommandContext ctx) {
        return ctx.get("value");
    }

    private LivingEntity target(CommandSender sender, CommandContext ctx) {
        EntityFinder finder = ctx.get("target");
        Entity entity = finder.findFirstEntity(sender);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }
        return livingEntity;
    }

    @Nullable
    private Attribute attribute(CommandContext ctx) {
        String namespaceId = ctx.get("attribute");
        return Attribute.fromNamespaceId(namespaceId);
    }

    private Component name(Entity entity) {
        if (entity instanceof NamedAndIdentified named) {
            return named.getName();
        }
        return entity.getCustomName() == null ? entity.getCustomName() : text(entity.getEntityType().name());
    }

    @Contract("!null, _, _ -> false; null, _, _ -> true")
    private boolean check(@Nullable LivingEntity livingEntity, CommandContext ctx, CommandSender sender) {
        if (livingEntity == null) {
            Entity entity = ctx.get("target");
            sender.sendMessage(translatable("commands.attribute.failed.entity").arguments(name(entity)));
            return true;
        }
        return false;
    }

    @Contract("!null, _, _ -> false; null, _, _ -> true")
    private boolean check(@Nullable Attribute attribute, CommandContext ctx, CommandSender sender) {
        if (attribute == null) {
            sender.sendMessage(translatable("argument.resource.invalid_type").arguments(text(ctx.<String>get("attribute")), text("minecraft:attribute"), text("minecraft:attribute")));
            return true;
        }
        return false;
    }
}
