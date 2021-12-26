package net.minestom.server.extras.blockplacement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventBinding;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;

import java.util.Set;

/**
 * These placement mechanics are not intended to 100% replicate vanilla <br>
 * They are intended to provide creative mode builders with the ability to place the different blockstates <br>
 * These mechanics should not be used for survival gameplay as there are likely to be some dupes <br>
 * Differences from vanilla placement include: <br>
 * <ul>
 *  <li> Not enforcing "place on" requirements (eg. saplings on dirt/grass)
 *  <li> Many "block breaking" mechanics are not implemented, eg. breaking the lower half of a sunflower won't break the top
 *  <li> Placing water/lava will not update the held item to an empty bucket
 *  <li> A number of mechanics (or small nuances of mechanics) that have yet to be implemented
 * </ul>
 */
public final class PlacementRules {

    //TODO (implement execution of secondary place events only if event isn't cancelled):
    // BlockPlaceMechanicUpper

    //TODO (remove Instance#setBlock)
    // BlockPlaceMechanicSlab

    //TODO (new mechanics):
    // Candles (stacking)
    // Turtle eggs (stacking)
    // Snow layer (stacking)
    // Beds (place 2nd block)
    // Tripwire

    //TODO (fix mechanics):
    // bell neighbor update
    // water/lava buckets consume in survival
    // BlockPlaceMechanicDoorHinge check for neighboring doors/blocks for overriding hinge

    /* Filters */

