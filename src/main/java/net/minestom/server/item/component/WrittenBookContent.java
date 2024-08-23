package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record WrittenBookContent(@NotNull FilteredText<String> title, @NotNull String author, int generation,
                                 @NotNull List<FilteredText<Component>> pages, boolean resolved) {
    public static final WrittenBookContent EMPTY = new WrittenBookContent(new FilteredText<>("", null), "", 0, List.of(), true);

    public static final NetworkBuffer.Type<WrittenBookContent> NETWORK_TYPE = NetworkBufferTemplate.template(
            FilteredText.STRING_NETWORK_TYPE, WrittenBookContent::title,
            STRING, WrittenBookContent::author,
            VAR_INT, WrittenBookContent::generation,
            FilteredText.COMPONENT_NETWORK_TYPE.list(100), WrittenBookContent::pages,
            BOOLEAN, WrittenBookContent::resolved,
            WrittenBookContent::new
    );

    public static final @NotNull BinaryTagSerializer<WrittenBookContent> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            compound -> {
                ListBinaryTag pagesTag = compound.getList("pages");
                List<FilteredText<Component>> pages = pagesTag.stream()
                        .map(FilteredText.COMPONENT_NBT_TYPE::read)
                        .toList();
                FilteredText<String> title = FilteredText.STRING_NBT_TYPE.read(compound.get("title"));
                String author = compound.getString("author");
                int generation = compound.getInt("generation");
                boolean resolved = compound.getBoolean("resolved");
                return new WrittenBookContent(title, author, generation, pages, resolved);
            },
            value -> {
                ListBinaryTag.Builder<BinaryTag> pagesTag = ListBinaryTag.builder();
                for (FilteredText<Component> page : value.pages) {
                    pagesTag.add(FilteredText.COMPONENT_NBT_TYPE.write(page));
                }
                return CompoundBinaryTag.builder()
                        .put("pages", pagesTag.build())
                        .put("title", FilteredText.STRING_NBT_TYPE.write(value.title))
                        .putString("author", value.author)
                        .putInt("generation", value.generation)
                        .putBoolean("resolved", value.resolved)
                        .build();
            }
    );

    public WrittenBookContent {
        pages = List.copyOf(pages);
    }

    public WrittenBookContent(@NotNull String title, @NotNull String author, @NotNull List<Component> pages) {
        this(title, author, 0, pages, true);
    }

    public WrittenBookContent(@NotNull String title, @NotNull String author, int generation, @NotNull List<Component> pages, boolean resolved) {
        this(new FilteredText<>(title, null), author, generation, pages.stream().map(page -> new FilteredText<>(page, null)).toList(), resolved);
    }
}
