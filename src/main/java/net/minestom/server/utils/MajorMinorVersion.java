package net.minestom.server.utils;

import org.jetbrains.annotations.NotNull;

public record MajorMinorVersion(int major, int minor) {

    @Override
    public @NotNull String toString() {
        return major + "." + minor;
    }
}
