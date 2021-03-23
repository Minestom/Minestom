package permissions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import net.minestom.server.permission.verifier.PermissionVerifier;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
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

    private Permission permission1, permission2;

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
                new NBTCompound()
                        .setString("name", "Minestom")
                        .setInt("amount", 5));

        permission2 = new Permission("perm.name2");
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

        assertTrue(PermissionVerifier.verifierValid(
                (permission, otherPermissions) -> {
                    if (!otherPermissions.contains(permission)) return false;
                    final String name = permission.getNBTData() != null ? permission.getNBTData().getString("name") : null;
                    return Objects.equals(name, "Minestom");
                }, new Permission("perm.name",
                        new NBTCompound()
                                .setString("name", "Minestom")
                                .setInt("amount", 5)), player.getAllPermissions()));

        player.addPermission(permission2);
        assertFalse(PermissionVerifier.verifierValid(
                (permission, otherPermissions) -> {
                    if (!otherPermissions.contains(permission)) return false;
                    return Objects.nonNull(permission.getNBTData());
                }, new Permission("perm.name"), player.getAllPermissions()));
    }

    @AfterEach
    public void cleanup() {

    }
}
