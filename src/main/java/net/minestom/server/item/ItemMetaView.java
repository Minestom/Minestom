package net.minestom.server.item;

import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
@ApiStatus.Experimental
public interface ItemMetaView<T extends ItemMetaView.Builder> extends TagReadable {
    @ApiStatus.Experimental
    non-sealed interface Builder extends ItemMeta.Builder {
        default @NotNull ItemMeta build() {
            return new ItemMetaImpl(tagHandler().copy());
        }
    }
}
