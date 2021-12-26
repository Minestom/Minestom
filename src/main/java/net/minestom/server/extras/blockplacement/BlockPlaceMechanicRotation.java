package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.NamespaceID;

import java.util.HashSet;
import java.util.Set;

public class BlockPlaceMechanicRotation {

    static void updateDataFromBlock(Block block) {
        String facing = block.getProperty("facing");
        if(facing != null) {
            if(facing.equals("up") || facing.equals("down")) {
                ROTATION_VERTICAL.add(block.id());
            }
        }

        if(PlacementRules.isWallSign(block)) {
            USE_BLOCK_FACING.add(block.namespace());
        }
    }

    static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        block = event.getBlock();

        boolean horizontalOnly = !ROTATION_VERTICAL.contains(event.getBlock().id());
        boolean usePlayerFacing = !USE_BLOCK_FACING.contains(event.getBlock().namespace());
        boolean invert = ROTATION_INVERT.contains(event.getBlock().namespace());

        Vec playerDir = event.getPlayer().getPosition().direction();

        if(usePlayerFacing) {
            double absX = Math.abs(playerDir.x());
            double absY = Math.abs(playerDir.y());
            double absZ = Math.abs(playerDir.z());

            if(!horizontalOnly && absY > absX && absY > absZ) {
                if(playerDir.y() > 0 == invert) {
                    block = block.withProperty("facing", "down");
                } else {
                    block = block.withProperty("facing", "up");
                }
            } else if(absX > absZ) {
                if(playerDir.x() > 0 == invert) {
                    block = block.withProperty("facing", "west");
                } else {
                    block = block.withProperty("facing", "east");
                }
            } else {
                if(playerDir.z() > 0 == invert) {
                    block = block.withProperty("facing", "north");
                } else {
                    block = block.withProperty("facing", "south");
                }
            }
        } else {
            BlockFace face = event.getBlockFace();

            if(invert) {
                face = face.getOppositeFace();
            }

            if(horizontalOnly && (face == BlockFace.BOTTOM || face == BlockFace.TOP)) {
                if(Math.abs(playerDir.x()) > Math.abs(playerDir.z())) {
                    if(playerDir.x() > 0 == invert) {
                        block = block.withProperty("facing", "west");
                    } else {
                        block = block.withProperty("facing", "east");
                    }
                } else {
                    if(playerDir.z() > 0 == invert) {
                        block = block.withProperty("facing", "north");
                    } else {
                        block = block.withProperty("facing", "south");
                    }
                }
                event.setBlock(block);
                return;
            }

            String faceName = face.name().toLowerCase();
            if(face == BlockFace.BOTTOM) faceName = "down";
            if(face == BlockFace.TOP) faceName = "up";

            block = block.withProperty("facing", faceName);
        }

        event.setBlock(block);
    }

    private static final Set<Integer> ROTATION_VERTICAL = new HashSet<>();
    private static Set<NamespaceID> ROTATION_INVERT = Set.of(
            NamespaceID.from("minecraft:barrel"),
            NamespaceID.from("minecraft:command_block"),
            NamespaceID.from("minecraft:repeating_command_block"),
            NamespaceID.from("minecraft:chain_command_block"),
            NamespaceID.from("minecraft:dispenser"),
            NamespaceID.from("minecraft:dropper"),
            NamespaceID.from("minecraft:chest"),
            NamespaceID.from("minecraft:trapped_chest"),
            NamespaceID.from("minecraft:observer"),
            NamespaceID.from("minecraft:beehive"),
            NamespaceID.from("minecraft:bee_nest"),
            NamespaceID.from("minecraft:piston")
    );
    private static Set<NamespaceID> USE_BLOCK_FACING = new HashSet<>(Set.of(
            NamespaceID.from("minecraft:glow_lichen"),
            NamespaceID.from("minecraft:cocoa"),
            NamespaceID.from("minecraft:dead_tube_coral_wall_fan"),
            NamespaceID.from("minecraft:dead_brain_coral_wall_fan"),
            NamespaceID.from("minecraft:dead_bubble_coral_wall_fan"),
            NamespaceID.from("minecraft:dead_fire_coral_wall_fan"),
            NamespaceID.from("minecraft:dead_horn_coral_wall_fan"),
            NamespaceID.from("minecraft:tube_coral_wall_fan"),
            NamespaceID.from("minecraft:brain_coral_wall_fan"),
            NamespaceID.from("minecraft:bubble_coral_wall_fan"),
            NamespaceID.from("minecraft:fire_coral_wall_fan"),
            NamespaceID.from("minecraft:horn_coral_wall_fan"),
            NamespaceID.from("minecraft:ladder"),
            NamespaceID.from("minecraft:tripwire_hook"),
            NamespaceID.from("minecraft:vine"),
            NamespaceID.from("minecraft:wall_torch"),
            NamespaceID.from("minecraft:lightning_rod"),
            NamespaceID.from("minecraft:end_rod"),
            NamespaceID.from("minecraft:redstone_wall_torch"),
            NamespaceID.from("minecraft:soul_wall_torch"),
            NamespaceID.from("minecraft:skeleton_wall_skull"),
            NamespaceID.from("minecraft:wither_skeleton_wall_skull"),
            NamespaceID.from("minecraft:zombie_wall_head"),
            NamespaceID.from("minecraft:player_wall_head"),
            NamespaceID.from("minecraft:creeper_wall_head"),
            NamespaceID.from("minecraft:dragon_wall_head")
    ));

}
