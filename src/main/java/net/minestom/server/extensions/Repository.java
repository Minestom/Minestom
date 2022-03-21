package net.minestom.server.extensions;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Repository permits RepositoryImpl {

    static @NotNull Repository newRepository(@NotNull String id, @NotNull String url) {
        return new RepositoryImpl(id, url);
    }

    static @Nullable Repository fromJson(@NotNull JsonElement json) {
        return RepositoryImpl.fromJson(json);
    }

    @NotNull String id();

    @NotNull String url();

}
