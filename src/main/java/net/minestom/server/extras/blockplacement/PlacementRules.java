package net.minestom.server.extras.blockplacement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventBinding;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;

import java.util.Set;

public final class PlacementRules {

    //TODO:
    // Twisting Vines
    // Weeping Vines
    // Anvils (flip X/Z rotation)
    // Small Dripleaf (convert Y)
    // Big Dripleaf (convert Y)
    // Candles (stacking)
    // Non-collding blocks to place inside player
    // Waterlogged state
    // Bells
    // Banners (int rot)
    // Signs (int rot)
    // Doors (place upper door)
    // Sunflower (place upper sunflower)
    // Fern (place upper fern)
    // Beds (place 2nd block)
    // Turtle eggs (stacking)
    // Snow layer combination
    // Tripwire

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

    private static final EventBinding<BlockEvent> TWISTING_VINES_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isTwistingVine)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicTwistingVines::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicTwistingVines::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> WEEPING_VINES_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::isWeepingVine)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicWeepingVines::onPlace)
            .map(PlayerBlockUpdateNeighborEvent.class, BlockPlaceMechanicWeepingVines::onNeighbor)
            .build();

    private static final EventBinding<BlockEvent> ROTATION_BINDING = EventBinding.filtered(EventFilter.BLOCK, PlacementRules::hasRotation)
            .map(PlayerBlockPlaceEvent.class, BlockPlaceMechanicRotation::onPlace)
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

    /* Checks */

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

    private static boolean isChest(Block block) {
        return block.compare(Block.CHEST) || block.compare(Block.TRAPPED_CHEST);
    }

    private static boolean isPointedDripstone(Block block) {
        return block.compare(Block.POINTED_DRIPSTONE);
    }

    private static boolean isGlowLichen(Block block) {
        return block.compare(Block.GLOW_LICHEN);
    }

    private static boolean isVine(Block block) {
        return block.compare(Block.VINE);
    }

    private static boolean isTwistingVine(Block block) {
        return block.compare(Block.TWISTING_VINES);
    }

    private static boolean isWeepingVine(Block block) {
        return block.compare(Block.WEEPING_VINES);
    }

    private static boolean hasRotation(Block block) {
        return block.getProperty("facing") != null;
    }

    private static boolean hasAxis(Block block) {
        return block.getProperty("axis") != null;
    }

    private static boolean hasHalf(Block block) {
        return block.getProperty("half") != null;
    }

    /* Init */

	public static void init() {
        final String STAIRS = "minecraft:stairs";
        final String WALLS = "minecraft:walls";
        final String SLABS = "minecraft:slabs";
        final String BUTTONS = "minecraft:buttons";
        final String FENCES = "minecraft:fences";
        final String WALL_SIGNS = "minecraft:wall_signs";

        for (Tag tag : MinecraftServer.getTagManager().getTagMap().get(Tag.BasicType.BLOCKS)) {
            switch (tag.getName().toString()) {
                case STAIRS -> MINECRAFT_STAIRS = tag.getValues();
                case WALLS -> MINECRAFT_WALLS = tag.getValues();
                case SLABS -> MINECRAFT_SLABS = tag.getValues();
                case BUTTONS -> MINECRAFT_BUTTONS = tag.getValues();
                case FENCES -> MINECRAFT_FENCES = tag.getValues();
                case WALL_SIGNS -> MINECRAFT_WALL_SIGNS = tag.getValues();
            }
        }

        for(short stateId=0; stateId<Short.MAX_VALUE; stateId++) {
            Block block = Block.fromStateId(stateId);
            if(block == null) continue;

            BlockPlaceMechanicRotation.updateDataFromBlock(block);
        }

        // Replacements
        MinecraftServer.getGlobalEventHandler().register(WALL_REPLACEMENT_BINDING);

        // Blockstates
        MinecraftServer.getGlobalEventHandler().register(ROTATION_BINDING);
        MinecraftServer.getGlobalEventHandler().register(AXIS_BINDING);
        MinecraftServer.getGlobalEventHandler().register(HALF_BINDING);

        // Specific blocks
        MinecraftServer.getGlobalEventHandler().register(STAIRS_BINDING);
        MinecraftServer.getGlobalEventHandler().register(WALLS_BINDING);
        MinecraftServer.getGlobalEventHandler().register(SLAB_BINDING);
        MinecraftServer.getGlobalEventHandler().register(BUTTON_BINDING);
        MinecraftServer.getGlobalEventHandler().register(CHEST_BINDING);
        MinecraftServer.getGlobalEventHandler().register(FENCE_BINDING);
        MinecraftServer.getGlobalEventHandler().register(GLOW_LICHEN_BINDING);
        MinecraftServer.getGlobalEventHandler().register(VINE_BINDING);
        MinecraftServer.getGlobalEventHandler().register(TWISTING_VINES_BINDING);
        MinecraftServer.getGlobalEventHandler().register(WEEPING_VINES_BINDING);
        MinecraftServer.getGlobalEventHandler().register(POINTED_DRIPSTONE_BINDING);
	}

}
