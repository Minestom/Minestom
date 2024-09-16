package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record WritableBookContent(@NotNull List<FilteredText<String>> pages) {
    public static final WritableBookContent EMPTY = new WritableBookContent(List.of());

    public static final NetworkBuffer.Type<WritableBookContent> NETWORK_TYPE = NetworkBufferTemplate.template(
            FilteredText.STRING_NETWORK_TYPE.list(100), WritableBookContent::pages,
            WritableBookContent::new
    );

    public static final BinaryTagSerializer<WritableBookContent> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull WritableBookContent value) {
            ListBinaryTag.Builder<BinaryTag> pages = ListBinaryTag.builder();
            for (FilteredText<String> page : value.pages) {
                pages.add(FilteredText.STRING_NBT_TYPE.write(page));
            }
            return CompoundBinaryTag.builder().put("pages", pages.build()).build();
        }

        @Override
        public @NotNull WritableBookContent read(@NotNull BinaryTag tag) {
            if (!(tag instanceof CompoundBinaryTag compound)) return EMPTY;
            ListBinaryTag pagesTag = compound.getList("pages");
            if (pagesTag.size() == 0) return EMPTY;

            List<FilteredText<String>> pages = new ArrayList<>(pagesTag.size());
            for (BinaryTag pageTag : pagesTag) {
                pages.add(FilteredText.STRING_NBT_TYPE.read(pageTag));
            }
            return new WritableBookContent(pages);
        }
    };

    public WritableBookContent {
        pages = List.copyOf(pages);
    }

}
