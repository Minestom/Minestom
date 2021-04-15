package net.minestom.server.raw_data;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockState;
import net.minestom.server.map.MapColors;
import net.minestom.server.raw_data.RawBlockData.RawBlockStateData;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class VanillaDataManager {
    public static final File DEFAULT_SOURCE_FOLDER_ROOT = new File(ResourceGatherer.DATA_FOLDER, "/json");
    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaDataManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final File inputFolder;
    private final String version;
    private final Map<Block, RawBlockData> blocks = new HashMap<>();
    private final Map<BlockState, RawBlockStateData> blockStates = new HashMap<>();

    public VanillaDataManager(@Nullable File inputFolder, @NotNull String version) {
        this.inputFolder = Objects.requireNonNullElse(inputFolder, DEFAULT_SOURCE_FOLDER_ROOT);
        this.version = version;
    }

    private void loadData() {
        loadBlockData(new File(inputFolder, version.replaceAll("\\.", "_") + "_blocks.json"));
    }

    private void loadBlockData(@NotNull File blocksFile) {
        if (!blocksFile.exists()) {
            LOGGER.error("Failed to find blocks.json.");
            LOGGER.error("Stopped vanilla data engine for blocks.");
            return;
        }
        JsonArray blocksJson;
        try {
            blocksJson = GSON.fromJson(new JsonReader(new FileReader(blocksFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find blocks.json.");
            LOGGER.error("Stopped vanilla data engine for blocks.");
            return;
        }
        for (JsonElement jsonElement : blocksJson) {
            // Load data
            JsonObject blockJson = jsonElement.getAsJsonObject();
            RawBlockData blockData = new RawBlockData();

            blockData.id = NamespaceID.from(blockJson.get("id").getAsString());
            blockData.explosionResistance = blockJson.get("explosionResistance").getAsDouble();
            blockData.friction = blockJson.get("friction").getAsDouble();
            blockData.speedFactor = blockJson.get("speedFactor").getAsDouble();
            blockData.jumpFactor = blockJson.get("jumpFactor").getAsDouble();
            blockData.defaultBlockState = blockJson.get("defaultBlockState").getAsShort();
            blockData.item = Registries.getMaterial(blockJson.get("itemId").getAsString());

            JsonArray blockStatesJson = blockJson.get("states").getAsJsonArray();
            for (JsonElement jsonElement2 : blockStatesJson) {
                JsonObject blockStateJson = jsonElement2.getAsJsonObject();
                RawBlockStateData blockStateData = new RawBlockStateData();

                blockStateData.block = blockData;

                blockStateData.id = blockStateJson.get("id").getAsShort();
                blockStateData.destroySpeed = blockStateJson.get("destroySpeed").getAsDouble();
                blockStateData.lightEmission = blockStateJson.get("lightEmission").getAsInt();
                blockStateData.occluding = blockStateJson.get("doesOcclude").getAsBoolean();
                // TODO: blockStateData.properties
                blockStateData.pushReaction = blockStateJson.get("pushReaction").getAsString();
                blockStateData.blocksMotion = blockStateJson.get("blocksMotion").getAsBoolean();
                blockStateData.isFlammable = blockStateJson.get("isFlammable").getAsBoolean();
                blockStateData.isLiquid = blockJson.get("isLiquid").getAsBoolean();
                blockStateData.isReplaceable = blockJson.get("isReplaceable").getAsBoolean();
                blockStateData.isSolid = blockJson.get("isSolid").getAsBoolean();
                blockStateData.isSolidBlocking = blockJson.get("isSolidBlocking").getAsBoolean();
                blockStateData.mapColor = MapColors.values()[blockJson.get("mapColorId").getAsInt()];
                blockStateData.boundingBox = blockJson.get("boundingBox").getAsString();
            }

            // Add to map
            {
                Block b = Registries.getBlock(blockData.id);
                // Returning the defaulted value, however make sure to include the defaulted data itself.
                // to clarify: We return AIR if it doesn't exist, but we also need Air in the HashMap
                // Therefore we check the id for this functionality.
                if (b == Block.AIR && !blockData.id.equals(Block.AIR.getId())) {
                    // This should honestly never happen as the values in the Registry are based on the json file.
                    continue;
                }
                blocks.put(b, blockData);
                for (RawBlockStateData rawBlockState : blockData.blockStates) {
                    BlockState bs = Registries.getBlockState(rawBlockState.id);
                    // Returning the defaulted value, however make sure to include the defaulted data itself.
                    if (bs == BlockState.AIR_0 && rawBlockState.id != BlockState.AIR_0.getId()) {
                        // This should honestly never happen as the values in the Registry are based on the json file.
                        continue;
                    }
                    blockStates.put(bs, rawBlockState);
                }
            }
        }
    }

    /**
     * Refreshes the data from disk
     * Careful: This does not (re)generate any code!
     */
    public void refreshData() {
        loadData();
    }

    public Stream<RawBlockData> startBlockQuery() {
        return blocks.values().stream();
    }

    public Stream<RawBlockStateData> startBlockStateQuery() {
        return blockStates.values().stream();
    }

    public RawBlockData toRaw(Block block) {
        return blocks.get(block);
    }

    public RawBlockStateData toRaw(BlockState blockState) {
        return blockStates.get(blockState);
    }

    public Block fromRaw(RawBlockData rawBlockData) {
        return Registries.getBlock(rawBlockData.id);
    }

    public BlockState fromRaw(RawBlockStateData rawBlockStateData) {
        return Registries.getBlockState(rawBlockStateData.id);
    }

}
