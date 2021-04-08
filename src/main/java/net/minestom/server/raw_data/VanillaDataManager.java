package net.minestom.server.raw_data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.raw_data.RawBlockData.RawBlockStateData;
import net.minestom.server.registry.ResourceGatherer;
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

public final class VanillaDataManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaDataManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final File DEFAULT_SOURCE_FOLDER_ROOT = new File(ResourceGatherer.DATA_FOLDER, "/json");
    private final File inputFolder;
    private final String version;
    private Map<Block, RawBlockData> blocks = new HashMap<>();
    private Map<Block, RawBlockStateData> blockStates = new HashMap<>();

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
        JsonArray blocks;
        try {
            blocks = GSON.fromJson(new JsonReader(new FileReader(blocksFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find blocks.json.");
            LOGGER.error("Stopped vanilla data engine for blocks.");
            return;
        }
        for (JsonElement block : blocks) {
        }
    }

    /**
     * Refreshes the data from disk
     * Careful: This does not (re)generate any code!
     */
    public void refreshData() {
        loadData();
    }
}
