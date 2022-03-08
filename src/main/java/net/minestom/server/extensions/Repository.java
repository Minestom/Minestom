package net.minestom.server.extensions;

import org.jetbrains.annotations.NotNull;

public interface Repository {

    @NotNull String id();

    @NotNull String url();

}
