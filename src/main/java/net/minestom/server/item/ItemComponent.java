package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.component.*;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.nbt.BinaryTagSerializer;

import java.util.List;

public final class ItemComponent {
    // Note that even non-networked components are registered here as they still contribute to the component ID counter.
    // The order in this file determines the component protocol IDs, so it is important to match the client.

    public static final DataComponent<CustomData> CUSTOM_DATA = DataComponent.register("custom_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final DataComponent<Integer> MAX_STACK_SIZE = DataComponent.register("max_stack_size", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Integer> MAX_DAMAGE = DataComponent.register("max_damage", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Integer> DAMAGE = DataComponent.register("damage", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Unbreakable> UNBREAKABLE = DataComponent.register("unbreakable", Unbreakable.NETWORK_TYPE, Unbreakable.NBT_TYPE);
    public static final DataComponent<Component> CUSTOM_NAME = DataComponent.register("custom_name", NetworkBuffer.COMPONENT, BinaryTagSerializer.JSON_COMPONENT);
    public static final DataComponent<Component> ITEM_NAME = DataComponent.register("item_name", NetworkBuffer.COMPONENT, BinaryTagSerializer.JSON_COMPONENT);
    public static final DataComponent<List<Component>> LORE = DataComponent.register("lore", NetworkBuffer.COMPONENT.list(256), BinaryTagSerializer.JSON_COMPONENT.list());
    public static final DataComponent<ItemRarity> RARITY = DataComponent.register("rarity", ItemRarity.NETWORK_TYPE, ItemRarity.NBT_TYPE);
    public static final DataComponent<EnchantmentList> ENCHANTMENTS = DataComponent.register("enchantments", EnchantmentList.NETWORK_TYPE, EnchantmentList.NBT_TYPE);
    public static final DataComponent<BlockPredicates> CAN_PLACE_ON = DataComponent.register("can_place_on", BlockPredicates.NETWORK_TYPE, BlockPredicates.NBT_TYPE);
    public static final DataComponent<BlockPredicates> CAN_BREAK = DataComponent.register("can_break", BlockPredicates.NETWORK_TYPE, BlockPredicates.NBT_TYPE);
    public static final DataComponent<AttributeList> ATTRIBUTE_MODIFIERS = DataComponent.register("attribute_modifiers", AttributeList.NETWORK_TYPE, AttributeList.NBT_TYPE);
    public static final DataComponent<Integer> CUSTOM_MODEL_DATA = DataComponent.register("custom_model_data", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Unit> HIDE_ADDITIONAL_TOOLTIP = DataComponent.register("hide_additional_tooltip", NetworkBuffer.UNIT, BinaryTagSerializer.UNIT);
    public static final DataComponent<Unit> HIDE_TOOLTIP = DataComponent.register("hide_tooltip", NetworkBuffer.UNIT, BinaryTagSerializer.UNIT);
    public static final DataComponent<Integer> REPAIR_COST = DataComponent.register("repair_cost", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<Unit> CREATIVE_SLOT_LOCK = DataComponent.register("creative_slot_lock", NetworkBuffer.UNIT, null);
    public static final DataComponent<Boolean> ENCHANTMENT_GLINT_OVERRIDE = DataComponent.register("enchantment_glint_override", NetworkBuffer.BOOLEAN, BinaryTagSerializer.BOOLEAN);
    public static final DataComponent<Unit> INTANGIBLE_PROJECTILE = DataComponent.register("intangible_projectile", null, BinaryTagSerializer.UNIT);
    public static final DataComponent<Food> FOOD = DataComponent.register("food", Food.NETWORK_TYPE, Food.NBT_TYPE);
    public static final DataComponent<Unit> FIRE_RESISTANT = DataComponent.register("fire_resistant", NetworkBuffer.UNIT, BinaryTagSerializer.UNIT);
    public static final DataComponent<Tool> TOOL = DataComponent.register("tool", Tool.NETWORK_TYPE, Tool.NBT_TYPE);
    public static final DataComponent<EnchantmentList> STORED_ENCHANTMENTS = DataComponent.register("stored_enchantments", EnchantmentList.NETWORK_TYPE, EnchantmentList.NBT_TYPE);
    public static final DataComponent<DyedItemColor> DYED_COLOR = DataComponent.register("dyed_color", DyedItemColor.NETWORK_TYPE, DyedItemColor.NBT_TYPE);
    public static final DataComponent<RGBLike> MAP_COLOR = DataComponent.register("map_color", Color.NETWORK_TYPE, Color.NBT_TYPE);
    public static final DataComponent<Integer> MAP_ID = DataComponent.register("map_id", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<MapDecorations> MAP_DECORATIONS = DataComponent.register("map_decorations", null, MapDecorations.NBT_TYPE);
    public static final DataComponent<MapPostProcessing> MAP_POST_PROCESSING = DataComponent.register("map_post_processing", MapPostProcessing.NETWORK_TYPE, null);
    public static final DataComponent<List<ItemStack>> CHARGED_PROJECTILES = DataComponent.register("charged_projectiles", ItemStack.NETWORK_TYPE.list(Short.MAX_VALUE), BinaryTagSerializer.ITEM.list());
    public static final DataComponent<List<ItemStack>> BUNDLE_CONTENTS = DataComponent.register("bundle_contents", ItemStack.NETWORK_TYPE.list(Short.MAX_VALUE), BinaryTagSerializer.ITEM.list());
    public static final DataComponent<PotionContents> POTION_CONTENTS = DataComponent.register("potion_contents", PotionContents.NETWORK_TYPE, PotionContents.NBT_TYPE);
    public static final DataComponent<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = DataComponent.register("suspicious_stew_effects", SuspiciousStewEffects.NETWORK_TYPE, SuspiciousStewEffects.NBT_TYPE);
    public static final DataComponent<WritableBookContent> WRITABLE_BOOK_CONTENT = DataComponent.register("writable_book_content", WritableBookContent.NETWORK_TYPE, WritableBookContent.NBT_TYPE);
    public static final DataComponent<WrittenBookContent> WRITTEN_BOOK_CONTENT = DataComponent.register("written_book_content", WrittenBookContent.NETWORK_TYPE, WrittenBookContent.NBT_TYPE);
    public static final DataComponent<ArmorTrim> TRIM = DataComponent.register("trim", ArmorTrim.NETWORK_TYPE, ArmorTrim.NBT_TYPE);
    public static final DataComponent<DebugStickState> DEBUG_STICK_STATE = DataComponent.register("debug_stick_state", null, DebugStickState.NBT_TYPE);
    public static final DataComponent<CustomData> ENTITY_DATA = DataComponent.register("entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final DataComponent<CustomData> BUCKET_ENTITY_DATA = DataComponent.register("bucket_entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final DataComponent<CustomData> BLOCK_ENTITY_DATA = DataComponent.register("block_entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final DataComponent<String> INSTRUMENT = DataComponent.register("instrument", NetworkBuffer.STRING, BinaryTagSerializer.STRING);
    public static final DataComponent<Integer> OMINOUS_BOTTLE_AMPLIFIER = DataComponent.register("ominous_bottle_amplifier", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final DataComponent<List<String>> RECIPES = DataComponent.register("recipes", NetworkBuffer.STRING.list(Short.MAX_VALUE), BinaryTagSerializer.STRING.list());
    public static final DataComponent<LodestoneTracker> LODESTONE_TRACKER = DataComponent.register("lodestone_tracker", LodestoneTracker.NETWORK_TYPE, LodestoneTracker.NBT_TYPE);
    public static final DataComponent<FireworkExplosion> FIREWORK_EXPLOSION = DataComponent.register("firework_explosion", FireworkExplosion.NETWORK_TYPE, FireworkExplosion.NBT_TYPE);
    public static final DataComponent<FireworkList> FIREWORKS = DataComponent.register("fireworks", FireworkList.NETWORK_TYPE, FireworkList.NBT_TYPE);
    public static final DataComponent<HeadProfile> PROFILE = DataComponent.register("profile", HeadProfile.NETWORK_TYPE, HeadProfile.NBT_TYPE);
    public static final DataComponent<String> NOTE_BLOCK_SOUND = DataComponent.register("note_block_sound", NetworkBuffer.STRING, BinaryTagSerializer.STRING);
    public static final DataComponent<BannerPatterns> BANNER_PATTERNS = DataComponent.register("banner_patterns", BannerPatterns.NETWORK_TYPE, BannerPatterns.NBT_TYPE);
    public static final DataComponent<DyeColor> BASE_COLOR = DataComponent.register("base_color", DyeColor.NETWORK_TYPE, DyeColor.NBT_TYPE);
    public static final DataComponent<PotDecorations> POT_DECORATIONS = DataComponent.register("pot_decorations", PotDecorations.NETWORK_TYPE, PotDecorations.NBT_TYPE);
    public static final DataComponent<List<ItemStack>> CONTAINER = DataComponent.register("container", ItemStack.NETWORK_TYPE.list(256), BinaryTagSerializer.ITEM.list());
    public static final DataComponent<ItemBlockState> BLOCK_STATE = DataComponent.register("block_state", ItemBlockState.NETWORK_TYPE, ItemBlockState.NBT_TYPE);
    public static final DataComponent<List<Bee>> BEES = DataComponent.register("bees", Bee.NETWORK_TYPE.list(Short.MAX_VALUE), Bee.NBT_TYPE.list());
    public static final DataComponent<String> LOCK = DataComponent.register("lock", null, BinaryTagSerializer.STRING);
    public static final DataComponent<SeededContainerLoot> CONTAINER_LOOT = DataComponent.register("container_loot", null, SeededContainerLoot.NBT_TYPE);

    private ItemComponent() {
    }
}
