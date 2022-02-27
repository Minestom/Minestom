package net.minestom.server.permission;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jglrxavpok.hephaistos.nbt.NBT;
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

    private Permission permission1, permission2, permission3;

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

        permission1 = new Permission("perm.name",
                NBT.Compound(nbt -> {
                    nbt.setString("name", "Minestom");
                    nbt.setInt("amount", 5);
                })
        );

        permission2 = new Permission("perm.name2");

        permission3 = new Permission("perm.test.*");
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
    public void hasPermissionWildcard() {
        assertFalse(player.hasPermission(permission3));

        player.addPermission(permission3);

        assertTrue(player.hasPermission(permission3));
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

    @AfterEach
    public void cleanup() {

    }
}
