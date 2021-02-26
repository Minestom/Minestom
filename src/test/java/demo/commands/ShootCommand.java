package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.arrow.ArrowMeta;
import net.minestom.server.entity.type.Projectile;
import net.minestom.server.entity.type.projectile.EntityArrow;
import net.minestom.server.entity.type.projectile.EntitySpectralArrow;

import java.util.concurrent.ThreadLocalRandom;

public class ShootCommand extends Command {

    public ShootCommand() {
        super("shoot");
        setCondition(this::condition);
        setDefaultExecutor(this::defaultExecutor);
        var typeArg = ArgumentType.Word("type").from("default", "spectral", "colored");
        setArgumentCallback(this::onTypeError, typeArg);
        addSyntax(this::onShootCommand, typeArg);
    }

    private boolean condition(CommandSender sender, String commandString) {
        if (!sender.isPlayer()) {
            sender.sendMessage("The command is only available for player");
            return false;
        }
        return true;
    }

    private void defaultExecutor(CommandSender sender, Arguments args) {
        sender.sendMessage("Correct usage: shoot [default/spectral/colored]");
    }

    private void onTypeError(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage("SYNTAX ERROR: '" + exception.getInput() + "' should be replaced by 'default', 'spectral' or 'colored'");
    }

    private void onShootCommand(CommandSender sender, Arguments args) {
        Player     player = (Player) sender;
        String     mode   = args.getWord("type");
        Projectile projectile;
        var        pos    = player.getPosition().clone().add(0D, player.getEyeHeight(), 0D);
        switch (mode) {
            case "default":
                projectile = new EntityArrow(player, pos);
                break;
            case "spectral":
                projectile = new EntitySpectralArrow(player, pos);
                break;
            case "colored":
                projectile = new EntityArrow(player, pos);
                var meta = (ArrowMeta) ((Entity) projectile).getEntityMeta();
                meta.setColor(ThreadLocalRandom.current().nextInt());
                break;
            default:
                return;
        }
        ((Entity) projectile).setInstance(player.getInstance());
        var dir = pos.getDirection().multiply(30D);
        pos = pos.clone().add(dir.getX(), dir.getY(), dir.getZ());
        Projectile.shoot(projectile, player, pos, 1D, 0D);
    }
}
