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
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance

class PFInstanceSpace(val instance: Instance) : IInstanceSpace {
    private val chunkSpaceMap: MutableMap<Chunk, PFColumnarSpace> = ConcurrentHashMap()
    override fun blockObjectAt(x: Int, y: Int, z: Int): IBlockObject {
        val block = instance.getBlock(x, y, z)
        return PFBlock.Companion.get(block)
    }

    override fun columnarSpaceAt(cx: Int, cz: Int): IColumnarSpace {
        val chunk = instance.getChunk(cx, cz) ?: return null
        return chunkSpaceMap.computeIfAbsent(chunk) { c: Chunk ->
            val cs = PFColumnarSpace(this, c)
            c.setColumnarSpace(cs)
            cs
        }
    }
}