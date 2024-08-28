package net.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

// Based on net.kyori.adventure.nbt.TagStringIO licensed under the MIT license.
// https://github.com/KyoriPowered/adventure/blob/main/4/nbt/src/main/java/net/kyori/adventure/nbt/TagStringIO.java
public final class TagStringIOExt {

    public static @NotNull String writeTag(@NotNull BinaryTag tag) {
        return writeTag(tag, "");
    }

    public static @NotNull String writeTag(@NotNull BinaryTag input, @NotNull String indent) {
        final StringBuilder sb = new StringBuilder();
        try (final TagStringWriter emit = new TagStringWriter(sb, indent)) {
            emit.writeTag(input);
        } catch (IOException e) {
            // The IOException comes from Writer#close(), but we are passing a StringBuilder which
            // is not a writer and does not need to be closed so will not throw.
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static @NotNull BinaryTag readTag(@NotNull String input) throws IOException {
        try {
            final CharBuffer buffer = new CharBuffer(input);
            final TagStringReader parser = new TagStringReader(buffer);
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
     * Reads a tag and returns the remainder of the input buffer.
     */
    public static Map.Entry<@NotNull BinaryTag, @NotNull String> readTagEmbedded(@NotNull String input) throws IOException {
        try {
            final CharBuffer buffer = new CharBuffer(input);
            final TagStringReader parser = new TagStringReader(buffer);
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

    private TagStringIOExt() {}
}
