package net.minestom.server.gamedata.tags;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.network.packet.server.play.TagsPacket;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles loading and caching of tags
 */
public class TagManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagManager.class);
    private final Gson gson;
    private Map<NamespaceID, Tag> cache = new ConcurrentHashMap<>();
    private List<RequiredTag> requiredTags = new LinkedList<>();

    public TagManager() {
        gson = new GsonBuilder()
                .create();
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
    }

    /**
     * Loads a tag with the given name. This method attempts to read from "data/&lt;name.domain&gt;/tags/&lt;tagType&gt;/&lt;name.path&gt;.json" if the given name is not already present in cache
     *
     * @param name
     * @param tagType the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @return
     * @throws FileNotFoundException if the file does not exist
     */
    public Tag load(NamespaceID name, String tagType) throws FileNotFoundException {
        return load(name, tagType, () -> new FileReader(new File(ResourceGatherer.DATA_FOLDER, "data/" + name.getDomain() + "/tags/" + tagType + "/" + name.getPath() + ".json")));
    }

    /**
     * Loads a tag with the given name. This method attempts to read from 'reader' if the given name is not already present in cache
     *
     * @param name
     * @param tagType the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @param reader
     * @return
     */
    public Tag load(NamespaceID name, String tagType, Reader reader) throws FileNotFoundException {
        return load(name, tagType, () -> reader);
    }

    /**
     * Loads a tag with the given name. This method reads from 'reader'. This will override the previous tag
     *
     * @param name
     * @param tagType        the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @param readerSupplier
     * @return
     */
    public Tag forceLoad(NamespaceID name, String tagType, ReaderSupplierWithFileNotFound readerSupplier) throws FileNotFoundException {
        Tag prev = cache.getOrDefault(name, Tag.EMPTY);
        Tag result = create(prev, name, tagType, readerSupplier);
        cache.put(name, result);
        return result;
    }

    /**
     * Loads a tag with the given name. This method attempts to read from 'reader' if the given name is not already present in cache
     *
     * @param name
     * @param tagType        the type of the tag to load, used to resolve paths (blocks, items, entity_types, fluids, functions are the vanilla variants)
     * @param readerSupplier
     * @return
     */
    public Tag load(NamespaceID name, String tagType, ReaderSupplierWithFileNotFound readerSupplier) throws FileNotFoundException {
        Tag prev = cache.getOrDefault(name, Tag.EMPTY);
        Tag result = cache.get(name);
        if (result == null) {
            result = create(prev, name, tagType, readerSupplier);
            cache.put(name, result);
        }
        return result;
    }

    private Tag create(Tag prev, NamespaceID name, String tagType, ReaderSupplierWithFileNotFound reader) throws FileNotFoundException {
        TagContainer container = gson.fromJson(reader.get(), TagContainer.class);
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
            Tag tag = silentLoad(requiredTag.getName(), requiredTag.getType().name().toLowerCase());
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

    private Tag silentLoad(NamespaceID name, String type) {
        try {
            return load(name, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Tag.EMPTY;
        }
    }

    public interface ReaderSupplierWithFileNotFound {
        Reader get() throws FileNotFoundException;
    }
}
