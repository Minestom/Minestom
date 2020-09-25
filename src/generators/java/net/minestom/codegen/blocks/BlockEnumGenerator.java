package net.minestom.codegen.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.codegen.EnumGenerator;
import net.minestom.codegen.MinestomEnumGenerator;
import net.minestom.codegen.PrismarinePaths;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Generates a Block enum containing all data about blocks
 */
public class BlockEnumGenerator extends MinestomEnumGenerator<BlockContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockEnumGenerator.class);

    public static final String MC_DATA_BLOCKS_PATH = "minecraft_data/reports/blocks.json";

    private final String targetVersion;
    private final File targetFolder;

    private StringBuilder staticBlock = new StringBuilder();
    private Map<String, String> subclassContents = new HashMap<>();


    public static void main(String[] args) throws IOException {
        String targetVersion;
        if (args.length < 1) {
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
        if (args.length >= 2) {
            targetPart = args[1];
        }

        File targetFolder = new File(targetPart);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        new BlockEnumGenerator(targetVersion, targetFolder);
    }

    private BlockEnumGenerator(String targetVersion, File targetFolder) throws IOException {
        this.targetVersion = targetVersion;
        this.targetFolder = targetFolder;
        generateTo(targetFolder);
    }

    /**
     * Compiles all block information in a single location
     *
     * @param dataBlocks
     * @param prismarineJSBlocks
     * @param burgerBlocks
     */
    private Collection<BlockContainer> compile(List<DataReportBlock> dataBlocks, List<PrismarineJSBlock> prismarineJSBlocks, List<BurgerBlock> burgerBlocks) {
        TreeSet<BlockContainer> blocks = new TreeSet<>(BlockContainer::compareTo);
        // ensure the 3 list have the same length and order
        dataBlocks.sort(Comparator.comparing(block -> block.name.toString()));
        prismarineJSBlocks.sort(Comparator.comparing(block -> NamespaceID.from(block.name).toString()));
        burgerBlocks.sort(Comparator.comparing(block -> NamespaceID.from(block.text_id).toString()));

        // if one of these tests fail, you probably forgot to clear the minecraft_data cache before launching this program
        if (dataBlocks.size() != prismarineJSBlocks.size()) {
            throw new Error("minecraft_data block count is different from PrismarineJS count! Try clearing the minecraft_data cache");
        }
        if (prismarineJSBlocks.size() != burgerBlocks.size()) {
            throw new Error("Burger's block count is different from PrismarineJS count! Try clearing the minecraft_data cache");
        }

        for (int i = 0; i < dataBlocks.size(); i++) {
            DataReportBlock data = dataBlocks.get(i);
            PrismarineJSBlock prismarine = prismarineJSBlocks.get(i);
            BurgerBlock burger = burgerBlocks.get(i);

            assert data.name.getPath().equals(prismarine.name) && prismarine.name.equalsIgnoreCase(burger.text_id);

            List<BlockContainer.BlockState> states = new LinkedList<>();
            for (DataReportBlock.BlockState s : data.states) {
                states.add(new BlockContainer.BlockState(s.id, s.properties));
            }

            BlockContainer.BlockState defaultState = new BlockContainer.BlockState(data.defaultState.id, data.defaultState.properties);

            BlockContainer block = new BlockContainer(prismarine.id, data.name, prismarine.hardness, burger.resistance, burger.blockEntity == null ? null : NamespaceID.from(burger.blockEntity.name), defaultState, states);
            if (!"empty".equals(prismarine.boundingBox)) {
                block.setSolid();
            }
            if (data.name.equals(NamespaceID.from("minecraft:water")) || data.name.equals(NamespaceID.from("minecraft:lava"))) {
                block.setLiquid();
            }
            boolean isAir = data.name.equals(NamespaceID.from("minecraft:air")) || data.name.getPath().endsWith("_air");
            if (isAir) {
                block.setAir();
            }

            blocks.add(block);
        }

        return blocks;
    }

    /**
     * Extracts block information from Burger
     *
     * @param gson
     * @param url
     * @return
     * @throws IOException
     */
    private List<BurgerBlock> parseBlocksFromBurger(Gson gson, String url) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            LOGGER.debug("\tConnection established, reading file");
            JsonObject dictionary = gson.fromJson(bufferedReader, JsonArray.class).get(0).getAsJsonObject();
            JsonObject tileEntityMap = dictionary.getAsJsonObject("tileentity").getAsJsonObject("tileentities");

            Map<String, BurgerTileEntity> block2entityMap = new HashMap<>();
            for (var entry : tileEntityMap.entrySet()) {
                BurgerTileEntity te = gson.fromJson(entry.getValue(), BurgerTileEntity.class);
                if (te.blocks != null) {
                    for (String block : te.blocks) {
                        block2entityMap.put(block, te);
                    }
                }
            }

            JsonObject blockMap = dictionary.getAsJsonObject("blocks").getAsJsonObject("block");

            LOGGER.debug("\tExtracting blocks");
            List<BurgerBlock> blocks = new LinkedList<>();
            for (var entry : blockMap.entrySet()) {
                BurgerBlock block = gson.fromJson(entry.getValue(), BurgerBlock.class);
                block.blockEntity = block2entityMap.get(block.text_id);
                blocks.add(block);
            }

            return blocks;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Extract block information from PrismarineJS (submodule of Minestom)
     *
     * @param gson
     * @param blockFile
     * @return
     * @throws IOException
     */
    private List<PrismarineJSBlock> parseBlocksFromPrismarineJS(Gson gson, File blockFile) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(blockFile))) {
            PrismarineJSBlock[] blocks = gson.fromJson(bufferedReader, PrismarineJSBlock[].class);
            return Arrays.asList(blocks);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Extract block information from reports generated by the data extractor present in minecraft_server.jar
     *
     * @param gson
     * @param path
     * @return
     */
    private List<DataReportBlock> parseBlocksFromMCData(Gson gson, String path) {
        List<DataReportBlock> blocks = new LinkedList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            JsonObject obj = gson.fromJson(bufferedReader, JsonObject.class);
            for (var entry : obj.entrySet()) {
                NamespaceID id = NamespaceID.from(entry.getKey());
                JsonElement blockInfo = entry.getValue();
                DataReportBlock block = gson.fromJson(blockInfo, DataReportBlock.class);
                block.bindDefaultState();
                block.name = id;

                blocks.add(block);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return blocks;
    }

    @Override
    public String getPackageName() {
        return "net.minestom.server.instance.block";
    }

    @Override
    public String getClassName() {
        return "Block";
    }

    @Override
    protected Collection<BlockContainer> compile() throws IOException {
        Gson gson = new Gson();

        // load report blocks and block states
        LOGGER.debug("Loading information from data extraction");
        List<DataReportBlock> dataBlocks = parseBlocksFromMCData(gson, MC_DATA_BLOCKS_PATH);
        // load properties from Prismarine
        LOGGER.debug("Finding path for PrismarineJS blocks");
        JsonObject dataPaths = gson.fromJson(new BufferedReader(new FileReader(PRISMARINE_JS_DATA_PATHS)), JsonObject.class);
        JsonObject pathsJson = dataPaths.getAsJsonObject("pc").getAsJsonObject(targetVersion);

        PrismarinePaths paths = gson.fromJson(pathsJson, PrismarinePaths.class);
        LOGGER.debug("Loading PrismarineJS blocks data");
        List<PrismarineJSBlock> prismarineJSBlocks = parseBlocksFromPrismarineJS(gson, paths.getBlockFile());

        LOGGER.debug("Loading Burger blocks data (requires Internet connection)");
        List<BurgerBlock> burgerBlocks = parseBlocksFromBurger(gson, BURGER_URL_BASE_URL + targetVersion + ".json");

        LOGGER.debug("Compiling information");
        return compile(dataBlocks, prismarineJSBlocks, burgerBlocks);
    }

    @Override
    protected void prepare(EnumGenerator generator) {
        String className = getClassName();
        generator.addClassAnnotation("@SuppressWarnings({\"deprecation\"})");
        generator.addImport(Registries.class.getCanonicalName());
        generator.addImport(NamespaceID.class.getCanonicalName());
        generator.addImport(List.class.getCanonicalName());
        generator.addImport(ArrayList.class.getCanonicalName());
        generator.addImport(Arrays.class.getCanonicalName());
        generator.addImport(generator.getPackage() + ".states.*");
        generator.addHardcodedField("List<BlockAlternative>", "alternatives", "new ArrayList<BlockAlternative>()");

        generator.setParams("String namespaceID", "short defaultID", "double hardness", "double resistance", "boolean isAir", "boolean isSolid", "NamespaceID blockEntity", "boolean singleState");
        generator.addMethod("getBlockId", "()", "short", "return defaultID;");
        generator.addMethod("getName", "()", "String", "return namespaceID;");
        generator.addMethod("isAir", "()", "boolean", "return isAir;");
        generator.addMethod("hasBlockEntity", "()", "boolean", "return blockEntity != null;");
        generator.addMethod("getBlockEntityName", "()", "NamespaceID", "return blockEntity;");
        generator.addMethod("isSolid", "()", "boolean", "return isSolid;");
        generator.addMethod("isLiquid", "()", "boolean", "return this == WATER || this == LAVA;");
        generator.addMethod("getHardness", "()", "double", "return hardness;");
        generator.addMethod("getResistance", "()", "double", "return resistance;");
        generator.addMethod("breaksInstantaneously", "()", "boolean", "return hardness == 0;");
        generator.addMethod("addBlockAlternative", "(BlockAlternative alternative)", "void",
                "alternatives.add(alternative);",
                "BlockArray.blocks[alternative.getId()] = this;"
        );
        String[] withPropertiesLines = {
                "for (BlockAlternative alt : alternatives) {",
                "\tif (Arrays.equals(alt.getProperties(), properties)) {",
                "\t\treturn alt.getId();",
                "\t}",
                "}",
                "return defaultID;"
        };
        generator.addMethod("getAlternative", "(short blockId)", "BlockAlternative",
                "for (BlockAlternative alt : alternatives) {",
                "\tif (alt.getId() == blockId) {",
                "\t\treturn alt;",
                "\t}",
                "}",
                "return null;");
        generator.addMethod("getAlternatives", "()", "List<BlockAlternative>", "return alternatives;");
        generator.addMethod("withProperties", "(String... properties)", "short", withPropertiesLines);
        generator.addMethod("fromStateId", "(short blockStateId)", "static " + className, "return BlockArray.blocks[blockStateId];");
        generator.appendToConstructor("if(singleState) {");
        generator.appendToConstructor("\taddBlockAlternative(new BlockAlternative(defaultID));");
        generator.appendToConstructor("}");
        generator.appendToConstructor("Registries.blocks.put(NamespaceID.from(namespaceID), this);");
    }

    @Override
    protected void writeSingle(EnumGenerator generator, BlockContainer block) {
        String instanceName = block.getId().getPath().toUpperCase();
        generator.addInstance(instanceName,
                "\"" + block.getId().toString() + "\"",
                "(short) " + block.getDefaultState().getId(),
                block.getHardness(),
                block.getResistance(),
                block.isAir(),
                block.isSolid(),
                block.getBlockEntityName() != null ? "NamespaceID.from(\"" + block.getBlockEntityName() + "\")" : "null",
                block.getStates().size() == 1 // used to avoid duplicates inside the 'alternatives' field due to both constructor addition and subclasses initStates()
        );

        // do not add alternative for default states. This will be added by default inside the constructor
        if (block.getStates().size() > 1) {
            StringBuilder subclass = new StringBuilder();
            for (BlockContainer.BlockState state : block.getStates()) {
                if (state == block.getDefaultState())
                    continue;
                // generate BlockAlternative instance that will be used to lookup block alternatives

                subclass.append(instanceName).append(".addBlockAlternative(");
                subclass.append("new BlockAlternative(");
                subclass.append("(short) ").append(state.getId());

                if (state.getProperties() != null) {
                    for (var property : state.getProperties().entrySet()) {
                        subclass.append(", ");
                        subclass.append("\"").append(property.getKey()).append("=").append(property.getValue()).append("\"");
                    }
                }
                subclass.append(")").append(");\n");
            }
            String blockName = snakeCaseToCapitalizedCamelCase(block.getId().getPath());
            blockName = blockName.replace("_", "");
            subclassContents.put(blockName, subclass.toString());
            staticBlock.append("\t\t").append(blockName).append(".initStates();\n");
        }
    }

    @Override
    protected void postGeneration() throws IOException {
        File classFolder = new File(targetFolder, getRelativeFolderPath());
        if (!classFolder.exists()) {
            classFolder.mkdirs();
        }
        File subclassFolder = new File(classFolder, "states");
        if (!subclassFolder.exists()) {
            subclassFolder.mkdirs();
        }

        StringBuilder blockArrayClass = new StringBuilder();
        blockArrayClass.append("package " + getPackageName() + ";\n")
                .append("final class BlockArray {\n")
                .append("\tstatic final Block[] blocks = new Block[Short.MAX_VALUE];")
                .append("}\n");
        LOGGER.debug("Writing BlockArray to file: " + classFolder + "/BlockArray.java\n");
        try (Writer writer = new BufferedWriter(new FileWriter(new File(classFolder, "BlockArray.java")))) {
            writer.write(blockArrayClass.toString());
        }

        LOGGER.debug("Writing subclasses for block alternatives...");
        StringBuilder classContents = new StringBuilder();
        for (var entry : subclassContents.entrySet()) {
            classContents.delete(0, classContents.length());
            String subclass = entry.getKey();
            LOGGER.debug("\t Writing subclass " + subclass + "... ");

            String contents = entry.getValue();
            classContents.append("package ").append(getPackageName()).append(".states;\n");
            classContents.append("import ").append(BlockAlternative.class.getCanonicalName()).append(";\n");
            classContents.append("import static ").append(getPackageName()).append(".").append(getClassName()).append(".*;\n");
            classContents.append("/**\n");
            classContents.append(" * Completely internal. DO NOT USE. IF YOU ARE A USER AND FACE A PROBLEM WHILE USING THIS CODE, THAT'S ON YOU.\n");
            classContents.append(" */\n");
            classContents.append("@Deprecated(forRemoval = false, since = \"forever\")\n");
            classContents.append("public class ").append(subclass).append(" {\n");
            classContents.append("\tpublic static void initStates() {\n");

            String[] lines = contents.split("\n");
            for (String line : lines) {
                classContents.append("\t\t").append(line).append("\n");
            }

            classContents.append("\t}\n");
            classContents.append("}\n");

            try (Writer writer = new BufferedWriter(new FileWriter(new File(subclassFolder, subclass + ".java")))) {
                writer.write(classContents.toString());
            }
            LOGGER.debug("\t\t - Done");
        }
    }

    @Override
    protected void postWrite(EnumGenerator generator) {
        generator.setStaticInitBlock(staticBlock.toString());
    }
}
