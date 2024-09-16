package net.minestom.server.permission;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.GameProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO: more tests
public class TestPermissions {

    private Player player;

    private Permission permission1, permission2, permission3, wildcard;

    @BeforeEach
    public void init() {
        MinecraftServer.init(); // for entity manager
        player = new Player(null, new GameProfile(UUID.randomUUID(), "TestPlayer")) {
            @Override
            protected void playerConnectionInit() {
            }

            @Override
            public boolean isOnline() {
                return false;
            }
        };

        permission1 = new Permission("perm.name",
                CompoundBinaryTag.builder()
                        .putString("name", "Minestom")
                        .putInt("amount", 5)
                        .build()
        );

        permission2 = new Permission("perm.name2");

        permission3 = new Permission("perm.name2.sub.sub2");

        wildcard = new Permission("*");
    }

    @Test
    public void noPermission() {
        assertFalse(player.hasPermission(""));
        assertFalse(player.hasPermission("random.permission"));
    }

    @Test
    public void hasPermissionClass() {

        assertFalse(player.hasPermission(permission1));
        player.addPermission(permission1);
        assertTrue(player.hasPermission(permission1));
        assertFalse(player.hasPermission(permission2));

        player.addPermission(permission2);
        assertTrue(player.hasPermission(permission2));
    }

    @Test
    public void hasPermissionNameNbt() {
        player.addPermission(permission1);
        assertTrue(player.hasPermission("perm.name"));
        assertTrue(player.hasPermission("perm.name",
                nbtCompound -> {
                    final String name = nbtCompound != null ? nbtCompound.getString("name") : null;
                    return Objects.equals(name, "Minestom");
                }));

        player.addPermission(permission2);
        assertFalse(player.hasPermission("perm.name2", Objects::nonNull));
    }

    @Test
    public void hasPatternMatchingWildcard() {
        Permission permission = new Permission("foo.b*r.baz");
        Permission match = new Permission("foo.baaar.baz");
        Permission match2 = new Permission("foo.br.baz");
        String match3 = "foo.br.baz";
        String match4 = "foo.baaar.baz";
        Permission nomatch = new Permission("foo.br.bz");
        Permission nomatch2 = new Permission("foo.b.baz");
        assertFalse(player.hasPermission(match));
        assertFalse(player.hasPermission(match2));
        assertFalse(player.hasPermission(nomatch));
        assertFalse(player.hasPermission(nomatch2));

        player.addPermission(permission);

        assertTrue(player.hasPermission(match));
        assertTrue(player.hasPermission(match2));
        assertTrue(player.hasPermission(match3));
        assertTrue(player.hasPermission(match4));
        assertFalse(player.hasPermission(nomatch));
        assertFalse(player.hasPermission(nomatch2));
    }

    @Test
    public void hasPermissionWildcard() {
        Permission permission = new Permission("foo.b*");
        Permission match = new Permission("foo.baaar.baz");
        Permission match2 = new Permission("foo.b");
        String match3 = "foo.b";
        String match4 = "foo.baaar.baz";
        Permission nomatch = new Permission("foo.");
        Permission nomatch2 = new Permission("foo/b");
        assertFalse(player.hasPermission(match));
        assertFalse(player.hasPermission(match2));
        assertFalse(player.hasPermission(nomatch));
        assertFalse(player.hasPermission(nomatch2));

        player.addPermission(permission);

        assertTrue(player.hasPermission(match));
        assertTrue(player.hasPermission(match2));
        assertTrue(player.hasPermission(match3));
        assertTrue(player.hasPermission(match4));
        assertFalse(player.hasPermission(nomatch));
        assertFalse(player.hasPermission(nomatch2));
    }

    @Test
    public void hasAllPermissionsWithWildcard() {
        assertFalse(player.hasPermission(permission2));
        assertFalse(player.hasPermission(permission3));
        player.addPermission(wildcard);
        assertTrue(player.hasPermission(permission2));
        assertTrue(player.hasPermission(permission3));
    }

    @AfterEach
    public void cleanup() {

    }
}
