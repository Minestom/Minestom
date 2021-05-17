package net.minestom.server.tag;

import com.google.common.annotations.Beta;

/**
 * Represents an element which can read and write {@link Tag tags}.
 */
@Beta
public interface TagHandler extends TagReadable, TagWritable {
}
