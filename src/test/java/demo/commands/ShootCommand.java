package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.arrow.ArrowMeta;
import net.minestom.server.entity.type.projectile.EntityProjectile;

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

    private void defaultExecutor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Correct usage: shoot [default/spectral/colored]"));
    }

    private void onTypeError(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage(Component.text("SYNTAX ERROR: '" + exception.getInput() + "' should be replaced by 'default', 'spectral' or 'colored'"));
    }

    private void onShootCommand(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;
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
        var pos = player.getPosition().clone().add(0D, player.getEyeHeight(), 0D);
        //noinspection ConstantConditions - It should be impossible to execute a command without being in an instance
        projectile.setInstance(player.getInstance(), pos);
        var dir = pos.getDirection().multiply(30D);
        pos = pos.clone().add(dir.getX(), dir.getY(), dir.getZ());
        projectile.shoot(pos, 1D, 0D);
    }
}
