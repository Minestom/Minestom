package net.minestom.server.instance.palette

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
import net.minestom.server.api.TestConnection
import net.minestom.server.api.FlexibleListener
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.ChunkGenerator
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.instance.ChunkPopulator
import net.minestom.server.api.EnvImpl.FlexibleListenerImpl
import java.util.concurrent.CopyOnWriteArrayList
import net.minestom.server.api.TestConnectionImpl
import net.minestom.server.api.EnvImpl.EventCollector
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.api.EnvParameterResolver
import net.minestom.server.api.EnvBefore
import net.minestom.server.api.EnvCleaner
import java.lang.ref.WeakReference
import java.lang.InterruptedException
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTException
import java.lang.StringBuilder
import java.lang.Void
import net.minestom.server.api.EnvImpl
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
import net.minestom.server.entity.EntityType
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
import net.minestom.server.api.EnvTest
import net.minestom.server.entity.GameMode
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
import net.minestom.server.entity.EntityTypes
import net.minestom.server.entity.EntityTypeImpl
import net.minestom.server.entity.PlayerSkin
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
import net.minestom.server.entity.EquipmentSlot
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
import net.minestom.server.entity.EntityProjectile
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent
import net.minestom.server.entity.LivingEntity
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
import net.minestom.server.coordinate.Point
import net.minestom.server.utils.debug.DebugUtils
import net.minestom.server.item.ItemMeta
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import regressions.ItemMetaBuilderRegressions.BasicMetaBuilder
import java.lang.Exception
import java.util.HashSet

class PaletteTest {
    @Test
    fun singlePlacement() {
        val palette = Palette.blocks()
        palette[0, 0, 1] = 1
        Assertions.assertEquals(1, palette[0, 0, 1])
    }

    @Test
    fun placement() {
        val palettes = testPalettes()
        for (palette in palettes) {
            val dimension = palette.dimension()
            Assertions.assertEquals(0, palette[0, 0, 0], "Default value should be 0")
            Assertions.assertEquals(0, palette.count())
            palette[0, 0, 0] = 64
            Assertions.assertEquals(64, palette[0, 0, 0])
            Assertions.assertEquals(64, palette[dimension, 0, 0], "Coordinate must be rounded to the palette dimension")
            Assertions.assertEquals(1, palette.count())
            palette[1, 0, 0] = 65
            Assertions.assertEquals(64, palette[0, 0, 0])
            Assertions.assertEquals(65, palette[1, 0, 0])
            Assertions.assertEquals(2, palette.count())
            palette[0, 1, 0] = 66
            Assertions.assertEquals(64, palette[0, 0, 0])
            Assertions.assertEquals(65, palette[1, 0, 0])
            Assertions.assertEquals(66, palette[0, 1, 0])
            Assertions.assertEquals(3, palette.count())
            palette[0, 0, 1] = 67
            Assertions.assertEquals(64, palette[0, 0, 0])
            Assertions.assertEquals(65, palette[1, 0, 0])
            Assertions.assertEquals(66, palette[0, 1, 0])
            Assertions.assertEquals(67, palette[0, 0, 1])
            Assertions.assertEquals(4, palette.count())
            palette[0, 0, 1] = 68
            Assertions.assertEquals(4, palette.count())
        }
    }

    @Test
    fun negPlacement() {
        val palettes = testPalettes()
        for (palette in palettes) {
            Assertions.assertThrows(IllegalArgumentException::class.java) { palette[-1, 0, 0] = 64 }
            Assertions.assertThrows(IllegalArgumentException::class.java) { palette[0, -1, 0] = 64 }
            Assertions.assertThrows(IllegalArgumentException::class.java) { palette[0, 0, -1] = 64 }
            Assertions.assertThrows(IllegalArgumentException::class.java) { palette[-1, 0, 0] }
            Assertions.assertThrows(IllegalArgumentException::class.java) { palette[0, -1, 0] }
            Assertions.assertThrows(IllegalArgumentException::class.java) { palette[0, 0, -1] }
        }
    }

