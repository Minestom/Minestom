package net.kyori.adventure.nbt;

import net.minestom.server.adventure.MinestomAdventure;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

// Based on net.kyori.adventure.nbt.TagStringIO licensed under the MIT license.
// https://github.com/KyoriPowered/adventure/blob/main/4/nbt/src/main/java/net/kyori/adventure/nbt/TagStringIO.java

/**
 * Deprecated in Adventure 4.22.0 in favor of {@link TagStringIO}, as related functionality has been upstreamed.
 * <p>
 * Use {@link MinestomAdventure#tagStringIO()} to access the configured {@linkplain TagStringIO} instance.
 * </p>
 * <p>Replacements:</p>
 * <ul>
 *   <li>{@link #writeTag(BinaryTag)} -> {@link TagStringIO#asString(BinaryTag)}</li>
 *   <li>{@link #writeTag(BinaryTag, String)} -> {@link TagStringIO#asString(BinaryTag)}</li>
 *   <li>{@link #readTag(String)} -> {@link TagStringIO#asTag(String)}</li>
 *   <li>{@link #readTagEmbedded(String)} -> {@link TagStringIO#asTag(String, Appendable)}</li>
 * </ul>
 */
@Deprecated(forRemoval = true)
public final class TagStringIOExt {

    /**
     * @deprecated Use {@link TagStringIO#asString(BinaryTag)}
     */
    @Deprecated(forRemoval = true)
    public static @NotNull String writeTag(@NotNull BinaryTag tag) {
        return writeTag(tag, "");
    }

    /**
     * @deprecated Use {@link TagStringIO#asString(BinaryTag)}
     */
    @Deprecated(forRemoval = true)
    public static @NotNull String writeTag(@NotNull BinaryTag input, @NotNull String indent) {
        final StringBuilder sb = new StringBuilder();
        try (final TagStringWriter emit = new TagStringWriter(sb, indent).heterogeneousLists(true)) {
            emit.writeTag(input);
        } catch (IOException e) {
            // The IOException comes from Writer#close(), but we are passing a StringBuilder which
            // is not a writer and does not need to be closed so will not throw.
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    /**
     * @deprecated Use {@link TagStringIO#asTag(String)}
     */
    @Deprecated(forRemoval = true)
    public static @NotNull BinaryTag readTag(@NotNull String input) throws IOException {
        try {
            final CharBuffer buffer = new CharBuffer(input);
            final TagStringReader parser = new TagStringReader(buffer);
            parser.heterogeneousLists(true);
            final BinaryTag tag = parser.tag();
            if (buffer.skipWhitespace().hasMore()) {
                throw new IOException("Document had trailing content after first tag");
            }
            return tag;
        } catch (final StringTagParseException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * @deprecated Use {@link TagStringIO#asTag(String, Appendable)}
     * Reads a tag and returns the remainder of the input buffer.
     */
    @Deprecated(forRemoval = true)
    public static Map.Entry<@NotNull BinaryTag, @NotNull String> readTagEmbedded(@NotNull String input) throws IOException {
        try {
            final CharBuffer buffer = new CharBuffer(input);
            final TagStringReader parser = new TagStringReader(buffer);
            parser.heterogeneousLists(true);
            final BinaryTag tag = parser.tag();

            // Collect remaining (todo figure out a better way, probably need to just write an snbt parser)
            final StringBuilder remainder = new StringBuilder();
            while (buffer.hasMore()) {
                remainder.append(buffer.take());
            }

            return Map.entry(tag, remainder.toString());
        } catch (final StringTagParseException ex) {
            throw new IOException(ex);
        }
    }

    private TagStringIOExt() {
    }
}
