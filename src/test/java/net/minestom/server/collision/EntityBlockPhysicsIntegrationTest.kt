package net.minestom.server.collision

import net.minestom.server.ServerProcess.ticker
import net.minestom.server.ServerProcess.Ticker.tick
import net.minestom.server.ServerProcess.instance
import net.minestom.server.ServerProcess.eventHandler
import org.jglrxavpok.hephaistos.parser.SNBTParser.parse
import net.minestom.server.ServerProcess.connection
import net.minestom.server.MinecraftServer.Companion.updateProcess
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.setInt
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.toCompound
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.set
import org.jglrxavpok.hephaistos.nbt.NBTCompound.toSNBT
import org.jglrxavpok.hephaistos.nbt.NBTList.size
import org.jglrxavpok.hephaistos.nbt.NBTList.asListView
import org.jglrxavpok.hephaistos.nbt.NBTString.value
import net.minestom.server.utils.collection.IndexMap.get
import net.minestom.server.utils.collection.ObjectArray.set
import net.minestom.server.utils.collection.ObjectArray.get
import net.minestom.server.utils.collection.ObjectArray.trim
import net.minestom.server.utils.mojang.MojangUtils.fromUsername
import net.minestom.server.utils.position.PositionUtils.getLookYaw
import net.minestom.server.utils.position.PositionUtils.getLookPitch
import net.minestom.server.world.DimensionType.Companion.builder
import net.minestom.server.world.DimensionType.DimensionTypeBuilder.build
import net.minestom.server.ServerProcess.dimension
import net.minestom.server.world.DimensionTypeManager.addDimension
import net.minestom.server.command.builder.arguments.ArgumentType.Literal
import net.minestom.server.utils.binary.PooledBuffers.packetBuffer
import net.minestom.server.utils.binary.BinaryBuffer.Companion.wrap
import net.minestom.server.utils.binary.BinaryBuffer.reset
import net.minestom.server.utils.Utils.writeVarInt
import net.minestom.server.utils.Utils.getVarIntSize
import net.minestom.server.utils.binary.BinaryBuffer.readableBytes
import net.minestom.server.utils.binary.PooledBuffers.clear
import net.minestom.server.utils.binary.PooledBuffers.count
import net.minestom.server.utils.binary.PooledBuffers.get
import net.minestom.server.utils.binary.PooledBuffers.bufferSize
import net.minestom.server.utils.binary.BinaryBuffer.capacity
import net.minestom.server.utils.binary.PooledBuffers.add
import net.minestom.server.utils.binary.Writeable.write
import net.minestom.server.utils.binary.BinaryWriter.toByteArray
import net.minestom.server.MinecraftServer.Companion.chunkViewDistance
import net.minestom.server.utils.chunk.ChunkUtils.getChunkCount
import net.minestom.server.entity.metadata.other.SlimeMeta.size
import net.minestom.server.MinecraftServer.Companion.globalEventHandler
import net.minestom.server.utils.inventory.PlayerInventoryUtils.convertToPacketSlot
import net.minestom.server.MinecraftServer.Companion.init
import net.minestom.server.utils.inventory.PlayerInventoryUtils.convertPlayerInventorySlot
import net.minestom.server.utils.chunk.ChunkUtils.getChunkIndex
import net.minestom.server.utils.chunk.ChunkUtils.getChunkCoordX
import net.minestom.server.utils.chunk.ChunkUtils.getChunkCoordZ
import net.minestom.server.utils.chunk.ChunkUtils.getChunkCoordinate
import net.minestom.server.utils.chunk.ChunkUtils.toSectionRelativeCoordinate
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.setString
import net.minestom.server.permission.PermissionHandler.hasPermission
import net.minestom.server.permission.PermissionHandler.addPermission
import net.minestom.server.ServerProcess.advancement
import net.minestom.server.advancements.AdvancementManager.createTab
import net.minestom.server.advancements.AdvancementTab.addViewer
import net.minestom.server.advancements.AdvancementTab.getViewers
import net.minestom.server.advancements.AdvancementTab.Companion.getTabs
import net.minestom.server.advancements.AdvancementTab.removeViewer
import net.minestom.server.ServerProcess.start
import net.minestom.server.ServerProcess.stop
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.clear
import net.minestom.server.ServerProcess
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.ChunkGenerator
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.instance.ChunkPopulator
import net.minestom.server.api.EnvImpl.FlexibleListenerImpl
import java.util.concurrent.CopyOnWriteArrayList
import net.minestom.server.api.EnvImpl.EventCollector
import net.minestom.server.event.GlobalEventHandler
import java.lang.ref.WeakReference
import java.lang.InterruptedException
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTException
import java.lang.StringBuilder
import java.lang.Void
import java.util.concurrent.CompletableFuture
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.api.TestConnectionImpl.PlayerConnectionImpl
import net.minestom.server.api.TestConnectionImpl.IncomingCollector
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.network.packet.server.SendablePacket
import java.net.InetSocketAddress
import net.minestom.server.MinecraftServer
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound
import net.minestom.server.tag.TagHandler
import org.jglrxavpok.hephaistos.nbt.NBT
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import org.jglrxavpok.hephaistos.nbt.NBTInt
import java.lang.IllegalStateException
import net.minestom.server.tag.TagSerializer
import net.minestom.server.tag.TagReadable
import net.minestom.server.tag.TagWritable
import net.minestom.server.tag.TagViewTest
import net.minestom.server.tag.TagStructureTest
import net.minestom.server.item.ItemTest
import net.minestom.server.item.Enchantment
import net.minestom.server.item.ItemMetaBuilder
import java.util.function.UnaryOperator
import org.jglrxavpok.hephaistos.nbt.NBTList
import org.jglrxavpok.hephaistos.nbt.NBTString
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minestom.server.item.ItemStackBuilder
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.ItemHideFlag
import net.minestom.server.event.trait.CancellableEvent
import net.minestom.server.event.trait.RecursiveEvent
import net.minestom.server.event.EventNodeTest.Recursive1
import net.minestom.server.event.trait.EntityEvent
import net.minestom.server.event.EventNode
import java.util.concurrent.atomic.AtomicBoolean
import net.minestom.server.event.EventNodeTest.EventTest
import net.minestom.server.event.EventNodeTest.CancellableTest
import net.minestom.server.event.EventNodeTest.Recursive2
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiPredicate
import net.minestom.server.event.EventNodeTest.ItemTestEvent
import net.minestom.server.event.EventBinding
import java.util.function.BiConsumer
import net.minestom.server.event.EventNodeImpl
import net.minestom.server.event.EventNodeTest.EntityTestEvent
import net.minestom.server.event.trait.PlayerEvent
import java.lang.Runnable
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.collection.IndexMap
import net.minestom.server.utils.collection.ObjectArray
import net.minestom.server.utils.NamespaceID
import java.lang.AssertionError
import net.minestom.server.utils.mojang.MojangUtils
import net.minestom.server.network.packet.server.play.JoinGamePacket
import net.minestom.server.network.packet.server.play.ServerDifficultyPacket
import net.minestom.server.network.packet.server.play.SpawnPositionPacket
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket
import net.minestom.server.network.packet.server.play.EntityPropertiesPacket
import net.minestom.server.network.packet.server.play.EntityStatusPacket
import net.minestom.server.network.packet.server.play.UpdateHealthPacket
import net.minestom.server.network.packet.server.play.PlayerAbilitiesPacket
import net.minestom.server.world.DimensionType
import net.minestom.server.network.packet.server.play.SetExperiencePacket
import java.lang.IllegalAccessException
import net.minestom.server.network.packet.server.play.SpawnLivingEntityPacket
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket
import net.minestom.server.entity.EntityRemovalIntegrationTest.TestEntity
import net.minestom.server.event.entity.EntityTickEvent
import java.time.temporal.TemporalUnit
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket
import net.minestom.server.network.packet.server.play.EntityTeleportPacket
import java.util.concurrent.atomic.AtomicReference
import net.minestom.server.thread.TickThread
import net.minestom.server.thread.ThreadDispatcher
import net.minestom.server.thread.ThreadProvider
import net.minestom.server.thread.MinestomThread
import net.minestom.server.Tickable
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.IntFunction
import net.minestom.server.thread.ThreadProvider.RefreshType
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.command.builder.CommandDispatcher
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.condition.CommandCondition
import java.io.IOException
import net.minestom.server.network.PacketProcessor
import java.net.UnixDomainSocketAddress
import java.util.zip.DataFormatException
import net.minestom.server.network.packet.client.play.ClientPluginMessagePacket
import net.minestom.server.utils.binary.PooledBuffers
import net.minestom.server.utils.PacketUtils
import net.minestom.server.utils.binary.BinaryBuffer
import net.minestom.server.utils.binary.BinaryReader
import net.minestom.server.network.packet.server.play.ChatMessagePacket
import net.minestom.server.message.ChatPosition
import net.minestom.server.network.packet.server.LazyPacket
import net.minestom.server.network.packet.server.CachedPacket
import net.minestom.server.network.packet.server.FramedPacket
import net.minestom.server.network.PacketWriteReadTest
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.coordinate.Vec
import net.minestom.server.network.packet.server.handshake.ResponsePacket
import net.minestom.server.network.packet.server.status.PongPacket
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket
import net.minestom.server.network.packet.server.login.LoginSuccessPacket
import net.minestom.server.network.packet.server.login.SetCompressionPacket
import net.minestom.server.network.packet.server.play.AcknowledgePlayerDiggingPacket
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket
import net.minestom.server.network.packet.server.play.ActionBarPacket
import net.minestom.server.network.packet.server.play.AttachEntityPacket
import net.minestom.server.network.packet.server.play.BlockActionPacket
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket
import net.minestom.server.network.packet.server.play.BlockChangePacket
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket
import net.minestom.server.network.packet.server.play.BossBarPacket
import net.minestom.server.network.packet.server.play.BossBarPacket.AddAction
import net.kyori.adventure.bossbar.BossBar
import net.minestom.server.network.packet.server.play.BossBarPacket.RemoveAction
import net.minestom.server.network.packet.server.play.BossBarPacket.UpdateHealthAction
import net.minestom.server.network.packet.server.play.BossBarPacket.UpdateTitleAction
import net.minestom.server.network.packet.server.play.BossBarPacket.UpdateStyleAction
import net.minestom.server.network.packet.server.play.BossBarPacket.UpdateFlagsAction
import net.minestom.server.network.packet.server.play.CameraPacket
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket
import net.minestom.server.network.packet.server.play.ClearTitlesPacket
import net.minestom.server.network.packet.server.play.CloseWindowPacket
import net.minestom.server.network.packet.server.play.CollectItemPacket
import net.minestom.server.network.packet.server.play.CraftRecipeResponse
import net.minestom.server.network.packet.server.play.DeathCombatEventPacket
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredShapelessCraftingRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.Ingredient
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredShapedCraftingRecipe
import net.minestom.server.network.packet.server.play.DisconnectPacket
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket
import net.minestom.server.network.packet.server.play.EffectPacket
import net.minestom.server.network.packet.server.play.EndCombatEventPacket
import net.minestom.server.network.packet.server.play.EnterCombatEventPacket
import net.minestom.server.network.packet.server.play.EntityAnimationPacket
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket
import net.minestom.server.network.packet.server.play.EntityPositionAndRotationPacket
import net.minestom.server.network.packet.server.play.EntityPositionPacket
import net.minestom.server.attribute.AttributeInstance
import net.minestom.server.network.packet.server.play.EntityRotationPacket
import net.minestom.server.network.packet.server.play.PlayerInfoPacket
import net.minestom.server.network.packet.server.play.PlayerInfoPacket.UpdateDisplayName
import net.minestom.server.network.packet.server.play.PlayerInfoPacket.UpdateGameMode
import net.minestom.server.network.packet.server.play.PlayerInfoPacket.UpdateLatency
import net.minestom.server.network.packet.server.play.PlayerInfoPacket.AddPlayer
import net.minestom.server.network.packet.server.play.PlayerInfoPacket.RemovePlayer
import net.minestom.server.network.packet.client.handshake.HandshakePacket
import net.minestom.server.utils.binary.Writeable
import net.minestom.server.utils.binary.BinaryWriter
import java.lang.reflect.InvocationTargetException
import net.minestom.server.instance.palette.PaletteTest
import java.lang.IllegalArgumentException
import net.minestom.server.instance.palette.Palette.EntrySupplier
import net.minestom.server.instance.palette.Palette.EntryConsumer
import net.minestom.server.instance.palette.AdaptivePalette
import net.minestom.server.instance.EntityTracker
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.instance.InstanceManager
import net.minestom.server.utils.chunk.ChunkUtils
import net.minestom.server.network.packet.server.play.ChunkDataPacket
import java.lang.RuntimeException
import net.minestom.server.instance.SharedInstance
import net.minestom.server.event.player.PlayerTickEvent
import net.minestom.server.snapshot.ServerSnapshot
import net.minestom.server.snapshot.InstanceSnapshot
import net.minestom.server.snapshot.ChunkSnapshot
import net.minestom.server.snapshot.EntitySnapshot
import net.minestom.server.collision.PhysicsResult
import net.minestom.server.collision.CollisionUtils
import net.minestom.server.collision.EntityBlockPhysicsIntegrationTest
import net.minestom.server.entity.metadata.other.SlimeMeta
import net.minestom.server.collision.BoundingBox
import net.minestom.server.collision.SweepResult
import net.minestom.server.collision.BlockCollision
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.utils.inventory.PlayerInventoryUtils
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket.ChangedSlot
import net.minestom.server.inventory.TransactionOption
import org.jglrxavpok.hephaistos.nbt.CompoundBuilder
import net.minestom.server.permission.PermissionVerifier
import net.minestom.server.advancements.AdvancementRoot
import net.minestom.server.advancements.FrameType
import net.minestom.server.advancements.AdvancementTab
import net.minestom.server.api.*
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.*
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.debug.DebugUtils
import net.minestom.server.item.ItemMeta
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import regressions.ItemMetaBuilderRegressions.BasicMetaBuilder
import java.util.Map

