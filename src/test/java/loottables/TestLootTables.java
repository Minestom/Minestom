package loottables;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataImpl;
import net.minestom.server.gamedata.conditions.SurvivesExplosionCondition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.gamedata.loottables.entries.ItemEntry;
import net.minestom.server.gamedata.loottables.entries.ItemType;
import net.minestom.server.gamedata.loottables.tabletypes.BlockType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class TestLootTables {

    private LootTableManager tableManager;

    @BeforeEach
    public void init() {
        // Init material correctly.
        try {
            Class.forName(Material.class.getName(), true, MinecraftServer.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

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
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/acacia_button"), (a) -> new Gson().fromJson(new StringReader(lootTableJson), JsonObject.class));
        Assertions.assertTrue(lootTable.getType() instanceof BlockType);
        Assertions.assertEquals(1, lootTable.getPools().size());
        Assertions.assertEquals(1, lootTable.getPools().get(0).getMinRollCount());
        Assertions.assertEquals(1, lootTable.getPools().get(0).getMaxRollCount());
        Assertions.assertEquals(1, lootTable.getPools().get(0).getEntries().size());
        Assertions.assertTrue(lootTable.getPools().get(0).getEntries().get(0).getType() instanceof ItemType);
        Assertions.assertTrue(lootTable.getPools().get(0).getEntries().get(0) instanceof ItemEntry);
        ItemEntry entry = (ItemEntry) lootTable.getPools().get(0).getEntries().get(0);
        Assertions.assertEquals(Material.ACACIA_BUTTON, entry.getItem());
        Assertions.assertEquals(0, entry.getFunctions().size());
        Assertions.assertEquals(1, lootTable.getPools().get(0).getConditions().size());
        Assertions.assertTrue(lootTable.getPools().get(0).getConditions().get(0) instanceof SurvivesExplosionCondition);
    }

    @Test
    public void loadFromFile() throws IOException {
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/acacia_button"), (a) -> null);
        Assertions.assertTrue(lootTable.getType() instanceof BlockType);
        Assertions.assertEquals(1, lootTable.getPools().size());
        Assertions.assertEquals(1, lootTable.getPools().get(0).getMinRollCount());
        Assertions.assertEquals(1, lootTable.getPools().get(0).getMaxRollCount());
        Assertions.assertEquals(1, lootTable.getPools().get(0).getEntries().size());
        Assertions.assertTrue(lootTable.getPools().get(0).getEntries().get(0).getType() instanceof ItemType);
        Assertions.assertTrue(lootTable.getPools().get(0).getEntries().get(0) instanceof ItemEntry);
        ItemEntry entry = (ItemEntry) lootTable.getPools().get(0).getEntries().get(0);
        Assertions.assertEquals(Material.ACACIA_BUTTON, entry.getItem());
        Assertions.assertEquals(0, entry.getFunctions().size());
        Assertions.assertEquals(1, lootTable.getPools().get(0).getConditions().size());
        Assertions.assertTrue(lootTable.getPools().get(0).getConditions().get(0) instanceof SurvivesExplosionCondition);
    }

    @Test
    public void caching() throws IOException {
        LootTable lootTable1 = tableManager.load(NamespaceID.from("blocks/acacia_button"), (a) -> null);
        LootTable lootTable2 = tableManager.load(NamespaceID.from("blocks/acacia_button"), (a) -> null);
        Assertions.assertSame(lootTable1, lootTable2);
    }

    @Test
    public void simpleGenerate() throws IOException {
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/acacia_button"), (a) -> null);
        Data arguments = new DataImpl();
        List<ItemStack> stacks = lootTable.generate(arguments);
        Assertions.assertEquals(1, stacks.size());
        Assertions.assertEquals(Material.ACACIA_BUTTON, stacks.get(0).getMaterial());
    }

    @Test
    public void testExplosion() throws IOException {
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/acacia_button"), (a) -> null);
        Data arguments = new DataImpl();
        // negative value will force the condition to fail
        arguments.set("explosionPower", -1.0, Double.class);
        List<ItemStack> stacks = lootTable.generate(arguments);
        Assertions.assertEquals(0, stacks.size());
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
        LootTable lootTable = tableManager.load(NamespaceID.from("blocks/none"), (a) -> new Gson().fromJson(new StringReader(lootTableJson), JsonObject.class));
        List<ItemStack> stacks = lootTable.generate(Data.EMPTY);
        Assertions.assertEquals(0, stacks.size());
    }
}
