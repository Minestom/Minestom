package net.minestom.server.gamedata.tags;

import com.google.gson.*;
import net.minestom.server.network.packet.server.play.TagsPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.NamespaceIDHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Handles loading and caching of tags.
 */
public class TagManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagManager.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Map<NamespaceID, Tag> cache = new ConcurrentHashMap<>();
    private final List<RequiredTag> requiredTags = new LinkedList<>();
    private final NamespaceIDHashMap<JsonObject> blockTags = new NamespaceIDHashMap<>();
    private final NamespaceIDHashMap<JsonObject> entityTypeTags = new NamespaceIDHashMap<>();
    private final NamespaceIDHashMap<JsonObject> fluidTags = new NamespaceIDHashMap<>();
    private final NamespaceIDHashMap<JsonObject> itemTags = new NamespaceIDHashMap<>();
    public TagManager() {
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("acacia_logs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("anvil"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("bamboo_plantable_on"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("banners"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("base_stone_nether"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("base_stone_overworld"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("beacon_base_blocks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("beds"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("beehives"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("bee_growables"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("birch_logs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("buttons"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("campfires"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("carpets"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("climbable"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("corals"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("coral_blocks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("coral_plants"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("crimson_stems"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("crops"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("dark_oak_logs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("doors"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("dragon_immune"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("enderman_holdable"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("fences"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("fence_gates"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("fire"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("flowers"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("flower_pots"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("gold_ores"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("guarded_by_piglins"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("hoglin_repellents"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("ice"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("impermeable"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("infiniburn_end"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("infiniburn_nether"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("infiniburn_overworld"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("jungle_logs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("leaves"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("logs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("logs_that_burn"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("mushroom_grow_block"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("non_flammable_wood"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("nylium"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("oak_logs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("piglin_repellents"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("planks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("portals"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("pressure_plates"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("prevent_mob_spawning_inside"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("rails"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("sand"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("saplings"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("shulker_boxes"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("signs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("slabs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("small_flowers"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("soul_fire_base_blocks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("soul_speed_blocks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("spruce_logs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("stairs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("standing_signs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("stone_bricks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("stone_pressure_plates"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("strider_warm_blocks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("tall_flowers"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("trapdoors"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("underwater_bonemeals"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("unstable_bottom_center"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("valid_spawn"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("walls"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wall_corals"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wall_post_override"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wall_signs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("warped_stems"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wart_blocks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wither_immune"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wither_summon_base_blocks"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wooden_buttons"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wooden_doors"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wooden_fences"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wooden_pressure_plates"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wooden_slabs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wooden_stairs"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wooden_trapdoors"));
        addRequiredTag(Tag.BasicTypes.BLOCKS, NamespaceID.from("wool"));
        addRequiredTag(Tag.BasicTypes.ENTITY_TYPES, NamespaceID.from("arrows"));
        addRequiredTag(Tag.BasicTypes.ENTITY_TYPES, NamespaceID.from("beehive_inhabitors"));
        addRequiredTag(Tag.BasicTypes.ENTITY_TYPES, NamespaceID.from("impact_projectiles"));
        addRequiredTag(Tag.BasicTypes.ENTITY_TYPES, NamespaceID.from("raiders"));
        addRequiredTag(Tag.BasicTypes.ENTITY_TYPES, NamespaceID.from("skeletons"));
        addRequiredTag(Tag.BasicTypes.FLUIDS, NamespaceID.from("lava"));
        addRequiredTag(Tag.BasicTypes.FLUIDS, NamespaceID.from("water"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("acacia_logs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("anvil"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("arrows"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("banners"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("beacon_payment_items"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("beds"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("birch_logs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("boats"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("buttons"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("carpets"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("coals"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("creeper_drop_music_discs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("crimson_stems"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("dark_oak_logs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("doors"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("fences"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("fishes"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("flowers"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("gold_ores"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("jungle_logs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("leaves"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("lectern_books"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("logs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("logs_that_burn"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("music_discs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("non_flammable_wood"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("oak_logs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("piglin_loved"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("piglin_repellents"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("planks"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("rails"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("sand"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("saplings"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("signs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("slabs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("small_flowers"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("soul_fire_base_blocks"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("spruce_logs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("stairs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("stone_bricks"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("stone_crafting_materials"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("stone_tool_materials"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("tall_flowers"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("trapdoors"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("walls"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("warped_stems"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("wooden_buttons"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("wooden_doors"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("wooden_fences"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("wooden_pressure_plates"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("wooden_slabs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("wooden_stairs"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("wooden_trapdoors"));
        addRequiredTag(Tag.BasicTypes.ITEMS, NamespaceID.from("wool"));

        // TODO: Remove and replace with dedicated Tag objects.
        loadTagsFromFileSystem();
    }

    /**
     * Loads a tag with the given name. This method reads from 'reader'. This will override the previous tag
     *
     * @param name
     * @param tagType        the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @param objectSupplier
     * @return
     */
    public Tag forceLoad(NamespaceID name, String tagType, Supplier<TagContainer> objectSupplier) {
        Tag prev = cache.getOrDefault(name, Tag.EMPTY);
        Tag result = create(prev, name, tagType, objectSupplier);
        cache.put(name, result);
        return result;
    }

    private void loadTagsFromFileSystem() {
        // Block tags
        {
            InputStream a = getClass().getResourceAsStream("/minecraft_data/tags/block_tags.json");
            if (a != null) {
                JsonArray blockTags = gson.fromJson(new BufferedReader(new InputStreamReader(a)), JsonArray.class);
                for (JsonElement bT : blockTags) {
                    JsonObject blockTag = bT.getAsJsonObject();
                    this.blockTags.put(NamespaceID.from(blockTag.get("tagName").getAsString()), blockTag);
                }
            } else {
                LOGGER.error("Could not find block tags in JAR Resources.");
            }
        }
        // Entity type tags
        {
            InputStream a = getClass().getResourceAsStream("/minecraft_data/tags/entity_type_tags.json");
            if (a != null) {
                JsonArray entityTypeTags = gson.fromJson(new BufferedReader(new InputStreamReader(a)), JsonArray.class);
                for (JsonElement etT : entityTypeTags) {
                    JsonObject entityTypeTag = etT.getAsJsonObject();
                    this.entityTypeTags.put(NamespaceID.from(entityTypeTag.get("tagName").getAsString()), entityTypeTag);
                }
            } else {
                LOGGER.error("Could not find entity type tags in JAR Resources.");
            }
        }
        // Fluid tags
        {
            InputStream a = getClass().getResourceAsStream("/minecraft_data/tags/fluid_tags.json");
            if (a != null) {
                JsonArray fluidTags = gson.fromJson(new BufferedReader(new InputStreamReader(a)), JsonArray.class);
                for (JsonElement fT : fluidTags) {
                    JsonObject fluidTag = fT.getAsJsonObject();
                    this.fluidTags.put(NamespaceID.from(fluidTag.get("tagName").getAsString()), fluidTag);
                }
            } else {
                LOGGER.error("Could not find fluid tags in JAR Resources.");
            }
        }
        // Item tags
        {
            InputStream a = getClass().getResourceAsStream("/minecraft_data/tags/item_tags.json");
            if (a != null) {
                JsonArray itemTags = gson.fromJson(new BufferedReader(new InputStreamReader(a)), JsonArray.class);
                for (JsonElement bT : itemTags) {
                    JsonObject itemTag = bT.getAsJsonObject();
                    this.itemTags.put(NamespaceID.from(itemTag.get("tagName").getAsString()), itemTag);
                }
            } else {
                LOGGER.error("Could not find item tags in JAR Resources.");
            }
        }
    }

    /**
     * Loads a tag with the given name. This method attempts to read from 'reader' if the given name is not already present in cache
     *
     * @param name
     * @param tagType        the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @param objectSupplier
     * @return
     */
    public Tag load(NamespaceID name, String tagType, Supplier<TagContainer> objectSupplier) {
        Tag prev = cache.getOrDefault(name, Tag.EMPTY);
        Tag result = cache.get(name);
        if (result == null) {
            result = create(prev, name, tagType, objectSupplier);
            cache.put(name, result);
        }
        return result;
    }

    private Tag create(Tag prev, NamespaceID name, String tagType, Supplier<TagContainer> tagContainerSupplier) {
        TagContainer container = tagContainerSupplier.get();
        try {
            return new Tag(this, name, tagType, prev, container);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to load tag due to error", e);
            return Tag.EMPTY;
        }
    }

    /**
     * Adds the required tags for the game to function correctly
     *
     * @param tags the packet to add the tags to
     */
    public void addRequiredTagsToPacket(TagsPacket tags) {
        for (RequiredTag requiredTag : requiredTags) {
            Tag tag = silentLoad(requiredTag.getName(), requiredTag.getType());
            switch (requiredTag.getType()) {
                case BLOCKS:
                    tags.blockTags.add(tag);
                    break;

                case ITEMS:
                    tags.itemTags.add(tag);
                    break;

                case FLUIDS:
                    tags.fluidTags.add(tag);
                    break;

                case ENTITY_TYPES:
                    tags.entityTags.add(tag);
                    break;
            }
        }
    }

    /**
     * Adds a required tag to send to players when they connect
     *
     * @param type type of tag to send. Required so the client knows its use
     * @param name the name of the tag to load
     */
    public void addRequiredTag(Tag.BasicTypes type, NamespaceID name) {
        requiredTags.add(new RequiredTag(type, name));
    }

    private Tag silentLoad(NamespaceID name, Tag.BasicTypes type) {
        return load(name, type.name().toLowerCase());
    }

    public Tag load(NamespaceID name, String type) {
        Supplier<TagContainer> objectSupplier;
        switch (type) {
            case "entity_types": {
                objectSupplier = () -> gson.fromJson(entityTypeTags.get(name), TagContainer.class);
                break;
            }
            case "fluids": {
                objectSupplier = () -> gson.fromJson(fluidTags.get(name), TagContainer.class);
                break;
            }
            case "items": {
                objectSupplier = () -> gson.fromJson(itemTags.get(name), TagContainer.class);
                break;
            }
            case "blocks": {
                objectSupplier = () -> gson.fromJson(blockTags.get(name), TagContainer.class);
                break;
            }
            default: {
                // TODO: This needs to be improved
                objectSupplier = () -> {
                    try {
                        return gson.fromJson(new FileReader("./minecraft_data/data/" + name.getDomain() + "/tags/" + type + "/" + name.getPath() + ".json"), TagContainer.class);
                    } catch (FileNotFoundException e) {
                        LOGGER.error("Failed to find tag '{}'. An empty tag has been returned", name);
                        // Empty tag
                        return new TagContainer();
                    }
                };
            }
        }
        return load(name, type, objectSupplier);
    }
}
