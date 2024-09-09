package net.minestom.server.utils;

import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a namespaced ID
 * https://minecraft.wiki/w/Namespaced_ID
 */
public record NamespaceID(@NotNull String domain, @NotNull String path) implements CharSequence, Key {
    private static final String LEGAL_LETTERS = "[0123456789abcdefghijklmnopqrstuvwxyz_-]+";
    private static final String LEGAL_PATH_LETTERS = "[0123456789abcdefghijklmnopqrstuvwxyz./_-]+";

    public static @NotNull NamespaceID from(@NotNull String namespace) {
        final int index = namespace.indexOf(Key.DEFAULT_SEPARATOR);
        final String domain;
        final String path;
        if (index == -1) {
            domain = Key.MINECRAFT_NAMESPACE;
            path = namespace;
        } else {
            domain = namespace.substring(0, index);
            path = namespace.substring(index + 1);
        }
        return new NamespaceID(domain, path);
    }

    public static @NotNull NamespaceID from(@NotNull String domain, @NotNull String path) {
        return new NamespaceID(domain, path);
    }

    public static @NotNull NamespaceID from(@NotNull Key key) {
        return new NamespaceID(key.namespace(), key.value());
    }

    public NamespaceID {
        domain = domain.intern();
        path = path.intern();
        assert !domain.contains(".") && !domain.contains("/") : "Domain cannot contain a dot nor a slash character (" + asString() + ")";
        assert domain.matches(LEGAL_LETTERS) : "Illegal character in domain (" + asString() + "). Must match " + LEGAL_LETTERS;
        assert path.matches(LEGAL_PATH_LETTERS) : "Illegal character in path (" + asString() + "). Must match " + LEGAL_PATH_LETTERS;
    }

    @Override
    public int length() {
        return domain.length() + 1 + path.length();
    }

    @Override
    public char charAt(int index) {
        if (index < domain.length()) {
            return domain.charAt(index);
        } else if (index == domain.length()) {
            return ':';
        } else {
            return path.charAt(index - domain.length() - 1);
        }
    }

    @Override
    public @NotNull CharSequence subSequence(int start, int end) {
        return asString().subSequence(start, end);
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
        return domain + ':' + path;
    }

    @Override
    public @NotNull String toString() {
        return asString();
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
}
