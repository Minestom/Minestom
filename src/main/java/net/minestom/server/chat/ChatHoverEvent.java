package net.minestom.server.chat;

import com.google.gson.JsonObject;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.metadata.*;

/**
 * Represent a hover event for a specific portion of the message
 */
public class ChatHoverEvent {

    private String action;
    private String value;
    private JsonObject valueObject;
    private boolean isJson;

    private ChatHoverEvent(String action, String value) {
        this.action = action;
        this.value = value;
    }

    private ChatHoverEvent(String action, JsonObject valueObject) {
        this.action = action;
        this.valueObject = valueObject;
        this.isJson = true;
    }

    protected String getAction() {
        return action;
    }

    protected String getValue() {
        return value;
    }

    protected JsonObject getValueObject() {
        return valueObject;
    }

    protected boolean isJson() {
        return isJson;
    }

    /**
     * Show a {@link ColoredText} when hovered
     *
     * @param text the text to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showText(ColoredText text) {
        return new ChatHoverEvent("show_text", text.getJsonObject());
    }

    /**
     * Show a raw text when hovered
     *
     * @param text the text to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showText(String text) {
        return new ChatHoverEvent("show_text", text);
    }

    /**
     * Show an item when hovered
     *
     * @param itemStack the item to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showItem(ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        final boolean hasMeta = itemMeta != null;

        JsonObject itemJson = new JsonObject();
        // Basic Item structure
        itemJson.addProperty("id", itemStack.getMaterial().getName());
        itemJson.addProperty("Count", itemStack.getAmount());

        JsonObject tagJson = new JsonObject();

        // General tags
        itemJson.addProperty("Damage", itemStack.getDamage());
        {
            final boolean unbreakable = itemStack.isUnbreakable();
            if (unbreakable) {
                tagJson.addProperty("Unbreakable", itemStack.isUnbreakable());
            }
        }
        // TODO: CanDestroy
        {
            final int customModelData = itemStack.getCustomModelData();
            if (customModelData != 0) {
                tagJson.addProperty("CustomModelData", itemStack.getCustomModelData());
            }
        }

        // TODO: BlockTags

        // Enchantments
        // TODO: Enchantments
        // TODO: StoredEnchantments
        // TODO: RepairCost

        // TODO: Attribute modifiers

        // Potion Effects
        {
            if (hasMeta && itemMeta instanceof PotionMeta) {
                final PotionMeta potionMeta = (PotionMeta) itemMeta;
                // TODO: CustomPotionEffects
                // TODO: Potion
                // TODO: CustomPotionColor
            }
        }

        // Crossbows
        {
            if (hasMeta && itemMeta instanceof CrossbowMeta) {
                final CrossbowMeta crossbowMeta = (CrossbowMeta) itemMeta;
                // TODO: ChargedProjectiles
                // TODO: Charged
            }
        }

        // Display
        JsonObject displayJson = null;
        if (itemStack.hasDisplayName() || itemStack.hasLore()) {
            displayJson = new JsonObject();

            // Leather armor
            {
                if (hasMeta && itemMeta instanceof LeatherArmorMeta) {
                    final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
                    // TODO: Color
                }
            }

            if (itemStack.hasDisplayName()) {
                // This is done as this contains a json text component describing the item's name.
                // We replace it in the last step, as adding it now would replace it with lenient JSON which MC doesn't want.
                displayJson.addProperty("Name", "%item_name%");
            }
            if (itemStack.hasLore()) {
                // TODO: Lore
            }
        }

        // HideFlags
        if (!itemStack.getItemFlags().isEmpty()) {
            final int hideFlag = itemStack.getHideFlag();
            if (hideFlag != 0) {
                tagJson.addProperty("HideFlags", hideFlag);
            }
        }

        // WrittenBooks
        {
            if (hasMeta && itemMeta instanceof WrittenBookMeta) {
                final WrittenBookMeta writtenBookMeta = (WrittenBookMeta) itemMeta;
                // TODO: Resolved
                // TODO: Generation
                // TODO: Author
                // TODO: Title
                // TODO: Pages
            }
        }

        // Book and Quills
        {
            if (hasMeta && itemMeta instanceof WritableBookMeta) {
                final WritableBookMeta writableBookMeta = (WritableBookMeta) itemMeta;
                // TODO: Pages
            }
        }

        // Player Heads
        // TODO: Alot check https://minecraft.gamepedia.com/Player.dat_format#Item_structure#Player_Heads

        // Fireworks
        {
            if (hasMeta && itemMeta instanceof FireworkMeta) {
                final FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;
                // TODO: Alot check https://minecraft.gamepedia.com/Player.dat_format#Item_structure#Fireworks
            }
        }

        // Armorstands and Spawn Eggs
        // TODO: EntityTag

        // Buckets of Fish
        // TODO: BucketVariantTag
        // TODO: ENtityTag

        // Maps
        {
            if (hasMeta && itemMeta instanceof MapMeta) {
                final MapMeta mapMeta = (MapMeta) itemMeta;
                // TODO: Alot check https://minecraft.gamepedia.com/Player.dat_format#Item_structure#Maps
            }
        }

        // Suspicious Stew
        // TODO: Effects

        // Debug Sticks
        // TODO: DebugProperty

        // Compasses
        {
            if (hasMeta && itemMeta instanceof CompassMeta) {
                final CompassMeta compassMeta = (CompassMeta) itemMeta;
                // TODO: LodestoneTracked
                // TODO: LodestoneDimension
                // TODO: LodestonePos
            }
        }


        if (displayJson != null) {
            tagJson.add("display", displayJson);
        }
        itemJson.add("tag", tagJson);


        String item = itemJson.toString();
        item = item.replaceAll("\"(\\w+)\":", "$1:");
        if (itemStack.hasDisplayName()) {
            // TODO: Since embedded JSON is wrapped using (')s we should be able to use Regex to ignore any keys wrapped by (')s.
            item = item.replaceAll("\"%item_name%\"", '\'' + itemStack.getDisplayName().getJsonObject().toString() + '\'');
        }

        System.out.println(item);
        // Use regex to remove the qoutes around the keys (MC wants this).
        return new ChatHoverEvent("show_item", item);
    }

    /**
     * Show an entity when hovered
     *
     * @param entity the entity to show
     * @return the chat hover event
     */
    public static ChatHoverEvent showEntity(Entity entity) {
        // TODO
        /*final String id = entity.getUuid().toString();
        final String type = EntityType.fromId(entity.getEntityType())
                .getNamespaceID().replace("minecraft:", "");

        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("type", type);
        return new ChatHoverEvent("show_entity", object);*/
        throw new UnsupportedOperationException("Entity hover isn't implemented yet");
    }
}
