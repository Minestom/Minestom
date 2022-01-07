package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

import java.util.function.Predicate;

public class PermissionDefault implements Predicate<PermissionHandler> {

    public static final PermissionDefault TRUE = new PermissionDefault(it -> true);

    public static final PermissionDefault OP = newOP(1);

    private static final PermissionDefault OP2 = newOP(2);
    private static final PermissionDefault OP3 = newOP(3);
    private static final PermissionDefault OP4 = newOP(4);

    public static PermissionDefault OP(int level) {
        return switch(level) {
            case 1 -> OP;
            case 2 -> OP2;
            case 3 -> OP3;
            case 4 -> OP4;
            default -> newOP(level);
        };
    }

    private static PermissionDefault newOP(int level) {
        return new PermissionDefault(permissionHandler -> permissionHandler instanceof Entity && (!(permissionHandler instanceof Player) || ((Player) permissionHandler).getPermissionLevel() >= level));
    }

    private final Predicate<PermissionHandler> permissionPredicate;

    private PermissionDefault(Predicate<PermissionHandler> permissionPredicate) {
        this.permissionPredicate = permissionPredicate;
    }

    @Override
    public boolean test(PermissionHandler permissionHandler) {
        return permissionPredicate.test(permissionHandler);
    }
}