    @Test
    fun resize() {
        val palette = Palette.newPalette(16, 5, 2)
        palette[0, 0, 0] = 1
        Assertions.assertEquals(2, palette.bitsPerEntry())
        palette[0, 0, 1] = 2
        Assertions.assertEquals(2, palette.bitsPerEntry())
        palette[0, 0, 2] = 3
        Assertions.assertEquals(2, palette.bitsPerEntry())
        palette[0, 0, 3] = 4
        Assertions.assertEquals(3, palette.bitsPerEntry())
        Assertions.assertEquals(1, palette[0, 0, 0])
        Assertions.assertEquals(2, palette[0, 0, 1])
        Assertions.assertEquals(3, palette[0, 0, 2])
        Assertions.assertEquals(4, palette[0, 0, 3])
    }

    @Test
    fun fill() {
        val palettes = testPalettes()
        for (palette in palettes) {
            Assertions.assertEquals(0, palette.count())
            palette[0, 0, 0] = 5
            Assertions.assertEquals(1, palette.count())
            Assertions.assertEquals(5, palette[0, 0, 0])
            palette.fill(6)
            Assertions.assertEquals(6, palette[0, 0, 0])
            Assertions.assertEquals(palette.maxSize(), palette.count())
            for (x in 0 until palette.dimension()) {
                for (y in 0 until palette.dimension()) {
                    for (z in 0 until palette.dimension()) {
                        Assertions.assertEquals(6, palette[x, y, z])
                    }
                }
            }
            palette.fill(0)
            Assertions.assertEquals(0, palette.count())
            for (x in 0 until palette.dimension()) {
                for (y in 0 until palette.dimension()) {
                    for (z in 0 until palette.dimension()) {
                        Assertions.assertEquals(0, palette[x, y, z])
                    }
                }
            }
        }
    }

    @Test
    fun bulk() {
        val palettes = testPalettes()
        for (palette in palettes) {
            val dimension = palette.dimension()
            // Place
            for (x in 0 until dimension) {
                for (y in 0 until dimension) {
                    for (z in 0 until dimension) {
                        palette[x, y, z] = x + y + z + 1
                    }
                }
            }
            Assertions.assertEquals(palette.maxSize(), palette.count())
            // Verify
            for (x in 0 until dimension) {
                for (y in 0 until dimension) {
                    for (z in 0 until dimension) {
                        Assertions.assertEquals(x + y + z + 1, palette[x, y, z])
                    }
                }
            }
        }
    }

    @Test
    fun bulkAll() {
        val palettes = testPalettes()
        for (palette in palettes) {
            // Fill all entries
            palette.setAll { x: Int, y: Int, z: Int -> x + y + z + 1 }
            palette.getAll { x: Int, y: Int, z: Int, value: Int ->
                Assertions.assertEquals(
                    x + y + z + 1, value,
                    "x: " + x + ", y: " + y + ", z: " + z + ", dimension: " + palette.dimension()
                )
            }

            // Replacing
            palette.replaceAll { x: Int, y: Int, z: Int, value: Int ->
                Assertions.assertEquals(x + y + z + 1, value)
                x + y + z + 2
            }
            palette.getAll { x: Int, y: Int, z: Int, value: Int -> Assertions.assertEquals(x + y + z + 2, value) }
        }
    }

    @Test
    fun bulkAllOrder() {
        val palettes = testPalettes()
        for (palette in palettes) {
            val count = AtomicInteger()

            // Ensure that the lambda is called for every entry
            // even if the array is initialized
            palette.getAll { x: Int, y: Int, z: Int, value: Int -> count.incrementAndGet() }
            Assertions.assertEquals(count.get(), palette.maxSize())

            // Fill all entries
            count.set(0)
            val points: MutableSet<Point> = HashSet()
            palette.setAll { x: Int, y: Int, z: Int ->
                Assertions.assertTrue(
                    points.add(
                        Vec(
                            x.toDouble(), y.toDouble(), z.toDouble()
                        )
                    ), "Duplicate point: " + x + ", " + y + ", " + z + ", dimension " + palette.dimension()
                )
                count.incrementAndGet()
            }
            Assertions.assertEquals(palette.maxSize(), palette.count())
            Assertions.assertEquals(palette.count(), count.get())
            count.set(0)
            palette.getAll { x: Int, y: Int, z: Int, value: Int ->
                Assertions.assertEquals(
                    count.incrementAndGet(),
                    value
                )
            }
            Assertions.assertEquals(count.get(), palette.count())

            // Replacing
            count.set(0)
            palette.replaceAll { x: Int, y: Int, z: Int, value: Int ->
                Assertions.assertEquals(count.incrementAndGet(), value)
                count.get()
            }
            Assertions.assertEquals(count.get(), palette.count())
            count.set(0)
            palette.getAll { x: Int, y: Int, z: Int, value: Int ->
                Assertions.assertEquals(
                    count.incrementAndGet(),
                    value
                )
            }
        }
    }

