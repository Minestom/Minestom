package net.minestom.server.instance.block;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectSortedMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.Material;
import net.minestom.server.map.MapColor;
import net.minestom.server.map.MapColors;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

class BlockImpl implements Block {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockImpl.class);
    private static final Short2ObjectSortedMap<BlockData> blockData = new Short2ObjectAVLTreeMap<>();

    static {
        loadBlockData();
    }

    private final NamespaceID namespaceID;
    private final int blockId;
    private final short minStateId, stateId;
    private final List<BlockProperty<?>> properties;
    protected BlockImpl original = null;
    private LinkedHashMap<BlockProperty<?>, Object> propertiesMap;
    private NBTCompound compound;

    private BlockImpl(NamespaceID namespaceID,
                      int blockId,
                      short minStateId, short stateId,
                      List<BlockProperty<?>> properties,
                      LinkedHashMap<BlockProperty<?>, Object> propertiesMap,
                      NBTCompound compound) {
        this.namespaceID = namespaceID;
        this.blockId = blockId;
        this.minStateId = minStateId;
        this.stateId = stateId;
        this.properties = properties;
        this.propertiesMap = propertiesMap;
        this.compound = compound;
    }

    private BlockImpl(NamespaceID namespaceID,
                      int blockId, short minStateId, short stateId,
                      List<BlockProperty<?>> properties,
                      LinkedHashMap<BlockProperty<?>, Object> propertiesMap) {
        this(namespaceID, blockId, minStateId, stateId, properties, propertiesMap, null);
    }

    @Override
    public @NotNull <T> Block withProperty(@NotNull BlockProperty<T> property, @NotNull T value) {
        if (properties.isEmpty()) {
            // This block doesn't have any state
            return this;
        }
        final int index = properties.indexOf(property);
        if (index == -1) {
            // Invalid state
            return this;
        }

        // Find properties map
        LinkedHashMap<BlockProperty<?>, Object> map;
        if (propertiesMap == null) {
            // Represents the first id, create a new map
            map = new LinkedHashMap<>();
            properties.forEach(prop -> map.put(prop, prop.equals(property) ? value : null));
        } else {
            // Change property
            map = (LinkedHashMap<BlockProperty<?>, Object>) propertiesMap.clone();
            map.put(property, value);
        }

        var block = new BlockImpl(namespaceID, blockId, minStateId, computeId(minStateId, properties, map), properties, map);
        block.original = original;
        return block;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(compound);
    }

    @Override
    public boolean hasTag(@NotNull Tag<?> tag) {
        return compound.containsKey(tag.getKey());
    }

    @Override
    public @NotNull <T> Block withTag(@NotNull Tag<T> tag, @Nullable T value) {
        if ((compound == null || compound.getKeys().isEmpty()) && value == null) {
            // No change
            return this;
        }

        // Apply tag
        NBTCompound compound = Objects.requireNonNullElseGet(this.compound, NBTCompound::new);
        tag.write(compound, value);
        if (compound.getKeys().isEmpty()) {
            compound = null;
        }

        var block = new BlockImpl(namespaceID, blockId, minStateId, stateId, properties, propertiesMap, compound);
        block.original = original;
        return block;
    }

    @Override
    public @NotNull Block getDefaultBlock() {
        return original;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return namespaceID;
    }

    @Override
    public @NotNull Map<String, String> createPropertiesMap() {
        Map<String, String> properties = new HashMap<>();
        propertiesMap.forEach((blockProperty, o) -> properties.put(blockProperty.getName(), o.toString()));
        return properties;
    }

    @Override
    public int getBlockId() {
        return blockId;
    }

    @Override
    public short getStateId() {
        return stateId;
    }

    @Override
    public @NotNull BlockData getData() {
        return blockData.get(stateId);
    }

    protected static BlockImpl create(NamespaceID namespaceID, short blockId, short minStateId, short maxStateId,
                                      short defaultStateId, List<BlockProperty<?>> properties) {
        var block = new BlockImpl(namespaceID, blockId, minStateId, defaultStateId, properties, computeMap(defaultStateId, properties));
        block.original = block;
        Block.REGISTRY.register(namespaceID, block,
                new IntRange((int) minStateId, (int) maxStateId), requestedStateId -> {
                    var requestedBlock = new BlockImpl(namespaceID, blockId, minStateId, requestedStateId, properties, computeMap(requestedStateId, properties));
                    requestedBlock.original = block;
                    return requestedBlock;
                });
        return block;
    }

    private static short computeId(short id, List<BlockProperty<?>> properties,
                                   LinkedHashMap<BlockProperty<?>, Object> propertiesMap) {
        int[] factors = computeFactors(properties);
        int index = 0;
        for (var entry : propertiesMap.entrySet()) {
            var property = entry.getKey();
            var value = entry.getValue();
            if (value != null) {
                var values = property.getPossibleValues();
                id += values.indexOf(value) * factors[index++];
            }
        }
        return id;
    }

    private static LinkedHashMap<BlockProperty<?>, Object> computeMap(short deltaId, List<BlockProperty<?>> properties) {
        LinkedHashMap<BlockProperty<?>, Object> result = new LinkedHashMap<>();
        int[] factors = computeFactors(properties);
        int index = 0;
        for (var property : properties) {
            final int factor = factors[index++];
            final int valueIndex = deltaId / factor;
            final var possibilities = property.getPossibleValues();
            final var value = possibilities.get(valueIndex);
            result.put(property, value);
        }
        return result;
    }

    private static int[] computeFactors(List<BlockProperty<?>> properties) {
        final int size = properties.size();
        int[] result = new int[size];
        int factor = 1;
        ListIterator<BlockProperty<?>> li = properties.listIterator(properties.size());
        // Iterate in reverse.
        int i = size;
        while (li.hasPrevious()) {
            var property = li.previous();
            result[--i] = factor;
            factor *= property.getPossibleValues().size();
        }

        return result;
    }

    /**
     * Loads the {@link BlockData} from the JAR Resources to the Map.
     */
    private static void loadBlockData() {
        // E.G. 1_16_5_blocks.json
        InputStream blocksIS = BlockImpl.class.getResourceAsStream("/minestom_data/" + MinecraftServer.VERSION_NAME_UNDERSCORED + "_blocks.json");
        if (blocksIS == null) {
            LOGGER.error("Failed to find blocks.json");
            return;
        }
        // Get map Colors as we will need these
        MapColor[] mapColors = MapColors.values();

        JsonArray blocks = new Gson().fromJson(new InputStreamReader(blocksIS), JsonArray.class);
        for (JsonElement blockEntry : blocks) {
            // Load Data
            JsonObject block = blockEntry.getAsJsonObject();
            double explosionResistance = block.get("explosionResistance").getAsDouble();
            double friction = block.get("friction").getAsDouble();
            double speedFactor = block.get("speedFactor").getAsDouble();
            double jumpFactor = block.get("jumpFactor").getAsDouble();
            boolean blockEntity = block.get("blockEntity").getAsBoolean();
            // TODO: Get Item from (new) Material registry
            Material item = net.minestom.server.registry.Registry.MATERIAL_REGISTRY.get(block.get("itemId").getAsString());
            JsonArray states = block.get("states").getAsJsonArray();
            for (JsonElement stateEntry : states) {
                // Load Data
                JsonObject state = stateEntry.getAsJsonObject();

                short stateId = state.get("id").getAsShort();
                blockData.put(
                        stateId,
                        new BlockDataImpl(
                                explosionResistance,
                                item,
                                friction,
                                speedFactor,
                                jumpFactor,
                                blockEntity,

                                state.get("destroySpeed").getAsDouble(),
                                state.get("lightEmission").getAsInt(),
                                state.get("doesOcclude").getAsBoolean(),
                                state.get("pushReaction").getAsString(),
                                state.get("blocksMotion").getAsBoolean(),
                                state.get("isFlammable").getAsBoolean(),
                                state.get("air").getAsBoolean(),
                                state.get("isLiquid").getAsBoolean(),
                                state.get("isReplaceable").getAsBoolean(),
                                state.get("isSolid").getAsBoolean(),
                                state.get("isSolidBlocking").getAsBoolean(),
                                mapColors[state.get("mapColorId").getAsInt()],
                                state.get("boundingBox").getAsString()
                        )
                );
            }
        }
    }
}
