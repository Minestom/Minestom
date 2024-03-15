package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BasePermissionHandler implements PermissionHandler {
    private final Map<String, Boolean> allPermissions = new HashMap<>();

    @Override
    public void setPermission(@NotNull String permission, PermissionTristate tristate) {
        Boolean value = tristate.getValue();
        if (value != null) {
            allPermissions.put(permission, value);
        } else {
            allPermissions.remove(permission);
        }
    }

    @Override
    public PermissionTristate checkPermission(@NotNull String permission) {
        for (Map.Entry<String, Boolean> permissionEntry : allPermissions.entrySet()) {
            String permissionLoopName = permissionEntry.getKey();
            if (permissionLoopName.equals(permission)) {
                return PermissionTristate.fromBoolean(permissionEntry.getValue());
            }
            if (permissionLoopName.contains("*")) {
                // Sanitize permissionLoopName
                String regexSanitized = Pattern.quote(permissionLoopName).replace("*", "\\E(.*)\\Q"); // Replace * with regex
                // pattern matching for wildcards, where foo.b*r.baz matches foo.baaaar.baz or foo.bar.baz
                if (permission.matches(regexSanitized)) {
                    return PermissionTristate.fromBoolean(permissionEntry.getValue());
                }
            }
        }

        return PermissionTristate.UNDEFINED;
    }
}