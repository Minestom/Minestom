package permissions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO: more tests
public class TestPermissions {

    private Player player;

    @BeforeEach
    public void init() {
        MinecraftServer.init(); // for entity manager
        player = new Player(UUID.randomUUID(), "TestPlayer", null) {
            @Override
            protected void playerConnectionInit() {}

            @Override
            public boolean isOnline() {
                return false;
            }
        };
    }

    @Test
    public void noPermission() {
        assertFalse(player.hasPermission(Permission.class));
    }

    class PermTest1 implements Permission {
        @Override
        public boolean isValidFor(CommandSender commandSender) {
            return true;
        }
    }
    class PermTest2 implements Permission {
        @Override
        public boolean isValidFor(CommandSender commandSender) {
            return true;
        }
    }

    @Test
    public void hasPermissionClass() {
        assertFalse(player.hasPermission(Permission.class));
        player.addPermission(new PermTest1());
        assertTrue(player.hasPermission(PermTest1.class));
        assertFalse(player.hasPermission(PermTest2.class));
        assertTrue(player.hasPermission(Permission.class)); // allow superclasses

        player.addPermission(new PermTest2());
        assertTrue(player.hasPermission(PermTest2.class));
    }

    class BooleanPerm implements Permission {
        private final boolean value;

        BooleanPerm(boolean v) {
            this.value = v;
        }

        @Override
        public boolean isValidFor(CommandSender commandSender) {
            return value;
        }
    }

    @Test
    public void hasTwoPermissionsOfSameClassButContradictEachOther() {
        player.addPermission(new BooleanPerm(true));
        assertTrue(player.hasPermission(BooleanPerm.class));
        player.addPermission(new BooleanPerm(false));
        assertFalse(player.hasPermission(BooleanPerm.class)); // all permissions must be valid
    }

    @Test
    public void singlePermission() {
        Permission p = commandSender -> true;
        player.addPermission(p);
        assertTrue(p.isValidFor(player));
        assertTrue(player.hasPermission(p));
        assertTrue(player.hasPermission(Permission.class));
    }

    @AfterEach
    public void cleanup() {

    }
}
