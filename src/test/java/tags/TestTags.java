package tags;

import com.google.gson.Gson;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.gamedata.tags.TagContainer;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.utils.NamespaceID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class TestTags {

    private TagManager tags;

    @BeforeEach
    public void init() {
        tags = new TagManager();
    }

    @Test
    public void testSubTag() {
        Gson gson = new Gson();
        String tag1 = "{\n" +
                "\t\"replace\": false,\n" +
                "\t\"values\": [\n" +
                "\t\t\"minestom:an_item\"\n" +
                "\t]\n" +
                "}";

        String tag2 = "{\n" +
                "\t\"replace\": false,\n" +
                "\t\"values\": [\n" +
                "\t\t\"#minestom:test_sub\",\n" +
                "\t\t\"minestom:some_other_item\"\n" +
                "\t]\n" +
                "}";
        TagContainer tc1 = gson.fromJson(new StringReader(tag1), TagContainer.class);
        TagContainer tc2 = gson.fromJson(new StringReader(tag2), TagContainer.class);
        Assertions.assertNotEquals(Tag.EMPTY, tags.load(NamespaceID.from("minestom:test_sub"), "any", () -> tc1));
        Tag loaded = tags.load(NamespaceID.from("minestom:test"), "any", () -> tc2);
        NamespaceID[] values = loaded.getValues().toArray(new NamespaceID[0]);
        Assertions.assertEquals(2, values.length);
        Assertions.assertTrue(loaded.contains(NamespaceID.from("minestom:an_item")));
        Assertions.assertTrue(loaded.contains(NamespaceID.from("minestom:some_other_item")));
        Assertions.assertFalse(loaded.contains(NamespaceID.from("minestom:some_other_item_that_is_not_in_the_tag")));
    }

    /**
     * A value of 'true' in 'replace' should replace previous contents
     */
    @Test
    public void testReplacement() {
        Gson gson = new Gson();
        String tag1 = "{\n" +
                "\t\"replace\": false,\n" +
                "\t\"values\": [\n" +
                "\t\t\"minestom:an_item\"\n" +
                "\t]\n" +
                "}";

        String tag2 = "{\n" +
                "\t\"replace\": true,\n" +
                "\t\"values\": [\n" +
                "\t\t\"minestom:some_other_item\"\n" +
                "\t]\n" +
                "}";
        TagContainer tc1 = gson.fromJson(new StringReader(tag1), TagContainer.class);
        TagContainer tc2 = gson.fromJson(new StringReader(tag2), TagContainer.class);
        Assertions.assertNotEquals(Tag.EMPTY, tags.load(NamespaceID.from("minestom:test"), "any", () -> tc1));
        Tag loaded = tags.forceLoad(NamespaceID.from("minestom:test"), "any", () -> tc2);
        Assertions.assertNotEquals(Tag.EMPTY, loaded);
        Assertions.assertEquals(1, loaded.getValues().size());
        Assertions.assertTrue(loaded.contains(NamespaceID.from("minestom:some_other_item")));
        Assertions.assertFalse(loaded.contains(NamespaceID.from("minestom:an_item")));
    }

    /**
     * A value of 'false' in 'replace' should append to previous contents
     */
    @Test
    public void testAppend() {
        Gson gson = new Gson();
        String tag1 = "{\n" +
                "\t\"replace\": false,\n" +
                "\t\"values\": [\n" +
                "\t\t\"minestom:an_item\"\n" +
                "\t]\n" +
                "}";

        String tag2 = "{\n" +
                "\t\"replace\": false,\n" +
                "\t\"values\": [\n" +
                "\t\t\"minestom:some_other_item\"\n" +
                "\t]\n" +
                "}";
        TagContainer tc1 = gson.fromJson(new StringReader(tag1), TagContainer.class);
        TagContainer tc2 = gson.fromJson(new StringReader(tag2), TagContainer.class);
        Assertions.assertNotEquals(Tag.EMPTY, tags.load(NamespaceID.from("minestom:test"), "any", () -> tc1));
        Tag loaded = tags.forceLoad(NamespaceID.from("minestom:test"), "any", () -> tc2);
        Assertions.assertNotEquals(Tag.EMPTY, loaded);
        Assertions.assertEquals(2, loaded.getValues().size());
        Assertions.assertTrue(loaded.contains(NamespaceID.from("minestom:some_other_item")));
        Assertions.assertTrue(loaded.contains(NamespaceID.from("minestom:an_item")));
    }

    @AfterEach
    public void cleanup() {
        tags = null;
    }
}
