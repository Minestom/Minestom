package net.minestom.server.item;

import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.Taggable;

public interface ItemMetaView extends TagReadable {
    interface Builder extends Taggable {
    }
}
