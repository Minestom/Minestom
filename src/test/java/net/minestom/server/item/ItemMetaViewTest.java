package net.minestom.server.item;

import net.minestom.server.item.metadata.BundleMeta;
import net.minestom.server.tag.TagHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ItemMetaViewTest {
    @Test
    void viewType() {
        assertEquals(BundleMeta.Builder.class, ItemMetaViewImpl.viewType(BundleMeta.class));
    }

    @Test
    void construct() {
        assertInstanceOf(BundleMeta.Builder.class, ItemMetaViewImpl.constructBuilder(BundleMeta.class, TagHandler.newHandler()));
    }
}
