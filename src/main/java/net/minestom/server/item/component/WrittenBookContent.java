package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WrittenBookContent(@NotNull List<FilteredText<Component>> pages, @NotNull FilteredText<String> title, @NotNull String author, int generation, boolean resolved) {
    public static final WrittenBookContent EMPTY = new WrittenBookContent(List.of(), new FilteredText<>("", null), "", 0, true);

    public static final @NotNull NetworkBuffer.Type<WrittenBookContent> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, WrittenBookContent value) {
            buffer.write(FilteredText.STRING_NETWORK_TYPE, value.title);
            buffer.write(NetworkBuffer.STRING, value.author);
            buffer.write(NetworkBuffer.VAR_INT, value.generation);
            buffer.writeCollection(FilteredText.COMPONENT_NETWORK_TYPE, value.pages);
            buffer.write(NetworkBuffer.BOOLEAN, value.resolved);
        }

        @Override
        public WrittenBookContent read(@NotNull NetworkBuffer buffer) {
            FilteredText<String> title = buffer.read(FilteredText.STRING_NETWORK_TYPE);
            String author = buffer.read(NetworkBuffer.STRING);
            int generation = buffer.read(NetworkBuffer.VAR_INT);
            List<FilteredText<Component>> pages = buffer.readCollection(FilteredText.COMPONENT_NETWORK_TYPE, 100);
            boolean resolved = buffer.read(NetworkBuffer.BOOLEAN);
            return new WrittenBookContent(pages, title, author, generation, resolved);
        }
    };

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
                return new WrittenBookContent(pages, title, author, generation, resolved);
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

    public WrittenBookContent(@NotNull List<Component> pages, @NotNull String title, @NotNull String author) {
        this(pages, title, author, 0, true);
    }

    public WrittenBookContent(@NotNull List<Component> pages, @NotNull String title, @NotNull String author, int generation, boolean resolved) {
        this(pages.stream().map(page -> new FilteredText<>(page, null)).toList(), new FilteredText<>(title, null), author, generation, resolved);
    }
}
