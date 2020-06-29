package loottables;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.conditions.SurvivesExplosionCondition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.gamedata.loottables.entries.ItemEntry;
import net.minestom.server.gamedata.loottables.entries.ItemType;
import net.minestom.server.gamedata.loottables.tabletypes.BlockType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.List;

public class TestLootTables {

    private LootTableManager tableManager;

    @Before
    public void init() {
        tableManager = new LootTableManager();
        tableManager.registerConditionDeserializer(NamespaceID.from("minecraft:survives_explosion"), new SurvivesExplosionCondition.Deserializer());
        tableManager.registerTableType(NamespaceID.from("minecraft:block"), new BlockType());
        tableManager.registerEntryType(NamespaceID.from("minecraft:item"), new ItemType());
    }

    @Test
    public void loadFromString() {
        // from acacia_button.json
        final String lootTableJson = "{\n" +
                "  \"type\": \"minecraft:block\",\n" +
                "  \"pools\": [\n" +
                "    {\n" +
                "      \"rolls\": 1,\n" +
                "      \"entries\": [\n" +
                "        {\n" +
                "          \"type\": \"minecraft:item\",\n" +
                "          \"name\": \"minecraft:acacia_button\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"conditions\": [\n" +
                "        {\n" +
                "          \"condition\": \"minecraft:survives_explosion\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/acacia_button"), new StringReader(lootTableJson));
        Assert.assertTrue(lootTable.getType() instanceof BlockType);
        Assert.assertEquals(1, lootTable.getPools().size());
        Assert.assertEquals(1, lootTable.getPools().get(0).getMinRollCount());
        Assert.assertEquals(1, lootTable.getPools().get(0).getMaxRollCount());
        Assert.assertEquals(1, lootTable.getPools().get(0).getEntries().size());
        Assert.assertTrue(lootTable.getPools().get(0).getEntries().get(0).getType() instanceof ItemType);
        Assert.assertTrue(lootTable.getPools().get(0).getEntries().get(0) instanceof ItemEntry);
        ItemEntry entry = (ItemEntry) lootTable.getPools().get(0).getEntries().get(0);
        Assert.assertEquals(Material.ACACIA_BUTTON, entry.getItem());
        Assert.assertEquals(0, entry.getFunctions().size());
        Assert.assertEquals(1, lootTable.getPools().get(0).getConditions().size());
        Assert.assertTrue(lootTable.getPools().get(0).getConditions().get(0) instanceof SurvivesExplosionCondition);
    }

    @Test
    public void loadFromFile() throws FileNotFoundException {
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/acacia_button"));
        Assert.assertTrue(lootTable.getType() instanceof BlockType);
        Assert.assertEquals(1, lootTable.getPools().size());
        Assert.assertEquals(1, lootTable.getPools().get(0).getMinRollCount());
        Assert.assertEquals(1, lootTable.getPools().get(0).getMaxRollCount());
        Assert.assertEquals(1, lootTable.getPools().get(0).getEntries().size());
        Assert.assertTrue(lootTable.getPools().get(0).getEntries().get(0).getType() instanceof ItemType);
        Assert.assertTrue(lootTable.getPools().get(0).getEntries().get(0) instanceof ItemEntry);
        ItemEntry entry = (ItemEntry) lootTable.getPools().get(0).getEntries().get(0);
        Assert.assertEquals(Material.ACACIA_BUTTON, entry.getItem());
        Assert.assertEquals(0, entry.getFunctions().size());
        Assert.assertEquals(1, lootTable.getPools().get(0).getConditions().size());
        Assert.assertTrue(lootTable.getPools().get(0).getConditions().get(0) instanceof SurvivesExplosionCondition);
    }

    @Test
    public void caching() throws FileNotFoundException {
        LootTable lootTable1 = tableManager.load(NamespaceID.from("blocks/acacia_button"));
        LootTable lootTable2 = tableManager.load(NamespaceID.from("blocks/acacia_button"));
        Assert.assertSame(lootTable1, lootTable2);
    }

    @Test
    public void simpleGenerate() throws FileNotFoundException {
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/acacia_button"));
        Data arguments = new Data();
        List<ItemStack> stacks = lootTable.generate(arguments);
        Assert.assertEquals(1, stacks.size());
        Assert.assertEquals(Material.ACACIA_BUTTON, stacks.get(0).getMaterial());
    }

    @Test
    public void testExplosion() throws FileNotFoundException {
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/acacia_button"));
        Data arguments = new Data();
        // negative value will force the condition to fail
        arguments.set("explosionPower", -1.0, Double.class);
        List<ItemStack> stacks = lootTable.generate(arguments);
        Assert.assertEquals(0, stacks.size());
    }

    @Test
    public void unknownCondition() {
        // from acacia_button.json
        final String lootTableJson = "{\n" +
                "  \"type\": \"minecraft:block\",\n" +
                "  \"pools\": [\n" +
                "    {\n" +
                "      \"rolls\": 1,\n" +
                "      \"entries\": [\n" +
                "        {\n" +
                "          \"type\": \"minecraft:item\",\n" +
                "          \"name\": \"minecraft:acacia_button\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"conditions\": [\n" +
                "        {\n" +
                "          \"condition\": \"minestom:unknown\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/none"), new StringReader(lootTableJson));
        List<ItemStack> stacks = lootTable.generate(Data.EMPTY);
        Assert.assertEquals(0, stacks.size());
    }
}
