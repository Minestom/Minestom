package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.color.Color;
import net.minestom.server.item.component.*;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.nbt.BinaryTagSerializer;

import java.util.List;

import static net.minestom.server.item.ItemComponentTypeImpl.declare;

public final class ItemComponent {
    // Note that even non-networked components are declared here as they still contribute to the component ID counter.

    public static final ItemComponentType<CustomData> CUSTOM_DATA = declare("custom_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final ItemComponentType<Integer> MAX_STACK_SIZE = declare("max_stack_size", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final ItemComponentType<Integer> MAX_DAMAGE = declare("max_damage", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final ItemComponentType<Integer> DAMAGE = declare("damage", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final ItemComponentType<Unbreakable> UNBREAKABLE = declare("unbreakable", Unbreakable.NETWORK_TYPE, Unbreakable.NBT_TYPE);
    public static final ItemComponentType<Component> CUSTOM_NAME = declare("custom_name", NetworkBuffer.COMPONENT, BinaryTagSerializer.JSON_COMPONENT);
    public static final ItemComponentType<Component> ITEM_NAME = declare("item_name", NetworkBuffer.COMPONENT, BinaryTagSerializer.JSON_COMPONENT);
    public static final ItemComponentType<List<Component>> LORE = declare("lore", NetworkBuffer.COMPONENT.list(256), BinaryTagSerializer.JSON_COMPONENT.list());
    public static final ItemComponentType<ItemRarity> RARITY = declare("rarity", ItemRarity.NETWORK_TYPE, ItemRarity.NBT_TYPE);
    public static final ItemComponentType<EnchantmentList> ENCHANTMENTS = declare("enchantments", EnchantmentList.NETWORK_TYPE, EnchantmentList.NBT_TYPE);
    public static final ItemComponentType<Void> CAN_PLACE_ON = declare("can_place_on", null, null); //todo
    public static final ItemComponentType<Void> CAN_BREAK = declare("can_break", null, null); //todo
    public static final ItemComponentType<AttributeList> ATTRIBUTE_MODIFIERS = declare("attribute_modifiers", AttributeList.NETWORK_TYPE, AttributeList.NBT_TYPE);
    public static final ItemComponentType<Integer> CUSTOM_MODEL_DATA = declare("custom_model_data", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final ItemComponentType<Void> HIDE_ADDITIONAL_TOOLTIP = declare("hide_additional_tooltip", NetworkBuffer.NOTHING, BinaryTagSerializer.NOTHING);
    public static final ItemComponentType<Void> HIDE_TOOLTIP = declare("hide_tooltip", NetworkBuffer.NOTHING, BinaryTagSerializer.NOTHING);
    public static final ItemComponentType<Integer> REPAIR_COST = declare("repair_cost", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final ItemComponentType<Unit> CREATIVE_SLOT_LOCK = declare("creative_slot_lock", NetworkBuffer.NOTHING_V2, null);
    public static final ItemComponentType<Boolean> ENCHANTMENT_GLINT_OVERRIDE = declare("enchantment_glint_override", NetworkBuffer.BOOLEAN, BinaryTagSerializer.BOOLEAN);
    public static final ItemComponentType<Void> INTANGIBLE_PROJECTILE = declare("intangible_projectile", null, BinaryTagSerializer.NOTHING);
    public static final ItemComponentType<Void> FOOD = declare("food", null, null); //todo
    public static final ItemComponentType<Void> FIRE_RESISTANT = declare("fire_resistant", NetworkBuffer.NOTHING, BinaryTagSerializer.NOTHING);
    public static final ItemComponentType<Void> TOOL = declare("tool", null, null); //todo
    public static final ItemComponentType<EnchantmentList> STORED_ENCHANTMENTS = declare("stored_enchantments", EnchantmentList.NETWORK_TYPE, EnchantmentList.NBT_TYPE);
    public static final ItemComponentType<DyedItemColor> DYED_COLOR = declare("dyed_color", DyedItemColor.NETWORK_TYPE, DyedItemColor.NBT_TYPE);
    public static final ItemComponentType<Color> MAP_COLOR = declare("map_color", NetworkBuffer.COLOR, BinaryTagSerializer.INT.map(Color::new, Color::asRGB));
    public static final ItemComponentType<Integer> MAP_ID = declare("map_id", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final ItemComponentType<MapDecorations> MAP_DECORATIONS = declare("map_decorations", null, MapDecorations.NBT_TYPE);
    public static final ItemComponentType<MapPostProcessing> MAP_POST_PROCESSING = declare("map_post_processing", MapPostProcessing.NETWORK_TYPE, null);
    public static final ItemComponentType<List<ItemStack>> CHARGED_PROJECTILES = declare("charged_projectiles", ItemStack.NETWORK_TYPE.list(Short.MAX_VALUE), BinaryTagSerializer.ITEM.list());
    public static final ItemComponentType<List<ItemStack>> BUNDLE_CONTENTS = declare("bundle_contents", ItemStack.NETWORK_TYPE.list(Short.MAX_VALUE), BinaryTagSerializer.ITEM.list());
    public static final ItemComponentType<Void> POTION_CONTENTS = declare("potion_contents", null, null); //todo
    public static final ItemComponentType<Void> SUSPICIOUS_STEW_EFFECTS = declare("suspicious_stew_effects", null, null); //todo
    public static final ItemComponentType<WritableBookContent> WRITABLE_BOOK_CONTENT = declare("writable_book_content", WritableBookContent.NETWORK_TYPE, WritableBookContent.NBT_TYPE);
    public static final ItemComponentType<WrittenBookContent> WRITTEN_BOOK_CONTENT = declare("written_book_content", WrittenBookContent.NETWORK_TYPE, WrittenBookContent.NBT_TYPE);
    public static final ItemComponentType<Void> TRIM = declare("trim", null, null); //todo
    public static final ItemComponentType<Void> DEBUG_STICK_STATE = declare("debug_stick_state", null, null); //todo
    public static final ItemComponentType<CustomData> ENTITY_DATA = declare("entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final ItemComponentType<CustomData> BUCKET_ENTITY_DATA = declare("bucket_entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final ItemComponentType<CustomData> BLOCK_ENTITY_DATA = declare("block_entity_data", CustomData.NETWORK_TYPE, CustomData.NBT_TYPE);
    public static final ItemComponentType<Void> INSTRUMENT = declare("instrument", null, null); //todo
    public static final ItemComponentType<Integer> OMINOUS_BOTTLE_AMPLIFIER = declare("ominous_bottle_amplifier", NetworkBuffer.VAR_INT, BinaryTagSerializer.INT);
    public static final ItemComponentType<List<String>> RECIPES = declare("recipes", NetworkBuffer.STRING.list(Short.MAX_VALUE), BinaryTagSerializer.STRING.list());
    public static final ItemComponentType<LodestoneTracker> LODESTONE_TRACKER = declare("lodestone_tracker", LodestoneTracker.NETWORK_TYPE, LodestoneTracker.NBT_TYPE);
    public static final ItemComponentType<FireworkExplosion> FIREWORK_EXPLOSION = declare("firework_explosion", FireworkExplosion.NETWORK_TYPE, FireworkExplosion.NBT_TYPE);
    public static final ItemComponentType<FireworkList> FIREWORKS = declare("fireworks", FireworkList.NETWORK_TYPE, FireworkList.NBT_TYPE);
    public static final ItemComponentType<Void> PROFILE = declare("profile", null, null); //todo
    public static final ItemComponentType<String> NOTE_BLOCK_SOUND = declare("note_block_sound", NetworkBuffer.STRING, BinaryTagSerializer.STRING);
    public static final ItemComponentType<Void> BANNER_PATTERNS = declare("banner_patterns", null, null); //todo
    public static final ItemComponentType<Void> BASE_COLOR = declare("base_color", null, null); //todo dyecolor is the same stringrepresentable as item rarity
    public static final ItemComponentType<Void> POT_DECORATIONS = declare("pot_decorations", null, null); //todo
    public static final ItemComponentType<List<ItemStack>> CONTAINER = declare("container", ItemStack.NETWORK_TYPE.list(256), BinaryTagSerializer.ITEM.list());
    public static final ItemComponentType<Void> BLOCK_STATE = declare("block_state", null, null); //todo
    public static final ItemComponentType<Void> BEES = declare("bees", null, null); //todo
    public static final ItemComponentType<String> LOCK = declare("lock", null, BinaryTagSerializer.STRING);
    public static final ItemComponentType<SeededContainerLoot> CONTAINER_LOOT = declare("container_loot", null, SeededContainerLoot.NBT_TYPE);

    private ItemComponent() {}
}
