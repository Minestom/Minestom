package net.minestom.server.utils;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a namespaced ID
 * https://minecraft.gamepedia.com/Namespaced_ID
 */
public final class NamespaceID implements CharSequence, Key {
    private static final Map<Integer, NamespaceID> cache = new ConcurrentHashMap<>();
    private static final String legalLetters = "[0123456789abcdefghijklmnopqrstuvwxyz_-]+";
    private static final String legalPathLetters = "[0123456789abcdefghijklmnopqrstuvwxyz./_-]+";

    private final String domain;
    private final String path;
    private final String full;

    /**
     * Extracts the domain from the namespace ID. "minecraft:stone" would return "minecraft".
     * If no ':' character is found, "minecraft" is returned.
     *
     * @param namespaceID the namespace id to get the domain from
     * @return the domain of the namespace ID
     */
    public static @NotNull String getDomain(@NotNull String namespaceID) {
        final int index = namespaceID.indexOf(':');
        if (index < 0)
            return "minecraft";
        assert namespaceID.indexOf(':', index + 1) == -1 : "Namespace ID can only have at most one colon ':' (" + namespaceID + ")";
        return namespaceID.substring(0, index);
    }

    /**
     * Extracts the path from the namespace ID. "minecraft:blocks/stone" would return "blocks/stone".
     * If no ':' character is found, the <pre>namespaceID</pre> is returned.
     *
     * @param namespaceID the namespace id to get the path from
     * @return the path of the namespace ID
     */
    public static @NotNull String getPath(@NotNull String namespaceID) {
        final int index = namespaceID.indexOf(':');
        if (index < 0)
            return namespaceID;
        assert namespaceID.indexOf(':', index + 1) == -1 : "Namespace ID can only have at most one colon ':' (" + namespaceID + ")";
        return namespaceID.substring(index + 1);
    }

    static int hash(String domain, String path) {
        return Objects.hash(domain, path);
    }

    public static NamespaceID from(String domain, String path) {
        final int hash = hash(domain, path);
        return cache.computeIfAbsent(hash, _unused -> new NamespaceID(domain, path));
    }

    public static NamespaceID from(String id) {
        return from(getDomain(id), getPath(id));
    }

    public static NamespaceID from(Key key) {
        return from(key.asString());
    }

    private NamespaceID(String domain, String path) {
        this.domain = domain;
        this.path = path;
        this.full = toString();
        validate();
    }

    private void validate() {
        assert !domain.contains(".") && !domain.contains("/") : "Domain cannot contain a dot nor a slash character (" + full + ")";
        assert domain.matches(legalLetters) : "Illegal character in domain (" + full + "). Must match " + legalLetters;
        assert path.matches(legalPathLetters) : "Illegal character in path (" + full + "). Must match " + legalPathLetters;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
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
        return hash(domain, path);
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
        return domain + ":" + path;
    }

    @Override
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
}
