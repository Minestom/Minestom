package net.minestom.server.extras.blockplacement;

import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;

import java.util.HashMap;
import java.util.Map;

final class BlockPlaceMechanicWallReplacement {
    private static final Map<NamespaceID, Block> WALL_REPLACEMENTS = new HashMap<>();

    static {
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:dead_tube_coral_fan"), Block.DEAD_TUBE_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:dead_brain_coral_fan"), Block.DEAD_BRAIN_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:dead_bubble_coral_fan"), Block.DEAD_BUBBLE_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:dead_fire_coral_fan"), Block.DEAD_FIRE_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:dead_horn_coral_fan"), Block.DEAD_HORN_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:tube_coral_fan"), Block.TUBE_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:brain_coral_fan"), Block.BRAIN_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:bubble_coral_fan"), Block.BUBBLE_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:fire_coral_fan"), Block.FIRE_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:horn_coral_fan"), Block.HORN_CORAL_WALL_FAN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:torch"), Block.WALL_TORCH);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:redstone_torch"), Block.REDSTONE_WALL_TORCH);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:soul_torch"), Block.SOUL_WALL_TORCH);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:skeleton_skull"), Block.SKELETON_WALL_SKULL);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:wither_skeleton_skull"), Block.WITHER_SKELETON_WALL_SKULL);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:zombie_head"), Block.ZOMBIE_WALL_HEAD);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:player_head"), Block.PLAYER_WALL_HEAD);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:creeper_head"), Block.CREEPER_WALL_HEAD);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:dragon_head"), Block.DRAGON_WALL_HEAD);

        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:acacia_sign"), Block.ACACIA_WALL_SIGN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:warped_sign"), Block.WARPED_WALL_SIGN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:birch_sign"), Block.BIRCH_WALL_SIGN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:oak_sign"), Block.OAK_WALL_SIGN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:crimson_sign"), Block.CRIMSON_WALL_SIGN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:dark_oak_sign"), Block.DARK_OAK_WALL_SIGN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:jungle_sign"), Block.JUNGLE_WALL_SIGN);
        WALL_REPLACEMENTS.put(NamespaceID.from("minecraft:spruce_sign"), Block.SPRUCE_WALL_SIGN);
    }

    static boolean shouldReplace(Block block) {
        return WALL_REPLACEMENTS.containsKey(block.namespace());
    }

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        Block replacement = WALL_REPLACEMENTS.get(block.namespace());
        if (replacement != null && event.getBlockFace().toDirection().normalY() == 0) {
            event.setBlock(replacement);
        }
    }
}
