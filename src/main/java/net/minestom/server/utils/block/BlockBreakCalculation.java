package net.minestom.server.utils.block;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.gamedata.tags.Tag.BasicType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.Tool;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BlockBreakCalculation {

    public static final int UNBREAKABLE = -1;
    private static final Tag WATER_TAG = Objects.requireNonNull(MinecraftServer.getTagManager().getTag(BasicType.FLUIDS, "minecraft:water"));
    // The vanilla client checks for bamboo breaking speed with item instanceof SwordItem.
    // We could either check all sword ID's, or the sword tag.
    // Since tags are immutable, checking the tag seems easier to understand
    private static final Tag SWORD_TAG = Objects.requireNonNull(MinecraftServer.getTagManager().getTag(BasicType.ITEMS, "minecraft:swords"));

    /**
     * Calculates the block break time in ticks
     *
     * @return the block break time in ticks, -1 if the block is unbreakable
     */
    public static int breakTicks(@NotNull Block block, @NotNull Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            // Creative can always break blocks instantly
            return 0;
        }
        // Taken from minecraft wiki Breaking#Calculation
        // https://minecraft.wiki/w/Breaking#Calculation
        // More information to mimic calculations taken from minecraft's source
        Registry.BlockEntry registry = block.registry();
        double blockHardness = registry.hardness();
        if (blockHardness == -1) {
            // Bedrock, barrier, and unbreakable blocks
            return UNBREAKABLE;
        }
        ItemStack item = player.getItemInMainHand();
        // Bamboo is hard-coded in client
        if (block.id() == Block.BAMBOO.id() || block.id() == Block.BAMBOO_SAPLING.id()) {
            if (SWORD_TAG.contains(item.material().namespace())) {
                return 0;
            }
        }
        Tool tool = item.get(ItemComponent.TOOL);
        boolean isBestTool = canBreakBlock(tool, block);
        float speedMultiplier;

        if (isBestTool) {
            speedMultiplier = getMiningSpeed(tool, block);

            // wiki seems to be incorrect here, taken from minecraft's code
            if (speedMultiplier > 1F) {
                // since data driven enchantments efficiency uses the PLAYER_MINING_EFFICIENCY attribute
                // If someone wants faster tools, they have to use player attributes or the TOOL component
                speedMultiplier += (float) player.getAttributeValue(Attribute.PLAYER_MINING_EFFICIENCY);
            }
        } else {
            speedMultiplier = 1;
        }

        if (player.hasEffect(PotionEffect.HASTE) || player.hasEffect(PotionEffect.CONDUIT_POWER)) {
            // Yes, conduit power is same as haste. I also had to go confirm, because I couldn't believe it
            speedMultiplier *= getHasteMultiplier(player);
        }

        if (player.hasEffect(PotionEffect.MINING_FATIGUE)) {
            speedMultiplier *= getMiningFatigueMultiplier(player);
        }

        speedMultiplier *= (float) player.getAttributeValue(Attribute.PLAYER_BLOCK_BREAK_SPEED);

        if (isInWater(player)) {
            speedMultiplier *= (float) player.getAttributeValue(Attribute.PLAYER_SUBMERGED_MINING_SPEED);
        }

        if (!player.isOnGround()) {
            speedMultiplier /= 5;
        }

        double damage = speedMultiplier / blockHardness;

        if (isBestTool) {
            damage /= 30;
        } else {
            damage /= 100;
        }

        if (damage >= 1) {
            // Instant breaking
            return 0;
        }

        return (int) Math.ceil(1 / damage);
    }

    private static boolean isInWater(@NotNull Player player) {
        Pos pos = player.getPosition();
        Instance instance = player.getInstance();
        double eyeY = pos.y() + player.getEyeHeight();
        int x = pos.blockX();
        int y = (int) Math.floor(eyeY);
        int z = pos.blockZ();
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Block block = instance.getBlock(eye);

        if (!WATER_TAG.contains(block.namespace())) {
            return false;
        }
        float fluidHeight = getFluidHeight(player.getInstance(), x, y, z, block);
        return eyeY < y + fluidHeight;
    }

    private static float getFluidHeight(Instance instance, int x, int y, int z, Block block) {
        Block blockAbove = instance.getBlock(x, y + 1, z);
        if (blockAbove.id() == block.id()) {
            // Full block if block above is same type
            return 1F;
        }
        // We gotta be extra careful, someone could modify properties of the block!
        String levelString = block.getProperty("level");
        if (levelString == null) {
            // Something is weird, return a full block
            return 1F;
        }

        int level;
        try {
            level = Integer.parseInt(levelString);
        } catch (Throwable ignored) {
            return 1;
        }
        if (level >= 8) {
            // These levels are as high as source blocks, but are for flowing water
            // Set the level to 0 for full source block calculation
            level = 0;
        }
        return (8 - level) / 9F;
    }

    private static float getMiningFatigueMultiplier(@NotNull Player player) {
        int level = player.getEffectLevel(PotionEffect.MINING_FATIGUE) + 1;
        // Use switch to avoid expensive Math.pow
        return switch (level) { // 0.3 ^ min(level, 4)
            case 0 -> 0;
            case 1 -> 0.3F; // 0.3 ^ 1
            case 2 -> 0.09F; // 0.3 ^ 2
            case 3 -> 0.027F; // 0.3 ^ 3
            default -> 0.0081F; // 0.3 ^ 4
        };
    }

    private static float getHasteMultiplier(@NotNull Player player) {
        // Add 1 to potion level for correct calculation
        float level = Math.max(player.getEffectLevel(PotionEffect.HASTE), player.getEffectLevel(PotionEffect.CONDUIT_POWER)) + 1;
        return (1F + 0.2F * level);
    }

    private static float getMiningSpeed(@Nullable Tool tool, @NotNull Block block) {
        if (tool == null) {
            return 1;
        }
        return tool.getSpeed(block);
    }

    private static boolean canBreakBlock(@Nullable Tool tool, @NotNull Block block) {
        return !block.registry().requiresTool() || isEffective(tool, block);
    }

    private static boolean isEffective(@Nullable Tool tool, @NotNull Block block) {
        return tool != null && tool.isCorrectForDrops(block);
    }
}
