package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.metadata.WrittenBookMeta;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ItemMetaWrittenBookTest {
    @Test
    public void testPages() {
        ItemStack book = ItemStack.of(Material.WRITTEN_BOOK);

        Component pageA = Component.text("Page A");
        Component pageB = Component.text("Page B").append(Component.newline()).append(Component.text("Page B"));
        Component pageC = Component.text("Page C");
        List<Component> originalPages = List.of(pageA, pageB, pageC);

        book = book.withMeta(WrittenBookMeta.class, meta -> meta.pages(List.of(pageA, pageB, pageC)));

        WrittenBookMeta meta = book.meta(WrittenBookMeta.class);

        assertEquals(originalPages, meta.getPages(), "written book output meta must equal original input meta");
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

                        List<Component> originalPages = List.of(pageA, pageB);

                        ItemStack book = ItemStack.of(Material.WRITTEN_BOOK)
                                .withMeta(WrittenBookMeta.class, meta -> meta.pages(pageA, pageB));

                        WrittenBookMeta meta = book.meta(WrittenBookMeta.class);
                        assertEquals(originalPages, meta.getPages(), "written book output meta must equal original input meta");
                    }
                }
            }
        }
    }

    @Test
    public void buildFromVanillSNBT() {
        String vanillaSNBT = """
{
    pages:['[
        "",
        {"text":"\\\\n\\\\ntest\\\\n\\\\n"},
        {"text":"TESTSETSET","clickEvent":{"action":"run_command","value":"hi"}},
        {"text":"\\\\n\\\\n"},
        {"text":"COLORS","color":"dark_red"},
        {"text":"\\\\n\\\\n","color":"reset"},
        {"text":"EVERYTHING","bold":true,"italic":true,"strikethrough":true,"underlined":true,"obfuscated":true,
            "color":"green","clickEvent":{"action":"run_command","value":"EVERYTHING"},
            "hoverEvent":{"action":"show_text","contents":"Test"}}
    ]'],
    title:"Minestom Book",
    author:"https://minestom.net/",
    display:{Lore:["Minestom."]}
}
                """;
        SNBTParser parser = new SNBTParser(new StringReader(vanillaSNBT));

        ItemStack book = ItemStack.of(Material.WRITTEN_BOOK).withMeta(WrittenBookMeta.class, meta -> {
            List<Component> pageA = new ArrayList<>();

            pageA.add(Component.newline());
            pageA.add(Component.newline());
            pageA.add(Component.text("test"));
            pageA.add(Component.newline());
            pageA.add(Component.newline());
            pageA.add(Component.text("TESTSETSET").clickEvent(ClickEvent.runCommand("hi")));
            pageA.add(Component.newline());
            pageA.add(Component.newline());
            pageA.add(Component.text("COLORS").color(NamedTextColor.DARK_RED));
            pageA.add(Component.newline());
            pageA.add(Component.newline());
            pageA.add(Component.text("EVERYTHING").style(style -> {
                style.decoration(TextDecoration.BOLD, true);
                style.decoration(TextDecoration.ITALIC, true);
                style.decoration(TextDecoration.UNDERLINED, true);
                style.decoration(TextDecoration.STRIKETHROUGH, true);
                style.decoration(TextDecoration.OBFUSCATED, true);
                style.color(NamedTextColor.GREEN);
                style.clickEvent(ClickEvent.runCommand("EVERYTHING"));
                style.hoverEvent(HoverEvent.showText(Component.text("Test")));
            }));

            Component firstPage = pageA.stream().reduce(Component::append).orElseGet(Component::empty);

            meta.pages(firstPage.compact());
            meta.title(Component.text("Minestom Book"));
            meta.author(Component.text("https://minestom.net/"));
            meta.lore(Component.text("Minestom."));
        });

        try {
            NBTCompound nbt = (NBTCompound) parser.parse();
            ItemStack vanillaBook = ItemStack.fromNBT(Material.WRITTEN_BOOK, nbt);

            var pagesA = vanillaBook.meta(WrittenBookMeta.class).getPages();
            var pagesB = book.meta(WrittenBookMeta.class).getPages();

            // Compact the components to ensure they are the same format.
            pagesA = pagesA.stream().map(Component::compact).toList();
            pagesB = pagesB.stream().map(Component::compact).toList();

            assertEquals(pagesA, pagesB, "written book output meta must equal original input meta");
        } catch (Throwable e) {
            fail(e);
        }
    }

    // TODO: Compare to vanilla snbt. This depends on a modern serializer that defaults to legacy in some cases.
//    @Test
//    public void compareToVanilla() {
//    String vanillaSNBT = """
//{
//    pages:['[
//        "",
//        {"text":"\\\\n\\\\ntest\\\\n\\\\n"},
//        {"text":"TESTSETSET","clickEvent":{"action":"run_command","value":"hi"}},
//        {"text":"\\\\n\\\\n"},
//        {"text":"COLORS","color":"dark_red"},
//        {"text":"\\\\n\\\\n","color":"reset"},
//        {"text":"EVERYTHING","bold":true,"italic":true,"strikethrough":true,"underlined":true,"obfuscated":true,
//            "color":"green","clickEvent":{"action":"run_command","value":"EVERYTHING"},
//            "hoverEvent":{"action":"show_text","contents":"Test"}}
//    ]'],
//    title:"Minestom Book",
//    author:"https://minestom.net/",
//    display:{Lore:["Minestom."]}
//}
//                """;
//
//        ItemStack book = ItemStack.of(Material.WRITTEN_BOOK).withMeta(WrittenBookMeta.class, meta -> {
//            List<Component> pageA = new ArrayList<>();
//
//            pageA.add(Component.newline());
//            pageA.add(Component.newline());
//            pageA.add(Component.text("test"));
//            pageA.add(Component.newline());
//            pageA.add(Component.newline());
//            pageA.add(Component.text("TESTSETSET").clickEvent(ClickEvent.runCommand("hi")));
//            pageA.add(Component.newline());
//            pageA.add(Component.newline());
//            pageA.add(Component.text("COLORS").color(NamedTextColor.DARK_RED));
//            pageA.add(Component.newline());
//            pageA.add(Component.newline());
//            pageA.add(Component.text("EVERYTHING").style(style -> {
//                style.decoration(TextDecoration.BOLD, true);
//                style.decoration(TextDecoration.ITALIC, true);
//                style.decoration(TextDecoration.UNDERLINED, true);
//                style.decoration(TextDecoration.STRIKETHROUGH, true);
//                style.decoration(TextDecoration.OBFUSCATED, true);
//                style.color(NamedTextColor.GREEN);
//                style.clickEvent(ClickEvent.runCommand("EVERYTHING"));
//                style.hoverEvent(HoverEvent.showText(Component.text("Test")));
//            }));
//
//            Component firstPage = pageA.stream().reduce(Component::append).orElseGet(() -> Component.empty());
//
//            meta.pages(firstPage.compact());
//            meta.title(Component.text("Minestom Book"));
//            meta.author(Component.text("https://minestom.net/"));
//            meta.lore(Component.text("Minestom."));
//        });
//
//        TestUtils.assertEqualsSNBT(vanillaSNBT, (NBTCompound) book.toItemNBT().get("tag"));
//    }
}
