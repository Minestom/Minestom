package net.minestom.server.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a namespaced ID
 * https://minecraft.wiki/w/Namespaced_ID
 *
 * @deprecated use {@link Key#key()}
 */
@Deprecated
public final class NamespaceID implements CharSequence, Key {
    private static final String legalLetters = "[0123456789abcdefghijklmnopqrstuvwxyz_-]+";
    private static final String legalPathLetters = "[0123456789abcdefghijklmnopqrstuvwxyz./_-]+";
    private static final Cache<String, NamespaceID> CACHE = Caffeine.newBuilder().weakKeys().weakValues().build();

    private final String domain;
    private final String path;
    private final String full;

    public static @NotNull NamespaceID from(@NotNull String namespace) {
        return CACHE.get(namespace, id -> {
            final int index = id.indexOf(':');
            final String domain;
            final String path;
            if (index < 0) {
                domain = "minecraft";
                path = id;
                id = "minecraft:" + id;
            } else {
                domain = id.substring(0, index);
                path = id.substring(index + 1);
            }
            return new NamespaceID(id, domain, path);
        });
    }

    public static @NotNull NamespaceID from(@NotNull String domain, @NotNull String path) {
        return from(domain + ":" + path);
    }

    public static @NotNull NamespaceID from(@NotNull Key key) {
        return from(key.asString());
    }

    private NamespaceID(String full, String domain, String path) {
        this.full = full;
        this.domain = domain;
        this.path = path;
        assert !domain.contains(".") && !domain.contains("/") : "Domain cannot contain a dot nor a slash character (" + full + ")";
        assert domain.matches(legalLetters) : "Illegal character in domain (" + full + "). Must match " + legalLetters;
        assert path.matches(legalPathLetters) : "Illegal character in path (" + full + "). Must match " + legalPathLetters;
    }

    public @NotNull String domain() {
        return domain;
    }

    public @NotNull String path() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof final Key that)) return false;
        return Objects.equals(this.domain, that.namespace()) && Objects.equals(this.path, that.value());
    }

    @Override
    public int hashCode() {
        int result = this.domain.hashCode();
        result = (31 * result) + this.path.hashCode();
        return result;
    }

    @Override
    public int length() {
        return full.length();
    }

    @Override
    public char charAt(int index) {
        return full.charAt(index);
    }

    @Override
    public @NotNull CharSequence subSequence(int start, int end) {
        return full.subSequence(start, end);
    }

    @Override
    public @NotNull String toString() {
        return full;
    }

    @Override
    @Pattern("[a-z0-9_\\-.]+")
    public @NotNull String namespace() {
        return this.domain;
    }

    @Override
    public @NotNull String value() {
        return this.path;
    }

    @Override
    public @NotNull String asString() {
        return this.full;
    }

    @Deprecated
    public String getDomain() {
        return domain();
    }

    @Deprecated
    public String getPath() {
        return path();
    }
}
