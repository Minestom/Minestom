package net.minestom.server.item;

import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.Taggable;

@SuppressWarnings("ALL")
public interface ItemMetaView<T extends ItemMetaView.Builder> extends TagReadable {
    interface Builder extends Taggable {
    }
}
