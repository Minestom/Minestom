package net.minestom.server.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagTransientTest {

    @Test
    public void twoTransientTags() {
        var tagHandler = TagHandler.newHandler();
        Tag<String> tag1 = Tag.Transient("a");
        Tag<String> tag2 = Tag.Transient("b");

        tagHandler.setTag(tag1, "abcdef");
        var result = tagHandler.getTag(tag2);
        assertNull(result);
    }

    @Test
    public void twoTransientTagsEqual() {
        var tagHandler = TagHandler.newHandler();
        Tag<String> tag1 = Tag.Transient("a");
        Tag<String> tag2 = Tag.Transient("a");

        tagHandler.setTag(tag1, "abcdef");
        var result = tagHandler.getTag(tag2);
        assertEquals("abcdef", result);
    }

}
