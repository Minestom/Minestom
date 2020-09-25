package net.minestom.codegen.items;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minestom.codegen.EnumGenerator;
import net.minestom.codegen.MinestomEnumGenerator;
import net.minestom.codegen.PrismarinePaths;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Generates a Material enum containing all data about items
 *
 * Assumes that Block is available
 */
public class ItemEnumGenerator extends MinestomEnumGenerator<ItemContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemEnumGenerator.class);

    private final String targetVersion;
    private final File targetFolder;

    public static void main(String[] args) throws IOException {
        String targetVersion;
        if(args.length < 1) {
            System.err.println("Usage: <MC version> [target folder]");
            return;
        }

        targetVersion = args[0];

        try {
            ResourceGatherer.ensureResourcesArePresent(targetVersion); // TODO
        } catch (IOException e) {
            e.printStackTrace();
        }

        String targetPart = DEFAULT_TARGET_PATH;
        if(args.length >= 2) {
            targetPart = args[1];
        }

        File targetFolder = new File(targetPart);
        if(!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        new ItemEnumGenerator(targetVersion, targetFolder);
    }

    private ItemEnumGenerator(String targetVersion, File targetFolder) throws IOException {
        this.targetVersion = targetVersion;
        this.targetFolder = targetFolder;
        generateTo(targetFolder);
    }

    /**
     * Extract item information from Burger (submodule of Minestom)
     * @param gson
     * @param url
     * @return
     * @throws IOException
     */
    private List<BurgerItem> parseItemsFromBurger(Gson gson, String url) throws IOException {
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            LOGGER.debug("\tConnection established, reading file");
            JsonObject dictionary = gson.fromJson(bufferedReader, JsonArray.class).get(0).getAsJsonObject();
            JsonObject itemMap = dictionary.getAsJsonObject("items").getAsJsonObject("item");
            List<BurgerItem> items = new LinkedList<>();
            for(var entry : itemMap.entrySet()) {
                BurgerItem item = gson.fromJson(entry.getValue(), BurgerItem.class);
                items.add(item);
            }
            return items;
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public String getPackageName() {
        return "net.minestom.server.item";
    }

    @Override
    public String getClassName() {
        return "Material";
    }

    @Override
    protected Collection<ItemContainer> compile() throws IOException {
        Gson gson = new Gson();
        LOGGER.debug("Finding path for PrismarineJS items");
        JsonObject dataPaths = gson.fromJson(new BufferedReader(new FileReader(PRISMARINE_JS_DATA_PATHS)), JsonObject.class);
        JsonObject pathsJson = dataPaths.getAsJsonObject("pc").getAsJsonObject(targetVersion);

        PrismarinePaths paths = gson.fromJson(pathsJson, PrismarinePaths.class);
        LOGGER.debug("Loading PrismarineJS blocks data");
        List<BurgerItem> burgerItems = parseItemsFromBurger(gson, BURGER_URL_BASE_URL+targetVersion+".json");

        TreeSet<ItemContainer> items = new TreeSet<>(ItemContainer::compareTo);
        for(var burgerItem : burgerItems) {
            items.add(new ItemContainer(burgerItem.numeric_id, NamespaceID.from("minecraft:"+burgerItem.text_id), burgerItem.max_stack_size, getBlock(burgerItem.text_id.toUpperCase())));
        }
        return items;
    }

    /**
     * Returns a block with the given name. Returns null if none
     * @param itemName
     * @return
     */
    private Block getBlock(String itemName) {
        // special cases
        if(itemName.equals("REDSTONE"))
            return Block.REDSTONE_WIRE;
        // end of special cases

        try {
            return Block.valueOf(itemName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected void prepare(EnumGenerator generator) {
        String className = getClassName();
        generator.addImport(Block.class.getCanonicalName());
        generator.addImport(Registries.class.getCanonicalName());
        generator.addImport(NamespaceID.class.getCanonicalName());
        generator.addClassAnnotation("@SuppressWarnings({\"deprecation\"})");
        generator.setParams("String namespaceID", "int maxDefaultStackSize", "Block correspondingBlock");
        generator.appendToConstructor("Registries.materials.put(NamespaceID.from(namespaceID), this);");

        generator.addMethod("getId", "()", "short", "return (short)ordinal();");
        generator.addMethod("getName", "()", "String", "return namespaceID;");
        generator.addMethod("getMaxDefaultStackSize", "()", "int", "return maxDefaultStackSize;");
        generator.addMethod("isBlock", "()", "boolean", "return correspondingBlock != null && this != AIR;");
        generator.addMethod("getBlock", "()", "Block", "return correspondingBlock;");

        generator.addMethod("fromId", "(short id)", "static "+className,
                "if(id >= 0 && id < values().length) {",
                "\treturn values()[id];",
                "}",
                "return AIR;"
        );

        // hard coded methods
        generator.addMethod("isHelmet", "()", "boolean", "return toString().endsWith(\"HELMET\");");
        generator.addMethod("isChestplate", "()", "boolean", "return toString().endsWith(\"CHESTPLATE\");");
        generator.addMethod("isLeggings", "()", "boolean", "return toString().endsWith(\"LEGGINGS\");");
        generator.addMethod("isBoots", "()", "boolean", "return toString().endsWith(\"BOOTS\");");
        generator.addMethod("isArmor", "()", "boolean", "return isChestplate() || isHelmet() || isLeggings() || isBoots();");
        generator.addMethod("isFood", "()", "boolean", "switch (this) {\n" +
                "            case APPLE:\n" +
                "            case MUSHROOM_STEW:\n" +
                "            case BREAD:\n" +
                "            case PORKCHOP:\n" +
                "            case COOKED_PORKCHOP:\n" +
                "            case GOLDEN_APPLE:\n" +
                "            case ENCHANTED_GOLDEN_APPLE:\n" +
                "            case COD:\n" +
                "            case SALMON:\n" +
                "            case TROPICAL_FISH:\n" +
                "            case PUFFERFISH:\n" +
                "            case COOKED_COD:\n" +
                "            case COOKED_SALMON:\n" +
                "            case CAKE:\n" +
                "            case COOKIE:\n" +
                "            case MELON_SLICE:\n" +
                "            case DRIED_KELP:\n" +
                "            case BEEF:\n" +
                "            case COOKED_BEEF:\n" +
                "            case CHICKEN:\n" +
                "            case COOKED_CHICKEN:\n" +
                "            case ROTTEN_FLESH:\n" +
                "            case SPIDER_EYE:\n" +
                "            case CARROT:\n" +
                "            case POTATO:\n" +
                "            case BAKED_POTATO:\n" +
                "            case POISONOUS_POTATO:\n" +
                "            case PUMPKIN_PIE:\n" +
                "            case RABBIT:\n" +
                "            case COOKED_RABBIT:\n" +
                "            case RABBIT_STEW:\n" +
                "            case MUTTON:\n" +
                "            case COOKED_MUTTON:\n" +
                "            case BEETROOT:\n" +
                "            case BEETROOT_SOUP:\n" +
                "            case SWEET_BERRIES:\n" +
                "            case HONEY_BOTTLE:\n" +
                "                return true;\n" +
                "            default:\n" +
                "                return false;\n" +
                "        }");
        generator.addMethod("hasState", "()", "boolean", "switch (this) {\n" +
                "            case BOW:\n" +
                "            case TRIDENT:\n" +
                "            case CROSSBOW:\n" +
                "            case SHIELD:\n" +
                "                return true;\n" +
                "        }\n" +
                "\n" +
                "        return isFood();");
    }

    @Override
    protected void writeSingle(EnumGenerator generator, ItemContainer item) {
        String instanceName = item.getName().getPath().toUpperCase();
        generator.addInstance(instanceName,
                "\""+item.getName().toString()+"\"",
                item.getStackSize(),
                item.getBlock() == null? "null" : ("Block."+item.getBlock().name())
        );
    }

    @Override
    protected void postGeneration() throws IOException {}

    @Override
    protected void postWrite(EnumGenerator generator) {}
}
