package net.minestom.server.entity.pathfinding

import net.minestom.server.utils.position.PositionUtils.getLookYaw
import net.minestom.server.utils.position.PositionUtils.getLookPitch
import net.minestom.server.utils.chunk.ChunkUtils.isLoaded
import com.extollit.gaming.ai.path.model.IBlockDescription
import com.extollit.gaming.ai.path.model.IBlockObject
import space.vectrix.flare.fastutil.Short2ObjectSyncMap
import net.minestom.server.entity.pathfinding.PFBlock
import net.minestom.server.entity.pathfinding.PFPathingEntity
import com.extollit.gaming.ai.path.HydrazinePathFinder
import net.minestom.server.collision.PhysicsResult
import net.minestom.server.collision.CollisionUtils
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.WorldBorder
import net.minestom.server.utils.chunk.ChunkUtils
import com.extollit.gaming.ai.path.PathOptions
import com.extollit.gaming.ai.path.model.IPath
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.pathfinding.PFInstanceSpace
import com.extollit.gaming.ai.path.model.IColumnarSpace
import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList
import com.extollit.gaming.ai.path.model.IInstanceSpace
import net.minestom.server.entity.pathfinding.PFColumnarSpace
import java.util.concurrent.ConcurrentHashMap
import com.extollit.gaming.ai.path.model.IPathingEntity
import com.extollit.gaming.ai.path.model.Passibility
import com.extollit.gaming.ai.path.model.Gravitation
import com.extollit.linalg.immutable.AxisAlignedBBox
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction
import net.minestom.server.instance.block.Block
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class PFBlock internal constructor(private val block: Block) : IBlockDescription, IBlockObject {
    override fun bounds(): AxisAlignedBBox {
        return AxisAlignedBBox(
            0, 0, 0,
            1, 1, 1
        )
    }

    override fun isFenceLike(): Boolean {
        // TODO: Use Hitbox
        // Return fences, fencegates and walls.
        // It just so happens that their namespace IDs contain "fence".
        return if (block.namespace().asString().contains("fence")) {
            true
        } else block.namespace().asString().endsWith("wall")
        // Return all walls
        // It just so happens that their namespace IDs all end with "door".
    }

    override fun isClimbable(): Boolean {
        // Return ladders and vines (including weeping and twisting vines)
        // Note that no other Namespace IDs contain "vine" except vines.
        return block.compare(Block.LADDER) || block.namespace().asString().contains("vine")
    }

    override fun isDoor(): Boolean {
        // Return all normal doors and trap doors.
        // It just so happens that their namespace IDs all end with "door".
        return block.namespace().asString().endsWith("door")
    }

    override fun isIntractable(): Boolean {
        // TODO: Interactability of blocks.
        return false
    }

    override fun isImpeding(): Boolean {
        return block.isSolid
    }

    override fun isFullyBounded(): Boolean {
        // TODO: Use Hitbox (would probably be faster as well)
        // Return false for anything that does not have a full hitbox but impedes
        // e.g. Anvils, Lilypads, Ladders, Walls, Fences, EnchantmentTables
        // Fences & Walls
        if (isFenceLike) {
            return false
        }
        // Ladders and Vines
        if (isClimbable) {
            return false
        }
        // All doors/trapdoors.
        if (isDoor) {
            return false
        }
        if (block.name().startsWith("potted")) {
            return false
        }
        // Skulls & Heads
        if (block.name().contains("skull") || block.name().contains("head")) {
            // NOTE: blocks.getName().contains("head") also matches Piston_Head
            // I could not find out by documentation if piston_head is fully bounded, I would presume it is NOT.
            return false
        }
        // Carpets
        if (block.name().endsWith("carpet")) {
            return false
        }
        // Slabs
        if (block.name().contains("slab")) {
            return false
        }
        // Beds
        if (block.name().endsWith("bed")) {
            return false
        }
        // Glass Panes
        return if (block.name().endsWith("pane")) {
            false
        } else !Block.CHORUS_FLOWER.compare(block) && !Block.CHORUS_PLANT.compare(
            block
        ) && !Block.BAMBOO.compare(block)
                && !Block.BAMBOO_SAPLING.compare(block) && !Block.SEA_PICKLE.compare(
            block
        )
                && !Block.TURTLE_EGG.compare(block) && !Block.SNOW.compare(
            block
        ) && !Block.FLOWER_POT.compare(block)
                && !Block.LILY_PAD.compare(block) && !Block.ANVIL.compare(
            block
        ) && !Block.CHIPPED_ANVIL.compare(block)
                && !Block.DAMAGED_ANVIL.compare(block) && !Block.CAKE.compare(
            block
        ) && !Block.CACTUS.compare(block)
                && !Block.BREWING_STAND.compare(block) && !Block.LECTERN.compare(
            block
        )
                && !Block.DAYLIGHT_DETECTOR.compare(block) && !Block.CAMPFIRE.compare(
            block
        )
                && !Block.SOUL_CAMPFIRE.compare(block) && !Block.ENCHANTING_TABLE.compare(
            block
        )
                && !Block.CHEST.compare(block) && !Block.ENDER_CHEST.compare(
            block
        ) && !Block.GRINDSTONE.compare(block)
                && !Block.TRAPPED_CHEST.compare(block) && !Block.SOUL_SAND.compare(
            block
        )
                && !Block.SOUL_SOIL.compare(block) && !Block.LANTERN.compare(
            block
        ) && !Block.COCOA.compare(block)
                && !Block.CONDUIT.compare(block) && !Block.DIRT_PATH.compare(
            block
        ) && !Block.FARMLAND.compare(block)
                && !Block.END_ROD.compare(block) && !Block.STONECUTTER.compare(
            block
        ) && !Block.BELL.compare(block)
    }

    override fun isLiquid(): Boolean {
        return block.isLiquid
    }

    override fun isIncinerating(): Boolean {
        return block === Block.LAVA || block === Block.FIRE || block === Block.SOUL_FIRE
    }

    companion object {
        private val BLOCK_DESCRIPTION_MAP = Short2ObjectSyncMap.hashmap<PFBlock>()

        /**
         * Gets the [PFBlock] linked to the block state id.
         *
         *
         * Cache the result if it is not already.
         *
         * @param block the block
         * @return the [PFBlock] linked to `blockStateId`
         */
        operator fun get(block: Block): PFBlock {
            return BLOCK_DESCRIPTION_MAP.computeIfAbsent(
                block.stateId(),
                Short2ObjectFunction { state: Short -> PFBlock(block) })
        }
    }
}