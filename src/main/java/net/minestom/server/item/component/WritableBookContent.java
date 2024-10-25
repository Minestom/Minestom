package net.minestom.server.item.component;

import net.minestom.server.item.book.FilteredText;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WritableBookContent(@NotNull List<FilteredText<String>> pages) {
    public static final WritableBookContent EMPTY = new WritableBookContent(List.of());

    public static final NetworkBuffer.Type<WritableBookContent> NETWORK_TYPE = NetworkBufferTemplate.template(
            FilteredText.STRING_NETWORK_TYPE.list(100), WritableBookContent::pages,
            WritableBookContent::new);
    public static final BinaryTagSerializer<WritableBookContent> NBT_TYPE = BinaryTagTemplate.object(
            "pages", FilteredText.STRING_NBT_TYPE.list().optional(List.of()), WritableBookContent::pages,
            WritableBookContent::new);

    public WritableBookContent {
        pages = List.copyOf(pages);
    }

}
