package net.minestom.server.item.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record WrittenBookContent(FilteredText<String> title, String author, int generation,
                                 List<FilteredText<Component>> pages, boolean resolved) {
    public static final WrittenBookContent EMPTY = new WrittenBookContent(new FilteredText<>("", null), "", 0, List.of(), true);

    public static final NetworkBuffer.Type<WrittenBookContent> NETWORK_TYPE = NetworkBufferTemplate.template(
            FilteredText.STRING_NETWORK_TYPE, WrittenBookContent::title,
            STRING, WrittenBookContent::author,
            VAR_INT, WrittenBookContent::generation,
            FilteredText.COMPONENT_NETWORK_TYPE.list(100), WrittenBookContent::pages,
            BOOLEAN, WrittenBookContent::resolved,
            WrittenBookContent::new);
    public static final Codec<WrittenBookContent> CODEC = StructCodec.struct(
            "title", FilteredText.STRING_CODEC, WrittenBookContent::title,
            "author", Codec.STRING, WrittenBookContent::author,
            "generation", Codec.INT.optional(0), WrittenBookContent::generation,
            "pages", FilteredText.COMPONENT_CODEC.list(100).optional(List.of()), WrittenBookContent::pages,
            "resolved", Codec.BOOLEAN.optional(false), WrittenBookContent::resolved,
            WrittenBookContent::new);

    public WrittenBookContent {
        pages = List.copyOf(pages);
    }

    public WrittenBookContent(String title, String author, List<Component> pages) {
        this(title, author, 0, pages, true);
    }

    public WrittenBookContent(String title, String author, int generation, List<Component> pages, boolean resolved) {
        this(new FilteredText<>(title, null), author, generation, pages.stream().map(page -> new FilteredText<>(page, null)).toList(), resolved);
    }
}