    private static final EventBinding<BlockEvent> STAIRS_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isStairs)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicStairShape::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicStairShape::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> WALLS_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isWall)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicWall::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicWall::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> SLAB_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isSlab)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicSlab::onPlace)
            .build();

    private static final EventBinding<BlockEvent> BUTTON_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isButton)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicButton::onPlace)
            .build();

    private static final EventBinding<BlockEvent> CHEST_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isChest)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicChestType::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicChestType::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> FENCE_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isFence)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicFence::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicFence::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> POINTED_DRIPSTONE_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isPointedDripstone)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicPointedDripstone::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicPointedDripstone::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> GLOW_LICHEN_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isGlowLichen)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicGlowLichen::onPlace)
            .build();

    private static final EventBinding<BlockEvent> VINE_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isVine)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicVine::onPlace)
            .build();

    private static final EventBinding<BlockEvent> BELL_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isBell)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicBell::onPlace)
            .build();

    private static final EventBinding<BlockEvent> TWISTING_VINES_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isTwistingVine)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicVerticalPlant.TWISTING_VINES::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicVerticalPlant.TWISTING_VINES::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> WEEPING_VINES_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isWeepingVine)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicVerticalPlant.WEEPING_VINES::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicVerticalPlant.WEEPING_VINES::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> BIG_DRIPLEAF_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isBigDripleaf)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicVerticalPlant.BIG_DRIPLEAF::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicVerticalPlant.BIG_DRIPLEAF::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> DOOR_HINGE_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isDoor)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicDoorHinge::onPlace)
            .build();

    private static final EventBinding<BlockEvent> UPPER_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isUpper)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicUpper::onPlace)
            .build();

    private static final EventBinding<BlockEvent> ROTATION_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::hasRotation)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicRotation::onPlace)
            .build();

    private static final EventBinding<BlockEvent> ROTATION8_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::hasRotation8)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicRotation8::onPlace)
            .build();

    private static final EventBinding<BlockEvent> AXIS_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::hasAxis)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicAxis::onPlace)
            .build();

    private static final EventBinding<BlockEvent> HALF_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::hasHalf)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicHalf::onPlace)
            .build();

    private static final EventBinding<BlockEvent> WALL_REPLACEMENT_BINDING =
            EventBinding.filtered(EventFilter.BLOCK, BlockPlaceMechanicWallReplacement::shouldReplace)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicWallReplacement::onPlace)
            .build();

    private static final EventBinding<ItemEvent> WATER_BUCKET_BINDING =
            EventBinding.filtered(EventFilter.ITEM, PlacementRules::isWaterBucket)
                    .map(PlayerUseItemOnBlockEvent.class, BlockPlaceMechanicWater::onInteract)
                    .build();

    private static final EventBinding<ItemEvent> LAVA_BUCKET_BINDING =
            EventBinding.filtered(EventFilter.ITEM, PlacementRules::isLavaBucket)
                    .map(PlayerUseItemOnBlockEvent.class, BlockPlaceMechanicLava::onInteract)
                    .build();

    /* Checks */

    private static boolean isWaterBucket(ItemStack itemStack) {
        return itemStack.getMaterial() == Material.WATER_BUCKET;
    }

    private static boolean isLavaBucket(ItemStack itemStack) {
        return itemStack.getMaterial() == Material.LAVA_BUCKET;
    }

    private static Set<NamespaceID> MINECRAFT_STAIRS;
    private static boolean isStairs(Block block) {
        return MINECRAFT_STAIRS.contains(block.namespace());
    }

    private static Set<NamespaceID> MINECRAFT_WALLS;
    public static boolean isWall(Block block) {
        return MINECRAFT_WALLS.contains(block.namespace());
    }

    private static Set<NamespaceID> MINECRAFT_SLABS;
    private static boolean isSlab(Block block) {
        return MINECRAFT_SLABS.contains(block.namespace());
    }

    private static Set<NamespaceID> MINECRAFT_BUTTONS;
    private static boolean isButton(Block block) {
        return MINECRAFT_BUTTONS.contains(block.namespace()) || block.compare(Block.LEVER);
    }

    private static Set<NamespaceID> MINECRAFT_WALL_SIGNS;
    static boolean isWallSign(Block block) {
        return MINECRAFT_WALL_SIGNS.contains(block.namespace());
    }

    private static Set<NamespaceID> MINECRAFT_FENCES;
    private static boolean isFence(Block block) {
        return MINECRAFT_FENCES.contains(block.namespace());
    }

    private static Set<NamespaceID> MINECRAFT_DOORS;
    private static boolean isDoor(Block block) {
        return MINECRAFT_DOORS.contains(block.namespace());
    }

    private static Set<NamespaceID> MINECRAFT_TALL_FLOWERS;
    private static boolean isUpper(Block block) {
        NamespaceID id = block.namespace();
        return MINECRAFT_TALL_FLOWERS.contains(id) || MINECRAFT_DOORS.contains(id) ||
                block.compare(Block.SMALL_DRIPLEAF) || block.compare(Block.LARGE_FERN);
    }

    private static boolean isChest(Block block) {
        return block.compare(Block.CHEST) || block.compare(Block.TRAPPED_CHEST);
    }

    private static boolean isPointedDripstone(Block block) {
        return block.compare(Block.POINTED_DRIPSTONE);
    }

    private static boolean isSmallDripleaf(Block block) {
        return block.compare(Block.SMALL_DRIPLEAF);
    }

    private static boolean isGlowLichen(Block block) {
        return block.compare(Block.GLOW_LICHEN);
    }

    private static boolean isVine(Block block) {
        return block.compare(Block.VINE);
    }

    private static boolean isBell(Block block) {
        return block.compare(Block.BELL);
    }

    private static boolean isTwistingVine(Block block) {
        return block.compare(Block.TWISTING_VINES) || block.compare(Block.TWISTING_VINES_PLANT);
    }

    private static boolean isWeepingVine(Block block) {
        return block.compare(Block.WEEPING_VINES) || block.compare(Block.WEEPING_VINES_PLANT);
    }

    private static boolean isBigDripleaf(Block block) {
        return block.compare(Block.BIG_DRIPLEAF) || block.compare(Block.BIG_DRIPLEAF_STEM);
    }

    private static boolean hasRotation(Block block) {
        return block.getProperty("facing") != null;
    }

    private static boolean hasRotation8(Block block) {
        return block.getProperty("rotation") != null;
    }

    private static boolean hasAxis(Block block) {
        return block.getProperty("axis") != null;
    }

    private static boolean hasHalf(Block block) {
        return block.getProperty("half") != null && !isUpper(block);
    }

    /* Init */

    public static void registerAll() {
        init();
        registerBucketFluids();
        registerPlacements();
    }

    private static void init() {
        final String STAIRS = "minecraft:stairs";
        final String WALLS = "minecraft:walls";
        final String SLABS = "minecraft:slabs";
        final String BUTTONS = "minecraft:buttons";
        final String FENCES = "minecraft:fences";
        final String WALL_SIGNS = "minecraft:wall_signs";
        final String TALL_FLOWERS = "minecraft:tall_flowers";
        final String DOORS = "minecraft:doors";

        for (Tag tag : MinecraftServer.getTagManager().getTagMap().get(Tag.BasicType.BLOCKS)) {
            switch (tag.getName().toString()) {
                case STAIRS -> MINECRAFT_STAIRS = tag.getValues();
                case WALLS -> MINECRAFT_WALLS = tag.getValues();
                case SLABS -> MINECRAFT_SLABS = tag.getValues();
                case BUTTONS -> MINECRAFT_BUTTONS = tag.getValues();
                case FENCES -> MINECRAFT_FENCES = tag.getValues();
                case WALL_SIGNS -> MINECRAFT_WALL_SIGNS = tag.getValues();
                case TALL_FLOWERS -> MINECRAFT_TALL_FLOWERS = tag.getValues();
                case DOORS -> MINECRAFT_DOORS = tag.getValues();
            }
        }

        for(short stateId=0; stateId<Short.MAX_VALUE; stateId++) {
            Block block = Block.fromStateId(stateId);
            if(block == null) continue;

            BlockPlaceMechanicRotation.updateDataFromBlock(block);
        }
    }

    public static void registerBucketFluids() {
        // Fluids
        MinecraftServer.getGlobalEventHandler().register(WATER_BUCKET_BINDING);
        MinecraftServer.getGlobalEventHandler().register(LAVA_BUCKET_BINDING);
    }

    public static void registerPlacements() {
        // Replacements (setting to a different block type)
        MinecraftServer.getGlobalEventHandler().register(WALL_REPLACEMENT_BINDING);
        MinecraftServer.getGlobalEventHandler().register(TWISTING_VINES_BINDING);
        MinecraftServer.getGlobalEventHandler().register(WEEPING_VINES_BINDING);
        MinecraftServer.getGlobalEventHandler().register(BIG_DRIPLEAF_BINDING);

        // Blockstates
        MinecraftServer.getGlobalEventHandler().register(ROTATION_BINDING);
        MinecraftServer.getGlobalEventHandler().register(ROTATION8_BINDING);
        MinecraftServer.getGlobalEventHandler().register(AXIS_BINDING);
        MinecraftServer.getGlobalEventHandler().register(HALF_BINDING);
        MinecraftServer.getGlobalEventHandler().register(UPPER_BINDING);

        // Specific blocks
        MinecraftServer.getGlobalEventHandler().register(STAIRS_BINDING);
        MinecraftServer.getGlobalEventHandler().register(WALLS_BINDING);
        MinecraftServer.getGlobalEventHandler().register(SLAB_BINDING);
        MinecraftServer.getGlobalEventHandler().register(BUTTON_BINDING);
        MinecraftServer.getGlobalEventHandler().register(CHEST_BINDING);
        MinecraftServer.getGlobalEventHandler().register(FENCE_BINDING);
        MinecraftServer.getGlobalEventHandler().register(GLOW_LICHEN_BINDING);
        MinecraftServer.getGlobalEventHandler().register(VINE_BINDING);
        MinecraftServer.getGlobalEventHandler().register(BELL_BINDING);
        MinecraftServer.getGlobalEventHandler().register(POINTED_DRIPSTONE_BINDING);
        MinecraftServer.getGlobalEventHandler().register(DOOR_HINGE_BINDING);
    }

}
