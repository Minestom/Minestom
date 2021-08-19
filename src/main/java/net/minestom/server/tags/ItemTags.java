package net.minestom.server.tags;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public final class ItemTags {

    public static final @NotNull Tag<@NotNull Material> WOOL = get("wool");
    public static final @NotNull Tag<@NotNull Material> PLANKS = get("planks");
    public static final @NotNull Tag<@NotNull Material> STONE_BRICKS = get("stone_bricks");
    public static final @NotNull Tag<@NotNull Material> WOODEN_BUTTONS = get("wooden_buttons");
    public static final @NotNull Tag<@NotNull Material> BUTTONS = get("buttons");
    public static final @NotNull Tag<@NotNull Material> CARPETS = get("carpets");
    public static final @NotNull Tag<@NotNull Material> WOODEN_DOORS = get("wooden_doors");
    public static final @NotNull Tag<@NotNull Material> WOODEN_STAIRS = get("wooden_stairs");
    public static final @NotNull Tag<@NotNull Material> WOODEN_SLABS = get("wooden_slabs");
    public static final @NotNull Tag<@NotNull Material> WOODEN_FENCES = get("wooden_fences");
    public static final @NotNull Tag<@NotNull Material> WOODEN_PRESSURE_PLATES = get("wooden_pressure_plates");
    public static final @NotNull Tag<@NotNull Material> WOODEN_TRAPDOORS = get("wooden_trapdoors");
    public static final @NotNull Tag<@NotNull Material> DOORS = get("doors");
    public static final @NotNull Tag<@NotNull Material> SAPLINGS = get("saplings");
    public static final @NotNull Tag<@NotNull Material> LOGS_THAT_BURN = get("logs_that_burn");
    public static final @NotNull Tag<@NotNull Material> LOGS = get("logs");
    public static final @NotNull Tag<@NotNull Material> DARK_OAK_LOGS = get("dark_oak_logs");
    public static final @NotNull Tag<@NotNull Material> OAK_LOGS = get("oak_logs");
    public static final @NotNull Tag<@NotNull Material> BIRCH_LOGS = get("birch_logs");
    public static final @NotNull Tag<@NotNull Material> ACACIA_LOGS = get("acacia_logs");
    public static final @NotNull Tag<@NotNull Material> JUNGLE_LOGS = get("jungle_logs");
    public static final @NotNull Tag<@NotNull Material> SPRUCE_LOGS = get("spruce_logs");
    public static final @NotNull Tag<@NotNull Material> CRIMSON_STEMS = get("crimson_stems");
    public static final @NotNull Tag<@NotNull Material> WARPED_STEMS = get("warped_stems");
    public static final @NotNull Tag<@NotNull Material> BANNERS = get("banners");
    public static final @NotNull Tag<@NotNull Material> SAND = get("sand");
    public static final @NotNull Tag<@NotNull Material> STAIRS = get("stairs");
    public static final @NotNull Tag<@NotNull Material> SLABS = get("slabs");
    public static final @NotNull Tag<@NotNull Material> WALLS = get("walls");
    public static final @NotNull Tag<@NotNull Material> ANVIL = get("anvil");
    public static final @NotNull Tag<@NotNull Material> RAILS = get("rails");
    public static final @NotNull Tag<@NotNull Material> LEAVES = get("leaves");
    public static final @NotNull Tag<@NotNull Material> TRAPDOORS = get("trapdoors");
    public static final @NotNull Tag<@NotNull Material> SMALL_FLOWERS = get("small_flowers");
    public static final @NotNull Tag<@NotNull Material> BEDS = get("beds");
    public static final @NotNull Tag<@NotNull Material> FENCES = get("fences");
    public static final @NotNull Tag<@NotNull Material> TALL_FLOWERS = get("tall_flowers");
    public static final @NotNull Tag<@NotNull Material> FLOWERS = get("flowers");
    public static final @NotNull Tag<@NotNull Material> PIGLIN_REPELLENTS = get("piglin_repellents");
    public static final @NotNull Tag<@NotNull Material> PIGLIN_LOVED = get("piglin_loved");
    public static final @NotNull Tag<@NotNull Material> IGNORED_BY_PIGLIN_BABIES = get("ignored_by_piglin_babies");
    public static final @NotNull Tag<@NotNull Material> PIGLIN_FOOD = get("piglin_food");
    public static final @NotNull Tag<@NotNull Material> FOX_FOOD = get("fox_food");
    public static final @NotNull Tag<@NotNull Material> GOLD_ORES = get("gold_ores");
    public static final @NotNull Tag<@NotNull Material> IRON_ORES = get("iron_ores");
    public static final @NotNull Tag<@NotNull Material> DIAMOND_ORES = get("diamond_ores");
    public static final @NotNull Tag<@NotNull Material> REDSTONE_ORES = get("redstone_ores");
    public static final @NotNull Tag<@NotNull Material> LAPIS_ORES = get("lapis_ores");
    public static final @NotNull Tag<@NotNull Material> COAL_ORES = get("coal_ores");
    public static final @NotNull Tag<@NotNull Material> EMERALD_ORES = get("emerald_ores");
    public static final @NotNull Tag<@NotNull Material> COPPER_ORES = get("copper_ores");
    public static final @NotNull Tag<@NotNull Material> NON_FLAMMABLE_WOOD = get("non_flammable_wood");
    public static final @NotNull Tag<@NotNull Material> SOUL_FIRE_BASE_BLOCKS = get("soul_fire_base_blocks");
    public static final @NotNull Tag<@NotNull Material> CANDLES = get("candles");
    public static final @NotNull Tag<@NotNull Material> BOATS = get("boats");
    public static final @NotNull Tag<@NotNull Material> FISHES = get("fishes");
    public static final @NotNull Tag<@NotNull Material> SIGNS = get("signs");
    public static final @NotNull Tag<@NotNull Material> MUSIC_DISCS = get("music_discs");
    public static final @NotNull Tag<@NotNull Material> CREEPER_DROP_MUSIC_DISCS = get("creeper_drop_music_discs");
    public static final @NotNull Tag<@NotNull Material> COALS = get("coals");
    public static final @NotNull Tag<@NotNull Material> ARROWS = get("arrows");
    public static final @NotNull Tag<@NotNull Material> LECTERN_BOOKS = get("lectern_books");
    public static final @NotNull Tag<@NotNull Material> BEACON_PAYMENT_ITEMS = get("beacon_payment_items");
    public static final @NotNull Tag<@NotNull Material> STONE_TOOL_MATERIALS = get("stone_tool_materials");
    public static final @NotNull Tag<@NotNull Material> STONE_CRAFTING_MATERIALS = get("stone_crafting_materials");
    public static final @NotNull Tag<@NotNull Material> FREEZE_IMMUNE_WEARABLES = get("freeze_immune_wearables");
    public static final @NotNull Tag<@NotNull Material> AXOLOTL_TEMPT_ITEMS = get("axolotl_tempt_items");
    public static final @NotNull Tag<@NotNull Material> OCCLUDES_VIBRATION_SIGNALS = get("occludes_vibration_signals");
    public static final @NotNull Tag<@NotNull Material> CLUSTER_MAX_HARVESTABLES = get("cluster_max_harvestables");

    private static Tag<Material> get(final String name) {
        return MinecraftServer.getTagManager().get(TagType.ITEMS, "minecraft:" + name);
    }

    private ItemTags() {
    }
}
