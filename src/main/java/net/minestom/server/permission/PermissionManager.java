package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PermissionManager {

    private final List<String> permissionStrings = new ArrayList<>();
    private final List<PermissionDescriptor> permissionDescriptors = new ArrayList<>();

    public void registerPermission(PermissionDescriptor permissionDescriptor) {
        permissionStrings.add(permissionDescriptor.getName());
        permissionDescriptors.add(permissionDescriptor);
    }

    public @NotNull Collection<@NotNull String> getPermissionStrings() {
        return Collections.unmodifiableCollection(permissionStrings);
    }

    public @NotNull Collection<@NotNull PermissionDescriptor> getPermissionDescriptors() {
        return Collections.unmodifiableCollection(permissionDescriptors);
    }

}
