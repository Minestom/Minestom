package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.metadata.WrittenBookMeta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMetaWrittenBookTest {
    @Test
    public void testPages() {
        ItemStack book = ItemStack.of(Material.WRITTEN_BOOK);

        Component pageA = Component.text("Page A");
        Component pageB = Component.text("Page B").append(Component.newline()).append(Component.text("Page B"));
        Component pageC = Component.text("Page C");

        book = book.withMeta(WrittenBookMeta.class, meta -> meta.pages(List.of(pageA, pageB, pageC)));

        WrittenBookMeta meta = book.meta(WrittenBookMeta.class);

        List<Component> pages = meta.getPages();

        assertEquals(3, pages.size(), "written book output meta has 3 pages");
        assertEquals(pageA, pages.get(0), "page A is the first page");
        assertEquals(pageB, pages.get(1), "page B is the second page");
        assertEquals(pageC, pages.get(2), "page C is the third page");
    }

    @Test
    public void testStyleComponents() {
        List<TextColor> colors = List.of(NamedTextColor.BLUE, NamedTextColor.WHITE, NamedTextColor.DARK_BLUE);
        List<TextDecoration> decorations = List.of(TextDecoration.UNDERLINED, TextDecoration.STRIKETHROUGH, TextDecoration.ITALIC);
        List<ClickEvent> clicks = List.of(
                ClickEvent.runCommand("/test"),
                ClickEvent.openUrl("https://minestom.net"),
                ClickEvent.changePage(2),
                ClickEvent.openFile("/test"),
                ClickEvent.copyToClipboard("clipboard text"),
                ClickEvent.suggestCommand("/test"));
        List<HoverEvent<?>> hovers = List.of(
                HoverEvent.showText(Component.text("Hover text")),
                ItemStack.of(Material.STONE).asHoverEvent(),
                HoverEvent.showText(Component.text("Hover text")),
                HoverEvent.showText(Component.text("Hover text")));

        for (TextColor color : colors) {
            for (TextDecoration decoration : decorations) {
                for (ClickEvent click : clicks) {
                    for (HoverEvent<?> hover : hovers) {

                        Component pageA = Component.text("Page A").style(style -> {
                            style.color(color);
                            style.decoration(decoration, true);
                            style.clickEvent(click);
                            style.hoverEvent(hover);
                        });
                        Component pageB = Component.text("test");

                        ItemStack book = ItemStack.of(Material.WRITTEN_BOOK)
                                .withMeta(WrittenBookMeta.class, meta -> meta.pages(pageA, pageB));

                        WrittenBookMeta meta = book.meta(WrittenBookMeta.class);
                        assertEquals(2, meta.getPages().size(), "written book output meta has 2 pages");
                        assertEquals(pageA, meta.getPages().get(0), "page A is the first page");
                        assertEquals(pageB, meta.getPages().get(1), "page B is the second page");
                    }
                }
            }
        }
    }
}
