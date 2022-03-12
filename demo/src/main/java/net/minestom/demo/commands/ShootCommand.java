package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.arrow.ArrowMeta;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class ShootCommand extends Command {

    public ShootCommand() {
        super("shoot");
        setCondition(Conditions::playerOnly);
        setDefaultExecutor(this::defaultExecutor);
        var typeArg = ArgumentType.Word("type").from("default", "spectral", "colored");
        setArgumentCallback(this::onTypeError, typeArg);
        addSyntax(this::onShootCommand, typeArg);
    }

    private void defaultExecutor(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        origin.sender().sendMessage(Component.text("Correct usage: shoot [default/spectral/colored]"));
    }

    private void onTypeError(@NotNull CommandOrigin origin, @NotNull CommandException exception) {
        origin.sender().sendMessage(Component.text("Expected 'default', 'spectral' or 'colored'", NamedTextColor.RED));
        origin.sender().sendMessage(exception.generateContextMessage());
    }

    private void onShootCommand(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        Player player = (Player) origin.entity();
        String mode = context.get("type");
        EntityProjectile projectile;
        switch (mode) {
            case "default":
                projectile = new EntityProjectile(player, EntityType.ARROW);
                break;
            case "spectral":
                projectile = new EntityProjectile(player, EntityType.SPECTRAL_ARROW);
                break;
            case "colored":
                projectile = new EntityProjectile(player, EntityType.ARROW);
                var meta = (ArrowMeta) projectile.getEntityMeta();
                meta.setColor(ThreadLocalRandom.current().nextInt());
                break;
            default:
                return;
        }
        var pos = player.getPosition().add(0D, player.getEyeHeight(), 0D);
        //noinspection ConstantConditions - It should be impossible to execute a command without being in an instance
        projectile.setInstance(player.getInstance(), pos);
        var dir = pos.direction().mul(30D);
        pos = pos.add(dir);
        projectile.shoot(pos, 1D, 0D);
    }
}
