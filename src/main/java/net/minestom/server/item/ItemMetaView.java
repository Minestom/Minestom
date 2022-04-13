package net.minestom.server.item;

import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("ALL")
@ApiStatus.Experimental
public interface ItemMetaView<T extends ItemMetaView.Builder> extends TagReadable {
    @ApiStatus.Experimental
    interface Builder extends Taggable {
    }
}