@EnvTest
class EntityBlockPhysicsIntegrationTest {
    @Test
    fun entityPhysicsCheckCollision(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 43, 1, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 42, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, 0, 10))
        assertEqualsPoint(Pos(0, 42, 0.7), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckSlab(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 42, 0, Block.STONE_SLAB)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 44, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, -10, 0))
        assertEqualsPoint(Pos(0, 42.5, 0), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckDiagonal(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 43, 1, Block.STONE)
        instance.setBlock(1, 43, 2, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 42, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(10, 0, 10))
        val isFirst = checkPoints(Pos(10, 42, 0.7), res.newPosition())
        val isSecond = checkPoints(Pos(0.7, 42, 10), res.newPosition())

        // First and second are both valid, it depends on the implementation
        // If x collision is checked first then isFirst will be true
        // If z collision is checked first then isSecond will be true
        Assertions.assertTrue(isFirst || isSecond)
    }

    @Test
    fun entityPhysicsCheckDirectSlide(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 43, 1, Block.STONE)
        instance.setBlock(1, 43, 2, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.69, 42, 0.69)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(10, 0, 11))
        assertEqualsPoint(Pos(0.7, 42, 11.69), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckCorner(env: Env) {
        val instance = env.createFlatInstance()
        for (i in -2..2) for (j in -2..2) instance.loadChunk(i, j).join()
        val entity = Entity(EntityType.ZOMBIE)
        instance.setBlock(5, 43, -5, Block.STONE)
        entity.setInstance(instance, Pos(-0.3, 42, -0.3)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(10, 0, -10))
        assertEqualsPoint(Pos(4.7, 42, -10.3), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckEnclosedHit(env: Env) {
        val instance = env.createFlatInstance()
        for (i in -2..2) for (j in -2..2) instance.loadChunk(i, j).join()
        instance.setBlock(8, 42, 8, Block.STONE)
        val entity = Entity(EntityType.SLIME)
        val meta = entity.entityMeta as SlimeMeta
        meta.size = 20
        entity.setInstance(instance, Pos(5, 50, 5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, -20, 0))
        assertEqualsPoint(Pos(5, 43, 5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckEnclosedHitSubBlock(env: Env) {
        val instance = env.createFlatInstance()
        for (i in -2..2) for (j in -2..2) instance.loadChunk(i, j).join()
        instance.setBlock(8, 42, 8, Block.LANTERN)
        val entity = Entity(EntityType.SLIME)
        val meta = entity.entityMeta as SlimeMeta
        meta.size = 20
        entity.setInstance(instance, Pos(5, 42.8, 5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, -0.4, 0))
        assertEqualsPoint(Pos(5, 42.56, 5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckEnclosedMiss(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(11, 43, 11, Block.STONE)
        val entity = Entity(EntityType.SLIME)
        val meta = entity.entityMeta as SlimeMeta
        meta.size = 5
        entity.setInstance(instance, Pos(5, 44, 5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, -2, 0))
        assertEqualsPoint(Pos(5, 42, 5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckEntityHit(env: Env?) {
        val z1: Point = Pos(0, 0, 0)
        val z2: Point = Pos(15, 0, 0)
        val z3: Point = Pos(11, 0, 0)
        val movement: Point = Pos(20, 1, 0)
        val bb = Entity(EntityType.ZOMBIE).boundingBox
        val sweepResultFinal = SweepResult(1, 0, 0, 0, null)
        bb.intersectBoxSwept(z1, movement, z2, bb, sweepResultFinal)
        bb.intersectBoxSwept(z1, movement, z3, bb, sweepResultFinal)
        Assertions.assertEquals(Pos(11, 0, 0), sweepResultFinal.collidedShapePosition)
        Assertions.assertEquals(sweepResultFinal.collidedShape, bb)
    }

    @Test
    fun entityPhysicsCheckEdgeClip(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 43, 1, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 42, 0.7)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(10, 0, 0))
        assertEqualsPoint(Pos(0.7, 42, 0.7), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckEdgeClipSmall(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 42, 1, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.6999, 42, 0.6999)).join()
        val res = CollisionUtils.handlePhysics(entity, Vec(0.702, 0, 0.702))
        val isFirst = checkPoints(Pos(1.402, 42, 0.7), res.newPosition())
        val isSecond = checkPoints(Pos(0.7, 42, 1.402), res.newPosition())

        // First and second are both valid, it depends on the implementation
        // If x collision is checked first then isFirst will be true
        // If z collision is checked first then isSecond will be true
        Assertions.assertTrue(isFirst || isSecond)
    }

    @Test
    fun entityPhysicsCheckDoorSubBlockNorth(env: Env) {
        val instance = env.createFlatInstance()
        val b: Block = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "north", "open", "true"))
        instance.setBlock(0, 42, 0, b)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.5, 42.5, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, 0, 0.4))
        assertEqualsPoint(Pos(0.5, 42.5, 0.512), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckDoorSubBlockSouth(env: Env) {
        val instance = env.createFlatInstance()
        val b: Block = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "south", "open", "true"))
        instance.setBlock(0, 42, 0, b)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.5, 42.5, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, 0, -0.4))
        assertEqualsPoint(Pos(0.5, 42.5, 0.487), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckDoorSubBlockWest(env: Env) {
        val instance = env.createFlatInstance()
        val b: Block = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "west", "open", "true"))
        instance.setBlock(0, 42, 0, b)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.5, 42.5, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.6, 0, 0))
        assertEqualsPoint(Pos(0.512, 42.5, 0.5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckDoorSubBlockEast(env: Env) {
        val instance = env.createFlatInstance()
        val b: Block = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "east", "open", "true"))
        instance.setBlock(0, 42, 0, b)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.5, 42.5, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(-0.6, 0, 0))
        assertEqualsPoint(Pos(0.487, 42.5, 0.5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckDoorSubBlockUp(env: Env) {
        val instance = env.createFlatInstance()
        val b: Block = Block.ACACIA_TRAPDOOR.withProperties(Map.of("half", "top"))
        instance.setBlock(0, 44, 0, b)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.5, 42.7, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, 0.4, 0))
        assertEqualsPoint(Pos(0.5, 42.862, 0.5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckDoorSubBlockDown(env: Env) {
        val instance = env.createFlatInstance()
        val b: Block = Block.ACACIA_TRAPDOOR
        instance.setBlock(0, 42, 0, b)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.5, 42.2, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, -0.4, 0))
        assertEqualsPoint(Pos(0.5, 42.187, 0.5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckOnGround(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 40, 0, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 50, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, -20, 0))
        Assertions.assertTrue(res.isOnGround)
    }

    @Test
    fun entityPhysicsCheckStairTop(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 42, 0, Block.ACACIA_STAIRS)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.4, 42.5, 0.9)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, 0, -1.2))
        assertEqualsPoint(Pos(0.4, 42.5, 0.8), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckStairTopSmall(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 42, 0, Block.ACACIA_STAIRS)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.4, 42.5, 0.9)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, 0, -0.2))
        assertEqualsPoint(Pos(0.4, 42.5, 0.8), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckNotOnGround(env: Env) {
        val instance = env.createFlatInstance()
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 50, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, -1, 0))
        Assertions.assertFalse(res.isOnGround)
    }

    @Test
    fun entityPhysicsCheckNotOnGroundHitUp(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 60, 0, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 50, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, 20, 0))
        Assertions.assertFalse(res.isOnGround)
    }

    @Test
    fun entityPhysicsCheckSlide(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 43, 1, Block.STONE)
        instance.setBlock(1, 43, 2, Block.STONE)
        instance.setBlock(1, 43, 3, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 42, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(11, 0, 10))
        assertEqualsPoint(Pos(11, 42, 0.7), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveCollide(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 43, 1, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.6, 42, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.3, 0, 0))
        assertEqualsPoint(Pos(0.7, 42, 0), res.newPosition())
    }

    // Checks C include all checks for crossing one intermediate block (3 block checks)
    @Test
    fun entityPhysicsSmallMoveC0(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 42, 0, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(0.7, 42, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.6, 0, 0.6))
        assertEqualsPoint(Pos(1, 42, 1.1), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC1(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 42, 1, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(0.5, 42, 0.7)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.6, 0, 0.6))
        assertEqualsPoint(Pos(1.1, 42, 1), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC2(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 42, 1, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(0.8, 42, 1.3)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.6, 0, -0.6))
        assertEqualsPoint(Pos(1, 42, 0.7), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC3(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 42, 0, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(0.7, 42, 1.1)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.6, 0, -0.6))
        assertEqualsPoint(Pos(1.3, 42, 1), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC4(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 42, 1, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(1.1, 42, 1.3)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(-0.6, 0, -0.6))
        assertEqualsPoint(Pos(1, 42, 0.7), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC5(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 42, 0, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(1.3, 42, 1.1)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(-0.6, 0, -0.6))
        assertEqualsPoint(Pos(0.7, 42, 1), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC6(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 42, 0, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(1.1, 42, 0.7)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(-0.6, 0, 0.6))
        assertEqualsPoint(Pos(1, 42, 1.3), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC7(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 42, 1, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(1.3, 42, 0.8)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(-0.6, 0, 0.6))
        assertEqualsPoint(Pos(0.7, 42, 1), res.newPosition())
    }

    // Checks CE include checks for crossing two intermediate block (4 block checks)
    @Test
    fun entityPhysicsSmallMoveC0E(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 43, 0, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(0.51, 42.51, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.57, 0.57, 0.57))
        assertEqualsPoint(Pos(1.08, 43, 1.07), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC1E(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 43, 1, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(0.50, 42.51, 0.51)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.57, 0.57, 0.57))
        assertEqualsPoint(Pos(1.07, 43, 1.08), res.newPosition())
    }

    @Test
    fun entityPhysicsSmallMoveC2E(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(1, 43, 1, Block.STONE)
        val bb = BoundingBox(0, 0, 0)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setBoundingBox(bb)
        entity.setInstance(instance, Pos(0.51, 42.50, 0.51)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0.57, 0.57, 0.57))
        assertEqualsPoint(Pos(1.08, 43, 1.08), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckNoCollision(env: Env) {
        val instance = env.createFlatInstance()
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 42, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(0, 0, 10))
        assertEqualsPoint(Pos(0, 42, 10), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckBlockMiss(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 43, 2, Block.STONE)
        instance.setBlock(2, 43, 0, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0, 42, 0)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(10, 0, 10))
        assertEqualsPoint(Pos(10, 42, 10), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckBlockDirections(env: Env) {
        val instance = env.createFlatInstance()
        instance.setBlock(0, 43, 1, Block.STONE)
        instance.setBlock(1, 43, 0, Block.STONE)
        instance.setBlock(0, 43, -1, Block.STONE)
        instance.setBlock(-1, 43, 0, Block.STONE)
        instance.setBlock(0, 41, 0, Block.STONE)
        instance.setBlock(0, 44, 0, Block.STONE)
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(0.5, 42, 0.5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val px = CollisionUtils.handlePhysics(entity, Vec(10, 0, 0))
        val py = CollisionUtils.handlePhysics(entity, Vec(0, 10, 0))
        val pz = CollisionUtils.handlePhysics(entity, Vec(0, 0, 10))
        val nx = CollisionUtils.handlePhysics(entity, Vec(-10, 0, 0))
        val ny = CollisionUtils.handlePhysics(entity, Vec(0, -10, 0))
        val nz = CollisionUtils.handlePhysics(entity, Vec(0, 0, -10))
        assertEqualsPoint(Pos(0.7, 42, 0.5), px.newPosition())
        assertEqualsPoint(Pos(0.5, 42.04, 0.5), py.newPosition())
        assertEqualsPoint(Pos(0.5, 42, 0.7), pz.newPosition())
        assertEqualsPoint(Pos(0.3, 42, 0.5), nx.newPosition())
        assertEqualsPoint(Pos(0.5, 42, 0.5), ny.newPosition())
        assertEqualsPoint(Pos(0.5, 42, 0.3), nz.newPosition())
    }

    @Test
    fun entityPhysicsCheckLargeVelocityMiss(env: Env) {
        val instance = env.createFlatInstance()
        val entity = Entity(EntityType.ZOMBIE)
        val distance = 20
        for (x in 0 until distance) instance.loadChunk(x, 0).join()
        entity.setInstance(instance, Pos(5, 42, 5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(((distance - 1) * 16).toDouble(), 0, 0))
        assertEqualsPoint(Pos((distance - 1) * 16 + 5, 42, 5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckLargeVelocityHit(env: Env) {
        val instance = env.createFlatInstance()
        val entity = Entity(EntityType.ZOMBIE)
        val distance = 20
        for (x in 0 until distance) instance.loadChunk(x, 0).join()
        instance.setBlock(distance * 8, 43, 5, Block.STONE)
        entity.setInstance(instance, Pos(5, 42, 5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec(((distance - 1) * 16).toDouble(), 0, 0))
        assertEqualsPoint(Pos(distance * 8 - 0.3, 42, 5), res.newPosition())
    }

    @Test
    fun entityPhysicsCheckNoMove(env: Env) {
        val instance = env.createFlatInstance()
        val entity = Entity(EntityType.ZOMBIE)
        entity.setInstance(instance, Pos(5, 42, 5)).join()
        Assertions.assertEquals(instance, entity.instance)
        val res = CollisionUtils.handlePhysics(entity, Vec.ZERO)
        assertEqualsPoint(Pos(5, 42, 5), res.newPosition())
    }

    companion object {
        private val PRECISION: Point = Pos(0.01, 0.01, 0.01)
        private fun checkPoints(expected: Point, actual: Point): Boolean {
            val diff = expected.sub(actual)
            return (PRECISION.x() > Math.abs(diff.x())
                    && PRECISION.y() > Math.abs(diff.y())
                    && PRECISION.z() > Math.abs(diff.z()))
        }

        private fun assertEqualsPoint(expected: Point, actual: Point) {
            Assertions.assertEquals(expected.x(), actual.x(), PRECISION.x())
            Assertions.assertEquals(expected.y(), actual.y(), PRECISION.y())
            Assertions.assertEquals(expected.z(), actual.z(), PRECISION.z())
        }
    }
}