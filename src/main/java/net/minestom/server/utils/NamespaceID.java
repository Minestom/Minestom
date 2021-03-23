package net.minestom.server.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a namespaced ID
 * https://minecraft.gamepedia.com/Namespaced_ID
 */
public class NamespaceID implements CharSequence, Keyed {
    private static final Int2ObjectOpenHashMap<NamespaceID> cache = new Int2ObjectOpenHashMap<>();
    private static final String legalLetters = "[0123456789abcdefghijklmnopqrstuvwxyz_-]+";
    private static final String legalPathLetters = "[0123456789abcdefghijklmnopqrstuvwxyz./_-]+";

    private final String domain;
    private final String path;
    private final String full;
    private final Key key;

    /**
     * Extracts the domain from the namespace ID. "minecraft:stone" would return "minecraft".
     * If no ':' character is found, "minecraft" is returned.
     *
     * @param namespaceID the namespace id to get the domain from
     * @return the domain of the namespace ID
     */
    @NotNull
    public static String getDomain(@NotNull String namespaceID) {
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
    public static String getPath(@NotNull String namespaceID) {
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

    private NamespaceID(@NotNull String path) {
        final int index = path.indexOf(':');
        if (index < 0) {
            this.domain = "minecraft";
            this.path = path;
        } else {
            this.domain = path.substring(0, index);
            this.path = path.substring(index + 1);
        }
        this.full = toString();
        validate();
        this.key = Key.key(this.domain, this.path);
    }

    private NamespaceID(String domain, String path) {
        this.domain = domain;
        this.path = path;
        this.full = toString();
        validate();
        this.key = Key.key(this.domain, this.path);
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
        if (o == null || getClass() != o.getClass()) return false;
        NamespaceID that = (NamespaceID) o;
        return Objects.equals(domain, that.domain) &&
                Objects.equals(path, that.path);
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
    public CharSequence subSequence(int start, int end) {
        return full.subSequence(start, end);
    }

    @NotNull
    @Override
    public String toString() {
        return domain + ":" + path;
    }

    @Override
    public @NotNull Key key() {
        return this.key;
    }
}
