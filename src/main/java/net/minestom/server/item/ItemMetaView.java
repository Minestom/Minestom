package net.minestom.server.item;

import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;

@Deprecated
public interface ItemMetaView<T extends ItemMetaView.Builder> extends TagReadable {

    @Deprecated
    non-sealed interface Builder extends ItemMeta.Builder {
        default @NotNull ItemMeta build() {
            return new ItemMetaImpl(components().build());
        }
    }
}
