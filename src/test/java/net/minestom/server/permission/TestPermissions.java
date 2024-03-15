package net.minestom.server.permission;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO: more tests
public class TestPermissions {

    private Player player;

    private String permission1, permission2, permission3, wildcard;

    @BeforeEach
    public void init() {
        MinecraftServer.init(); // for entity manager
        player = new Player(UUID.randomUUID(), "TestPlayer", null) {
            @Override
            protected void playerConnectionInit() {
            }

            @Override
            public boolean isOnline() {
                return false;
            }
        };

        permission1 = "perm.name";

        permission2 = "perm.name2";

        permission3 = "perm.name2.sub.sub2";

        wildcard = "*";
    }

    @Test
    public void noPermission() {
        assertFalse(player.getPermissionHandler().hasPermission(""));
        assertFalse(player.getPermissionHandler().hasPermission("random.permission"));
    }

    @Test
    public void hasPermissionClass() {

        assertFalse(player.getPermissionHandler().hasPermission(permission1));
        player.getPermissionHandler().addPermission(permission1);
        assertTrue(player.getPermissionHandler().hasPermission(permission1));
        assertFalse(player.getPermissionHandler().hasPermission(permission2));

        player.getPermissionHandler().addPermission(permission2);
        assertTrue(player.getPermissionHandler().hasPermission(permission2));
    }

    @Test
    public void hasPatternMatchingWildcard() {
        String permission = "foo.b*r.baz";
        String match = "foo.baaar.baz";
        String match2 = "foo.br.baz";
        String match3 = "foo.br.baz";
        String match4 = "foo.baaar.baz";
        String nomatch = "foo.br.bz";
        String nomatch2 = "foo.b.baz";
        assertFalse(player.getPermissionHandler().hasPermission(match));
        assertFalse(player.getPermissionHandler().hasPermission(match2));
        assertFalse(player.getPermissionHandler().hasPermission(nomatch));
        assertFalse(player.getPermissionHandler().hasPermission(nomatch2));

        player.getPermissionHandler().addPermission(permission);

        assertTrue(player.getPermissionHandler().hasPermission(match));
        assertTrue(player.getPermissionHandler().hasPermission(match2));
        assertTrue(player.getPermissionHandler().hasPermission(match3));
        assertTrue(player.getPermissionHandler().hasPermission(match4));
        assertFalse(player.getPermissionHandler().hasPermission(nomatch));
        assertFalse(player.getPermissionHandler().hasPermission(nomatch2));
    }

    @Test
    public void hasPermissionWildcard() {
        String permission = "foo.b*";
        String match = "foo.baaar.baz";
        String match2 = "foo.b";
        String match3 = "foo.b";
        String match4 = "foo.baaar.baz";
        String nomatch = "foo.";
        String nomatch2 = "foo/b";
        assertFalse(player.getPermissionHandler().hasPermission(match));
        assertFalse(player.getPermissionHandler().hasPermission(match2));
        assertFalse(player.getPermissionHandler().hasPermission(nomatch));
        assertFalse(player.getPermissionHandler().hasPermission(nomatch2));

        player.getPermissionHandler().addPermission(permission);

        assertTrue(player.getPermissionHandler().hasPermission(match));
        assertTrue(player.getPermissionHandler().hasPermission(match2));
        assertTrue(player.getPermissionHandler().hasPermission(match3));
        assertTrue(player.getPermissionHandler().hasPermission(match4));
        assertFalse(player.getPermissionHandler().hasPermission(nomatch));
        assertFalse(player.getPermissionHandler().hasPermission(nomatch2));
    }

    @Test
    public void hasAllPermissionsWithWildcard() {
        assertFalse(player.getPermissionHandler().hasPermission(permission2));
        assertFalse(player.getPermissionHandler().hasPermission(permission3));
        player.getPermissionHandler().addPermission(wildcard);
        assertTrue(player.getPermissionHandler().hasPermission(permission2));
        assertTrue(player.getPermissionHandler().hasPermission(permission3));
    }

    @AfterEach
    public void cleanup() {

    }
}