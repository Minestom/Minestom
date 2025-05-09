package net.minestom.server.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.color.Color;
import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.VillagerType;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfSoundVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.entity.metadata.water.AxolotlMeta;
import net.minestom.server.entity.metadata.water.fish.SalmonMeta;
import net.minestom.server.entity.metadata.water.fish.TropicalFishMeta;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.*;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.component.DataComponentImpl.register;

public class DataComponents {

    public static final DataComponent<CustomData> CUSTOM_DATA = register("custom_data", CustomData.NETWORK_TYPE, CustomData.CODEC);
    public static final DataComponent<Integer> MAX_STACK_SIZE = register("max_stack_size", NetworkBuffer.VAR_INT, Codec.INT);
    public static final DataComponent<Integer> MAX_DAMAGE = register("max_damage", NetworkBuffer.VAR_INT, Codec.INT);
    public static final DataComponent<Integer> DAMAGE = register("damage", NetworkBuffer.VAR_INT, Codec.INT);
    public static final DataComponent<Unit> UNBREAKABLE = register("unbreakable", NetworkBuffer.UNIT, Codec.UNIT);
    public static final DataComponent<Component> CUSTOM_NAME = register("custom_name", NetworkBuffer.COMPONENT, Codec.COMPONENT);
    public static final DataComponent<Component> ITEM_NAME = register("item_name", NetworkBuffer.COMPONENT, Codec.COMPONENT);
    public static final DataComponent<String> ITEM_MODEL = register("item_model", NetworkBuffer.STRING, Codec.STRING);
    public static final DataComponent<List<Component>> LORE = register("lore", NetworkBuffer.COMPONENT.list(256), Codec.COMPONENT.list(256));
    public static final DataComponent<ItemRarity> RARITY = register("rarity", ItemRarity.NETWORK_TYPE, ItemRarity.CODEC);
    public static final DataComponent<EnchantmentList> ENCHANTMENTS = register("enchantments", EnchantmentList.NETWORK_TYPE, EnchantmentList.CODEC);
    public static final DataComponent<BlockPredicates> CAN_PLACE_ON = register("can_place_on", BlockPredicates.NETWORK_TYPE, BlockPredicates.CODEC);
    public static final DataComponent<BlockPredicates> CAN_BREAK = register("can_break", BlockPredicates.NETWORK_TYPE, BlockPredicates.CODEC);
    public static final DataComponent<AttributeList> ATTRIBUTE_MODIFIERS = register("attribute_modifiers", AttributeList.NETWORK_TYPE, AttributeList.CODEC);
    public static final DataComponent<CustomModelData> CUSTOM_MODEL_DATA = register("custom_model_data", CustomModelData.NETWORK_TYPE, CustomModelData.CODEC);
    public static final DataComponent<TooltipDisplay> TOOLTIP_DISPLAY = register("tooltip_display", TooltipDisplay.NETWORK_TYPE, TooltipDisplay.CODEC);
    public static final DataComponent<Integer> REPAIR_COST = register("repair_cost", NetworkBuffer.VAR_INT, Codec.INT);
    public static final DataComponent<Unit> CREATIVE_SLOT_LOCK = register("creative_slot_lock", NetworkBuffer.UNIT, null);
    public static final DataComponent<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register("enchantment_glint_override", NetworkBuffer.BOOLEAN, Codec.BOOLEAN);
    public static final DataComponent<Unit> INTANGIBLE_PROJECTILE = register("intangible_projectile", null, Codec.UNIT);
    public static final DataComponent<Food> FOOD = register("food", Food.NETWORK_TYPE, Food.CODEC);
    public static final DataComponent<Consumable> CONSUMABLE = register("consumable", Consumable.NETWORK_TYPE, Consumable.CODEC);
    public static final DataComponent<ItemStack> USE_REMAINDER = register("use_remainder", ItemStack.NETWORK_TYPE, ItemStack.CODEC);
    public static final DataComponent<UseCooldown> USE_COOLDOWN = register("use_cooldown", UseCooldown.NETWORK_TYPE, UseCooldown.CODEC);
    public static final DataComponent<DamageResistant> DAMAGE_RESISTANT = register("damage_resistant", DamageResistant.NETWORK_TYPE, DamageResistant.CODEC);
    public static final DataComponent<Tool> TOOL = register("tool", Tool.NETWORK_TYPE, Tool.CODEC);
    public static final DataComponent<Weapon> WEAPON = register("weapon", Weapon.NETWORK_TYPE, Weapon.CODEC);
    public static final DataComponent<Integer> ENCHANTABLE = register("enchantable", NetworkBuffer.VAR_INT, wrapObject("value", Codec.INT));
    public static final DataComponent<Equippable> EQUIPPABLE = register("equippable", Equippable.NETWORK_TYPE, Equippable.CODEC);
    public static final DataComponent<ObjectSet<Material>> REPAIRABLE = register("repairable", ObjectSet.networkType(Tag.BasicType.ITEMS),
            wrapObject("items", ObjectSet.codec(Tag.BasicType.ITEMS)));
    public static final DataComponent<Unit> GLIDER = register("glider", NetworkBuffer.UNIT, Codec.UNIT);
    public static final DataComponent<String> TOOLTIP_STYLE = register("tooltip_style", NetworkBuffer.STRING, Codec.STRING);
    public static final DataComponent<DeathProtection> DEATH_PROTECTION = register("death_protection", DeathProtection.NETWORK_TYPE, DeathProtection.CODEC);
    public static final DataComponent<BlocksAttacks> BLOCKS_ATTACKS = register("blocks_attacks", BlocksAttacks.NETWORK_TYPE, BlocksAttacks.NBT_TYPE);
    public static final DataComponent<EnchantmentList> STORED_ENCHANTMENTS = register("stored_enchantments", EnchantmentList.NETWORK_TYPE, EnchantmentList.CODEC);
    public static final DataComponent<RGBLike> DYED_COLOR = register("dyed_color", Color.NETWORK_TYPE, Color.CODEC);
    public static final DataComponent<RGBLike> MAP_COLOR = register("map_color", Color.NETWORK_TYPE, Color.CODEC);
    public static final DataComponent<Integer> MAP_ID = register("map_id", NetworkBuffer.VAR_INT, Codec.INT);
    public static final DataComponent<MapDecorations> MAP_DECORATIONS = register("map_decorations", null, MapDecorations.CODEC);
    public static final DataComponent<MapPostProcessing> MAP_POST_PROCESSING = register("map_post_processing", MapPostProcessing.NETWORK_TYPE, null);
    public static final DataComponent<List<ItemStack>> CHARGED_PROJECTILES = register("charged_projectiles", ItemStack.NETWORK_TYPE.list(Short.MAX_VALUE), ItemStack.CODEC.list(Short.MAX_VALUE));
    public static final DataComponent<List<ItemStack>> BUNDLE_CONTENTS = register("bundle_contents", ItemStack.NETWORK_TYPE.list(Short.MAX_VALUE), ItemStack.CODEC.list(Short.MAX_VALUE));
    public static final DataComponent<PotionContents> POTION_CONTENTS = register("potion_contents", PotionContents.NETWORK_TYPE, PotionContents.CODEC);
    public static final DataComponent<Float> POTION_DURATION_SCALE = register("potion_duration_scale", NetworkBuffer.FLOAT, Codec.FLOAT);
    public static final DataComponent<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = register("suspicious_stew_effects", SuspiciousStewEffects.NETWORK_TYPE, SuspiciousStewEffects.CODEC);
    public static final DataComponent<WritableBookContent> WRITABLE_BOOK_CONTENT = register("writable_book_content", WritableBookContent.NETWORK_TYPE, WritableBookContent.CODEC);
    public static final DataComponent<WrittenBookContent> WRITTEN_BOOK_CONTENT = register("written_book_content", WrittenBookContent.NETWORK_TYPE, WrittenBookContent.CODEC);
    public static final DataComponent<ArmorTrim> TRIM = register("trim", ArmorTrim.NETWORK_TYPE, ArmorTrim.CODEC);
    public static final DataComponent<DebugStickState> DEBUG_STICK_STATE = register("debug_stick_state", DebugStickState.NETWORK_TYPE, DebugStickState.CODEC);
    public static final DataComponent<CustomData> ENTITY_DATA = register("entity_data", CustomData.NETWORK_TYPE, CustomData.CODEC);
    public static final DataComponent<CustomData> BUCKET_ENTITY_DATA = register("bucket_entity_data", CustomData.NETWORK_TYPE, CustomData.CODEC);
    public static final DataComponent<CustomData> BLOCK_ENTITY_DATA = register("block_entity_data", CustomData.NETWORK_TYPE, CustomData.CODEC);
    public static final DataComponent<Holder.Lazy<Instrument>> INSTRUMENT = register("instrument",
            Holder.lazyNetworkType(Registries::instrument, Instrument.REGISTRY_NETWORK_TYPE),
            Holder.lazyCodec(Registries::instrument, Instrument.REGISTRY_CODEC));
    public static final DataComponent<ProvidesTrimMaterial> PROVIDES_TRIM_MATERIAL = register("provides_trim_material", ProvidesTrimMaterial.NETWORK_TYPE, ProvidesTrimMaterial.CODEC);
    public static final DataComponent<Integer> OMINOUS_BOTTLE_AMPLIFIER = register("ominous_bottle_amplifier", NetworkBuffer.VAR_INT, Codec.INT);
    public static final DataComponent<Holder.Lazy<JukeboxSong>> JUKEBOX_PLAYABLE = register("jukebox_playable",
            Holder.lazyNetworkType(Registries::jukeboxSong, JukeboxSong.REGISTRY_NETWORK_TYPE),
            Holder.lazyCodec(Registries::jukeboxSong, JukeboxSong.REGISTRY_CODEC));
    public static final DataComponent<String> PROVIDES_BANNER_PATTERNS = register("provides_banner_patterns", NetworkBuffer.STRING, Codec.STRING);
    public static final DataComponent<List<String>> RECIPES = register("recipes", NetworkBuffer.STRING.list(Short.MAX_VALUE), Codec.STRING.list(Short.MAX_VALUE));
    public static final DataComponent<LodestoneTracker> LODESTONE_TRACKER = register("lodestone_tracker", LodestoneTracker.NETWORK_TYPE, LodestoneTracker.CODEC);
    public static final DataComponent<FireworkExplosion> FIREWORK_EXPLOSION = register("firework_explosion", FireworkExplosion.NETWORK_TYPE, FireworkExplosion.CODEC);
    public static final DataComponent<FireworkList> FIREWORKS = register("fireworks", FireworkList.NETWORK_TYPE, FireworkList.NBT_TYPE);
    public static final DataComponent<HeadProfile> PROFILE = register("profile", HeadProfile.NETWORK_TYPE, HeadProfile.CODEC);
    public static final DataComponent<String> NOTE_BLOCK_SOUND = register("note_block_sound", NetworkBuffer.STRING, Codec.STRING);
    public static final DataComponent<BannerPatterns> BANNER_PATTERNS = register("banner_patterns", BannerPatterns.NETWORK_TYPE, BannerPatterns.CODEC);
    public static final DataComponent<DyeColor> BASE_COLOR = register("base_color", DyeColor.NETWORK_TYPE, DyeColor.CODEC);
    public static final DataComponent<PotDecorations> POT_DECORATIONS = register("pot_decorations", PotDecorations.NETWORK_TYPE, PotDecorations.NBT_TYPE);
    public static final DataComponent<List<ItemStack>> CONTAINER = register("container", ItemStack.NETWORK_TYPE.list(256), ItemStack.CODEC.list(256));
    public static final DataComponent<ItemBlockState> BLOCK_STATE = register("block_state", ItemBlockState.NETWORK_TYPE, ItemBlockState.CODEC);
    public static final DataComponent<List<Bee>> BEES = register("bees", Bee.NETWORK_TYPE.list(Short.MAX_VALUE), Bee.CODEC.list());
    // Lock is an item predicate which we do not support, but can be user-represented as a compound tag (an empty tag would match everything).
    public static final DataComponent<CustomData> LOCK = register("lock", null, CustomData.CODEC);
    public static final DataComponent<SeededContainerLoot> CONTAINER_LOOT = register("container_loot", null, SeededContainerLoot.CODEC);
    public static final DataComponent<SoundEvent> BREAK_SOUND = register("break_sound", SoundEvent.NETWORK_TYPE, SoundEvent.CODEC);
    public static final DataComponent<VillagerType> VILLAGER_VARIANT = register("villager/variant", VillagerType.NETWORK_TYPE, VillagerType.CODEC);
    public static final DataComponent<DynamicRegistry.Key<WolfVariant>> WOLF_VARIANT = register("wolf/variant", WolfVariant.NETWORK_TYPE, WolfVariant.CODEC);
    public static final DataComponent<DynamicRegistry.Key<WolfSoundVariant>> WOLF_SOUND_VARIANT = register("wolf/sound_variant", WolfSoundVariant.NETWORK_TYPE, WolfSoundVariant.CODEC);
    public static final DataComponent<DyeColor> WOLF_COLLAR = register("wolf/collar", DyeColor.NETWORK_TYPE, DyeColor.CODEC);
    public static final DataComponent<FoxMeta.Variant> FOX_VARIANT = register("fox/variant", FoxMeta.Variant.NETWORK_TYPE, FoxMeta.Variant.CODEC);
    public static final DataComponent<SalmonMeta.Size> SALMON_SIZE = register("salmon/size", SalmonMeta.Size.NETWORK_TYPE, SalmonMeta.Size.CODEC);
    public static final DataComponent<ParrotMeta.Color> PARROT_VARIANT = register("parrot/variant", ParrotMeta.Color.NETWORK_TYPE, ParrotMeta.Color.CODEC);
    public static final DataComponent<TropicalFishMeta.Pattern> TROPICAL_FISH_PATTERN = register("tropical_fish/pattern", TropicalFishMeta.Pattern.NETWORK_TYPE, TropicalFishMeta.Pattern.CODEC);
    public static final DataComponent<DyeColor> TROPICAL_FISH_BASE_COLOR = register("tropical_fish/base_color", DyeColor.NETWORK_TYPE, DyeColor.CODEC);
    public static final DataComponent<DyeColor> TROPICAL_FISH_PATTERN_COLOR = register("tropical_fish/pattern_color", DyeColor.NETWORK_TYPE, DyeColor.CODEC);
    public static final DataComponent<MooshroomMeta.Variant> MOOSHROOM_VARIANT = register("mooshroom/variant", MooshroomMeta.Variant.NETWORK_TYPE, MooshroomMeta.Variant.CODEC);
    public static final DataComponent<RabbitMeta.Variant> RABBIT_VARIANT = register("rabbit/variant", RabbitMeta.Variant.NETWORK_TYPE, RabbitMeta.Variant.CODEC);
    public static final DataComponent<DynamicRegistry.Key<PigVariant>> PIG_VARIANT = register("pig/variant", PigVariant.NETWORK_TYPE, PigVariant.CODEC);
    public static final DataComponent<DynamicRegistry.Key<CowVariant>> COW_VARIANT = register("cow/variant", CowVariant.NETWORK_TYPE, CowVariant.CODEC);
    // TODO(1.21.5)
    public static final DataComponent<Unit> CHICKEN_VARIANT = register("chicken/variant", null, null);
    public static final DataComponent<DynamicRegistry.Key<FrogVariant>> FROG_VARIANT = register("frog/variant", FrogVariant.NETWORK_TYPE, FrogVariant.CODEC);
    public static final DataComponent<HorseMeta.Color> HORSE_VARIANT = register("horse/variant", HorseMeta.Color.NETWORK_TYPE, HorseMeta.Color.NBT_TYPE);
    public static final DataComponent<Holder<PaintingVariant>> PAINTING_VARIANT = register("painting/variant", PaintingVariant.NETWORK_TYPE, PaintingVariant.CODEC);
    public static final DataComponent<LlamaMeta.Variant> LLAMA_VARIANT = register("llama/variant", LlamaMeta.Variant.NETWORK_TYPE, LlamaMeta.Variant.CODEC);
    public static final DataComponent<AxolotlMeta.Variant> AXOLOTL_VARIANT = register("axolotl/variant", AxolotlMeta.Variant.NETWORK_TYPE, AxolotlMeta.Variant.CODEC);
    public static final DataComponent<DynamicRegistry.Key<CatVariant>> CAT_VARIANT = register("cat/variant", CatVariant.NETWORK_TYPE, CatVariant.NBT_TYPE);
    public static final DataComponent<DyeColor> CAT_COLLAR = register("cat/collar", DyeColor.NETWORK_TYPE, DyeColor.CODEC);
    public static final DataComponent<DyeColor> SHEEP_COLOR = register("sheep/color", DyeColor.NETWORK_TYPE, DyeColor.CODEC);
    public static final DataComponent<DyeColor> SHULKER_COLOR = register("shulker/color", DyeColor.NETWORK_TYPE, DyeColor.CODEC);

    // There are some components that are serialized to codec as an object containing a single field, for now we just inline them here.
    private static <T> @NotNull Codec<T> wrapObject(@NotNull String fieldName, @NotNull Codec<T> serializer) {
        return StructCodec.struct(fieldName, serializer, t -> t, t -> t);
    }
}
