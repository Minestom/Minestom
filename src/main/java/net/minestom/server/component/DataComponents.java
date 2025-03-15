package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.color.DyeColor;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.*;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.component.DataComponentImpl.register;

public class DataComponents {

    public static final DataComponent<CustomData> CUSTOM_DATA = register("custom_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final DataComponent<Integer> MAX_STACK_SIZE = register("max_stack_size", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Integer> MAX_DAMAGE = register("max_damage", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Integer> DAMAGE = register("damage", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Unbreakable> UNBREAKABLE = register("unbreakable", Unbreakable.NETWORK_TYPE, Unbreakable.NBT_TYPE);
    public static final DataComponent<Component> CUSTOM_NAME = register("custom_name", NetworkBuffer.COMPONENT, BinaryTagSerializer.NBT_COMPONENT);
    public static final DataComponent<Component> ITEM_NAME = register("item_name", NetworkBuffer.COMPONENT, BinaryTagSerializer.NBT_COMPONENT);
    public static final DataComponent<String> ITEM_MODEL = register("item_model", NetworkBuffer.STRING, BinaryTagSerializer.STRING);
    public static final DataComponent<List<Component>> LORE = register("lore", NetworkBuffer.COMPONENT.list(256), BinaryTagSerializer.JSON_COMPONENT.list());
    public static final DataComponent<ItemRarity> RARITY = register("rarity", ItemRarity.NETWORK_TYPE, ItemRarity.NBT_TYPE);
    public static final DataComponent<EnchantmentList> ENCHANTMENTS = register("enchantments", EnchantmentList.NETWORK_TYPE, EnchantmentList.NBT_TYPE);
    public static final DataComponent<BlockPredicates> CAN_PLACE_ON = register("can_place_on", BlockPredicates.NETWORK_TYPE, BlockPredicates.NBT_TYPE);
    public static final DataComponent<BlockPredicates> CAN_BREAK = register("can_break", BlockPredicates.NETWORK_TYPE, BlockPredicates.NBT_TYPE);
    public static final DataComponent<AttributeList> ATTRIBUTE_MODIFIERS = register("attribute_modifiers", AttributeList.NETWORK_TYPE, AttributeList.NBT_TYPE);
    public static final DataComponent<CustomModelData> CUSTOM_MODEL_DATA = register("custom_model_data", CustomModelData.NETWORK_TYPE, CustomModelData.NBT_TYPE);
    public static final DataComponent<TooltipDisplay> TOOLTIP_DISPLAY = register("tooltip_display", TooltipDisplay.NETWORK_TYPE, TooltipDisplay.NBT_TYPE);
    public static final DataComponent<Integer> REPAIR_COST = register("repair_cost", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Unit> CREATIVE_SLOT_LOCK = register("creative_slot_lock", NetworkBuffer.UNIT, null);
    public static final DataComponent<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register("enchantment_glint_override", NetworkBuffer.BOOLEAN, BinaryTagSerializer.BOOLEAN);
    public static final DataComponent<Unit> INTANGIBLE_PROJECTILE = register("intangible_projectile", null, BinaryTagSerializer.UNIT);
    public static final DataComponent<Food> FOOD = register("food", Food.NETWORK_TYPE, Food.NBT_TYPE);
    public static final DataComponent<Consumable> CONSUMABLE = register("consumable", Consumable.NETWORK_TYPE, Consumable.NBT_TYPE);
    public static final DataComponent<ItemStack> USE_REMAINDER = register("use_remainder", ItemStack.NETWORK_TYPE, BinaryTagSerializer.ITEM);
    public static final DataComponent<UseCooldown> USE_COOLDOWN = register("use_cooldown", UseCooldown.NETWORK_TYPE, UseCooldown.NBT_TYPE);
    public static final DataComponent<DamageResistant> DAMAGE_RESISTANT = register("damage_resistant", DamageResistant.NETWORK_TYPE, DamageResistant.NBT_TYPE);
    public static final DataComponent<Tool> TOOL = register("tool", Tool.NETWORK_TYPE, Tool.NBT_TYPE);
    public static final DataComponent<Weapon> WEAPON = register("weapon", Weapon.NETWORK_TYPE, Weapon.NBT_TYPE);
    public static final DataComponent<Integer> ENCHANTABLE = register("enchantable", NetworkBuffer.VAR_INT, wrapObject("value", BinaryTagSerializer.INT));
    public static final DataComponent<Equippable> EQUIPPABLE = register("equippable", Equippable.NETWORK_TYPE, Equippable.NBT_TYPE);
    public static final DataComponent<ObjectSet<Material>> REPAIRABLE = register("repairable", ObjectSet.networkType(Tag.BasicType.ITEMS), wrapObject("items", ObjectSet.nbtType(Tag.BasicType.ITEMS)));
    public static final DataComponent<Unit> GLIDER = register("glider", NetworkBuffer.UNIT, BinaryTagSerializer.UNIT);
    public static final DataComponent<String> TOOLTIP_STYLE = register("tooltip_style", NetworkBuffer.STRING, BinaryTagSerializer.STRING);
    public static final DataComponent<DeathProtection> DEATH_PROTECTION = register("death_protection", DeathProtection.NETWORK_TYPE, DeathProtection.NBT_TYPE);
    // TODO(1.21.5)
    public static final DataComponent<Unit> BLOCKS_ATTACKS = register("blocks_attacks", NetworkBuffer.UNIT, BinaryTagSerializer.UNIT);
    public static final DataComponent<EnchantmentList> STORED_ENCHANTMENTS = register("stored_enchantments", EnchantmentList.NETWORK_TYPE, EnchantmentList.NBT_TYPE);
    public static final DataComponent<DyedItemColor> DYED_COLOR = register("dyed_color", DyedItemColor.NETWORK_TYPE, DyedItemColor.NBT_TYPE);
    public static final DataComponent<RGBLike> MAP_COLOR = register("map_color", Color.NETWORK_TYPE, Color.NBT_TYPE);
    public static final DataComponent<Integer> MAP_ID = register("map_id", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<MapDecorations> MAP_DECORATIONS = register("map_decorations", null, MapDecorations.NBT_TYPE);
    public static final DataComponent<MapPostProcessing> MAP_POST_PROCESSING = register("map_post_processing", MapPostProcessing.NETWORK_TYPE, null);
    public static final DataComponent<List<ItemStack>> CHARGED_PROJECTILES = register("charged_projectiles", ItemStack.NETWORK_TYPE.list(Short.MAX_VALUE), BinaryTagSerializer.ITEM.list());
    public static final DataComponent<List<ItemStack>> BUNDLE_CONTENTS = register("bundle_contents", ItemStack.NETWORK_TYPE.list(Short.MAX_VALUE), BinaryTagSerializer.ITEM.list());
    public static final DataComponent<PotionContents> POTION_CONTENTS = register("potion_contents", PotionContents.NETWORK_TYPE, PotionContents.NBT_TYPE);
    public static final DataComponent<Float> POTION_DURATION_SCALE = register("potion_duration_scale", NetworkBuffer.FLOAT, BinaryTagSerializer.FLOAT);
    public static final DataComponent<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = register("suspicious_stew_effects", SuspiciousStewEffects.NETWORK_TYPE, SuspiciousStewEffects.NBT_TYPE);
    public static final DataComponent<WritableBookContent> WRITABLE_BOOK_CONTENT = register("writable_book_content", WritableBookContent.NETWORK_TYPE, WritableBookContent.NBT_TYPE);
    public static final DataComponent<WrittenBookContent> WRITTEN_BOOK_CONTENT = register("written_book_content", WrittenBookContent.NETWORK_TYPE, WrittenBookContent.NBT_TYPE);
    public static final DataComponent<ArmorTrim> TRIM = register("trim", ArmorTrim.NETWORK_TYPE, ArmorTrim.NBT_TYPE);
    public static final DataComponent<DebugStickState> DEBUG_STICK_STATE = register("debug_stick_state", DebugStickState.NETWORK_TYPE, DebugStickState.NBT_TYPE);
    public static final DataComponent<CustomData> ENTITY_DATA = register("entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final DataComponent<CustomData> BUCKET_ENTITY_DATA = register("bucket_entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final DataComponent<CustomData> BLOCK_ENTITY_DATA = register("block_entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final DataComponent<DynamicRegistry.Key<Instrument>> INSTRUMENT = register("instrument", Instrument.NETWORK_TYPE, Instrument.NBT_TYPE);
    // TODO(1.21.5)
    public static final DataComponent<Unit> PROVIDES_TRIM_MATERIAL = register("provides_trim_material", NetworkBuffer.UNIT, BinaryTagSerializer.UNIT);
    public static final DataComponent<Integer> OMINOUS_BOTTLE_AMPLIFIER = register("ominous_bottle_amplifier", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<JukeboxPlayable> JUKEBOX_PLAYABLE = register("jukebox_playable", JukeboxPlayable.NETWORK_TYPE, JukeboxPlayable.NBT_TYPE);
    public static final DataComponent<String> PROVIDES_BANNER_PATTERNS = register("provides_banner_patterns", NetworkBuffer.STRING, BinaryTagSerializer.STRING);
    public static final DataComponent<List<String>> RECIPES = register("recipes", NetworkBuffer.STRING.list(Short.MAX_VALUE), BinaryTagSerializer.STRING.list());
    public static final DataComponent<LodestoneTracker> LODESTONE_TRACKER = register("lodestone_tracker", LodestoneTracker.NETWORK_TYPE, LodestoneTracker.NBT_TYPE);
    public static final DataComponent<FireworkExplosion> FIREWORK_EXPLOSION = register("firework_explosion", FireworkExplosion.NETWORK_TYPE, FireworkExplosion.NBT_TYPE);
    public static final DataComponent<FireworkList> FIREWORKS = register("fireworks", FireworkList.NETWORK_TYPE, FireworkList.NBT_TYPE);
    public static final DataComponent<HeadProfile> PROFILE = register("profile", HeadProfile.NETWORK_TYPE, HeadProfile.NBT_TYPE);
    public static final DataComponent<String> NOTE_BLOCK_SOUND = register("note_block_sound", NetworkBuffer.STRING, BinaryTagSerializer.STRING);
    public static final DataComponent<BannerPatterns> BANNER_PATTERNS = register("banner_patterns", BannerPatterns.NETWORK_TYPE, BannerPatterns.NBT_TYPE);
    public static final DataComponent<DyeColor> BASE_COLOR = register("base_color", DyeColor.NETWORK_TYPE, DyeColor.NBT_TYPE);
    public static final DataComponent<PotDecorations> POT_DECORATIONS = register("pot_decorations", PotDecorations.NETWORK_TYPE, PotDecorations.NBT_TYPE);
    public static final DataComponent<List<ItemStack>> CONTAINER = register("container", ItemStack.NETWORK_TYPE.list(256), BinaryTagSerializer.ITEM.list());
    public static final DataComponent<ItemBlockState> BLOCK_STATE = register("block_state", ItemBlockState.NETWORK_TYPE, ItemBlockState.NBT_TYPE);
    public static final DataComponent<List<Bee>> BEES = register("bees", Bee.NETWORK_TYPE.list(Short.MAX_VALUE), Bee.NBT_TYPE.list());
    // Lock is an item predicate which we do not support, but can be user-represented as a compound tag (an empty tag would match everything).
    public static final DataComponent<CompoundBinaryTag> LOCK = register("lock", null, BinaryTagSerializer.COMPOUND);
    public static final DataComponent<SeededContainerLoot> CONTAINER_LOOT = register("container_loot", null, SeededContainerLoot.NBT_TYPE);
    // TODO(1.21.5) ALL BELOW
    public static final DataComponent<Unit> BREAK_SOUND = register("break_sound", NetworkBuffer.UNIT, BinaryTagSerializer.UNIT);
    public static final DataComponent<Unit> VILLAGER_VARIANT = register("villager/variant", null, null);
    public static final DataComponent<Unit> WOLF_VARIANT = register("wolf/variant", null, null);
    public static final DataComponent<Unit> WOLF_SOUND_VARIANT = register("wolf/sound_variant", null, null);
    public static final DataComponent<Unit> WOLF_COLLAR = register("wolf/collar", null, null);
    public static final DataComponent<Unit> FOX_VARIANT = register("fox/variant", null, null);
    public static final DataComponent<Unit> SALMON_SIZE = register("salmon/size", null, null);
    public static final DataComponent<Unit> PARROT_VARIANT = register("parrot/variant", null, null);
    public static final DataComponent<Unit> TROPICAL_FISH_PATTERN = register("tropical_fish/pattern", null, null);
    public static final DataComponent<Unit> TROPICAL_FISH_BASE_COLOR = register("tropical_fish/base_color", null, null);
    public static final DataComponent<Unit> TROPICAL_FISH_PATTERN_COLOR = register("tropical_fish/pattern_color", null, null);
    public static final DataComponent<Unit> MOOSHROOM_VARIANT = register("mooshroom/variant", null, null);
    public static final DataComponent<Unit> RABBIT_VARIANT = register("rabbit/variant", null, null);
    public static final DataComponent<Unit> PIG_VARIANT = register("pig/variant", null, null);
    public static final DataComponent<Unit> COW_VARIANT = register("cow/variant", null, null);
    public static final DataComponent<Unit> CHICKEN_VARIANT = register("chicken/variant", null, null);
    public static final DataComponent<Unit> FROG_VARIANT = register("frog/variant", null, null);
    public static final DataComponent<Unit> HORSE_VARIANT = register("horse/variant", null, null);
    public static final DataComponent<Unit> PAINTING_VARIANT = register("painting/variant", null, null);
    public static final DataComponent<Unit> LLAMA_VARIANT = register("llama/variant", null, null);
    public static final DataComponent<Unit> AXOLOTL_VARIANT = register("axolotl/variant", null, null);
    public static final DataComponent<Unit> CAT_VARIANT = register("cat/variant", null, null);
    public static final DataComponent<Unit> CAT_COLLAR = register("cat/collar", null, null);
    public static final DataComponent<Unit> SHEEP_COLOR = register("sheep/color", null, null);
    public static final DataComponent<Unit> SHULKER_COLOR = register("shulker/color", null, null);

    // There are some components that are serialized to nbt as an object containing a single field, for now we just inline them here.
    private static <T> @NotNull BinaryTagSerializer<T> wrapObject(@NotNull String fieldName, @NotNull BinaryTagSerializer<T> serializer) {
        return BinaryTagTemplate.object(fieldName, serializer, t -> t, t -> t);
    }
}
