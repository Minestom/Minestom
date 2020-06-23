package tags;

import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.utils.NamespaceID;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.StringReader;

public class TestTags {

    private TagManager tags;

    @Before
    public void init() {
        tags = new TagManager();
    }

    @Test
    public void testSubTag() throws FileNotFoundException {
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
        Assert.assertNotEquals(Tag.EMPTY, tags.load(NamespaceID.from("minestom:test_sub"), "any", new StringReader(tag1)));
        Tag loaded = tags.load(NamespaceID.from("minestom:test"), "any", new StringReader(tag2));
        NamespaceID[] values = loaded.getValues().toArray(new NamespaceID[0]);
        Assert.assertEquals(2, values.length);
        Assert.assertTrue(loaded.contains(NamespaceID.from("minestom:an_item")));
        Assert.assertTrue(loaded.contains(NamespaceID.from("minestom:some_other_item")));
        Assert.assertFalse(loaded.contains(NamespaceID.from("minestom:some_other_item_that_is_not_in_the_tag")));
    }

    /**
     * A value of 'true' in 'replace' should replace previous contents
     */
    @Test
    public void testReplacement() throws FileNotFoundException {
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
        Assert.assertNotEquals(Tag.EMPTY, tags.load(NamespaceID.from("minestom:test"), "any", new StringReader(tag1)));
        Tag loaded = tags.forceLoad(NamespaceID.from("minestom:test"), "any", () -> new StringReader(tag2));
        Assert.assertNotEquals(Tag.EMPTY, loaded);
        Assert.assertEquals(1, loaded.getValues().size());
        Assert.assertTrue(loaded.contains(NamespaceID.from("minestom:some_other_item")));
        Assert.assertFalse(loaded.contains(NamespaceID.from("minestom:an_item")));
    }

    /**
     * A value of 'false' in 'replace' should append to previous contents
     */
    @Test
    public void testAppend() throws FileNotFoundException {
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
        Assert.assertNotEquals(Tag.EMPTY, tags.load(NamespaceID.from("minestom:test"), "any", new StringReader(tag1)));
        Tag loaded = tags.forceLoad(NamespaceID.from("minestom:test"), "any", () -> new StringReader(tag2));
        Assert.assertNotEquals(Tag.EMPTY, loaded);
        Assert.assertEquals(2, loaded.getValues().size());
        Assert.assertTrue(loaded.contains(NamespaceID.from("minestom:some_other_item")));
        Assert.assertTrue(loaded.contains(NamespaceID.from("minestom:an_item")));
    }

    @After
    public void cleanup() {
        tags = null;
    }
}
