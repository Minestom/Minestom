package net.minestom.codegen.items;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.codegen.EnumGenerator;
import net.minestom.codegen.MinestomEnumGenerator;
import net.minestom.codegen.PrismarinePaths;
import net.minestom.codegen.blocks.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
            ResourceGatherer.ensureResourcesArePresent(targetVersion, null); // TODO
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
     * Extract block information from PrismarineJS (submodule of Minestom)
     * @param gson
     * @param blockFile
     * @return
     * @throws IOException
     */
    private List<PrismarineJSItem> parseItemsFromPrismarineJS(Gson gson, File blockFile) throws IOException {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(blockFile))) {
            PrismarineJSItem[] items = gson.fromJson(bufferedReader, PrismarineJSItem[].class);
            return Arrays.asList(items);
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public String getPackageName() {
        return "net.minestom.server.instance.item";
    }

    @Override
    public String getClassName() {
        return "TmpMaterial";
    }

    @Override
    protected Collection<ItemContainer> compile() throws IOException {
        Gson gson = new Gson();
        LOGGER.debug("Finding path for PrismarineJS items");
        JsonObject dataPaths = gson.fromJson(new BufferedReader(new FileReader(PRISMARINE_JS_DATA_PATHS)), JsonObject.class);
        JsonObject pathsJson = dataPaths.getAsJsonObject("pc").getAsJsonObject(targetVersion);

        PrismarinePaths paths = gson.fromJson(pathsJson, PrismarinePaths.class);
        LOGGER.debug("Loading PrismarineJS blocks data");
        List<PrismarineJSItem> prismarineJSItems = parseItemsFromPrismarineJS(gson, paths.getItemsFile());

        SortedSet<ItemContainer> items = Collections.synchronizedSortedSet(new TreeSet<>(ItemContainer::compareTo));
        for(var prismarineJSItem : prismarineJSItems) {
            items.add(new ItemContainer(prismarineJSItem.id, NamespaceID.from(prismarineJSItem.name), prismarineJSItem.stackSize, getBlock(prismarineJSItem.name.toUpperCase())));
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
        generator.addImport(Short2ObjectOpenHashMap.class.getCanonicalName());
        generator.setParams("String namespaceID", "int maxDefaultStackSize", "Block correspondingBlock");
        generator.addMethod("getId", "()", "short", "return (short)ordinal();");
        generator.addMethod("getName", "()", "String", "return namespaceID;");
        generator.addMethod("getMaxDefaultStackSize", "()", "int", "return maxDefaultStackSize;");
        generator.addMethod("isBlock", "()", "boolean", "return correspondingBlock != null && this != AIR;");
        generator.addMethod("getBlock", "()", "Block", "return correspondingBlock;");

        generator.addMethod("fromId", "(short blockId)", "static "+className, "return "+getClassName()+"Map.map.getOrDefault(blockId, AIR);");

        // hard coded methods
        generator.addMethod("isHelmet", "()", "boolean", "return toString().endsWith(\"HELMET\");");
        generator.addMethod("isChestplate", "()", "boolean", "return toString().endsWith(\"CHESTPLATE\");");
        generator.addMethod("isLeggings", "()", "boolean", "return toString().endsWith(\"LEGGINGS\");");
        generator.addMethod("isBoots", "()", "boolean", "return toString().endsWith(\"BOOTS\");");
        generator.addMethod("isArmor", "()", "boolean", "return isChestplate() || isHelmet() || isLeggings() || isBoots();");
        generator.addMethod("isFood", "()", "boolean", "return false; // TODO");
        generator.addMethod("hasState", "()", "boolean", "switch (this) {\n" +
                "            case BOW:\n" +
                "            case TRIDENT:\n" +
                "            case CROSSBOW:\n" +
                "            case SHIELD:\n" +
                "                return true;\n" +
                "        }\n" +
                "\n" +
                "        return isFood();");

        generator.appendToConstructor(getClassName()+"Map.map.put((short)ordinal(), this);");
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
    protected void postGeneration() throws IOException {
        File classFolder = new File(targetFolder, getRelativeFolderPath());
        if(!classFolder.exists()) {
            classFolder.mkdirs();
        }

        StringBuilder mapClass = new StringBuilder();
        mapClass.append("package "+getPackageName()+";\n")
                .append("import "+Short2ObjectOpenHashMap.class.getCanonicalName()+";\n")
                .append("final class "+getClassName()+"Map {\n")
                .append("\tstatic final Short2ObjectOpenHashMap<"+getClassName()+"> map = new Short2ObjectOpenHashMap<>();\n")
                .append("}\n");
        LOGGER.debug("Writing map to file: "+getRelativeFolderPath()+"/"+getClassName()+"Map.java");
        try(Writer writer = new BufferedWriter(new FileWriter(new File(classFolder, getClassName()+"Map.java")))) {
            writer.write(mapClass.toString());
        }
    }

    @Override
    protected void postWrite(EnumGenerator generator) {}
}
