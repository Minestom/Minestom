package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;

public record WritableBookContent(List<FilteredText<String>> pages) {
    public static final WritableBookContent EMPTY = new WritableBookContent(List.of());

    public static final NetworkBuffer.Type<WritableBookContent> NETWORK_TYPE = NetworkBufferTemplate.template(
            FilteredText.STRING_NETWORK_TYPE.list(100), WritableBookContent::pages,
            WritableBookContent::new);
    public static final Codec<WritableBookContent> CODEC = StructCodec.struct(
            "pages", FilteredText.STRING_CODEC.list().optional(List.of()), WritableBookContent::pages,
            WritableBookContent::new);

    public WritableBookContent {
        pages = List.copyOf(pages);
    }

}
