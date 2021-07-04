package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an element which can read and write {@link Tag tags}.
 */
@ApiStatus.Experimental
public interface TagHandler extends TagReadable, TagWritable {
}