    @Test
    fun setAllConstant() {
        val palettes = testPalettes()
        for (palette in palettes) {
            palette.setAll { x: Int, y: Int, z: Int -> 1 }
            palette.getAll { x: Int, y: Int, z: Int, value: Int -> Assertions.assertEquals(1, value) }
        }
    }

    @get:Test
    val allPresent: Unit
        get() {
            val palettes = testPalettes()
            for (palette in palettes) {
                palette.getAllPresent { x: Int, y: Int, z: Int, value: Int -> Assertions.fail<Any>("The palette should be empty") }
                palette[0, 0, 1] = 1
                palette.getAllPresent { x: Int, y: Int, z: Int, value: Int ->
                    Assertions.assertEquals(0, x)
                    Assertions.assertEquals(0, y)
                    Assertions.assertEquals(1, z)
                    Assertions.assertEquals(1, value)
                }
            }
        }

    @Test
    fun replaceAll() {
        val palettes = testPalettes()
        for (palette in palettes) {
            palette.setAll { x: Int, y: Int, z: Int -> x + y + z + 1 }
            palette.replaceAll { x: Int, y: Int, z: Int, value: Int ->
                Assertions.assertEquals(x + y + z + 1, value)
                x + y + z + 2
            }
            palette.getAll { x: Int, y: Int, z: Int, value: Int -> Assertions.assertEquals(x + y + z + 2, value) }
        }
    }

    @Test
    fun replace() {
        val palettes = testPalettes()
        for (palette in palettes) {
            palette[0, 0, 0] = 1
            palette.replace(0, 0, 0) { operand: Int ->
                Assertions.assertEquals(1, operand)
                2
            }
            Assertions.assertEquals(2, palette[0, 0, 0])
        }
    }

    @Test
    fun replaceLoop() {
        val palette = Palette.newPalette(2, 15, 4)
        palette.setAll { x: Int, y: Int, z: Int -> x + y + z }
        val dimension = palette.dimension()
        for (x in 0 until dimension) {
            for (y in 0 until dimension) {
                for (z in 0 until dimension) {
                    palette.replace(x, y, z) { value: Int -> value + 1 }
                }
            }
        }
    }

    @Test
    fun dimension() {
        Assertions.assertThrows(Exception::class.java) { Palette.newPalette(-4, 5, 3) }
        Assertions.assertThrows(Exception::class.java) { Palette.newPalette(0, 5, 3) }
        Assertions.assertThrows(Exception::class.java) { Palette.newPalette(1, 5, 3) }
        Assertions.assertDoesNotThrow<Palette> { Palette.newPalette(2, 5, 3) }
        Assertions.assertThrows(Exception::class.java) { Palette.newPalette(3, 5, 3) }
        Assertions.assertDoesNotThrow<Palette> { Palette.newPalette(4, 5, 3) }
        Assertions.assertThrows(Exception::class.java) { Palette.newPalette(6, 5, 3) }
        Assertions.assertDoesNotThrow<Palette> { Palette.newPalette(16, 5, 3) }
    }

    companion object {
        private fun testPalettes(): List<Palette> {
            return java.util.List.of(
                Palette.newPalette(2, 5, 3),
                Palette.newPalette(4, 5, 3),
                Palette.newPalette(8, 5, 3),
                Palette.newPalette(16, 5, 3)
            )
        }
    }
}