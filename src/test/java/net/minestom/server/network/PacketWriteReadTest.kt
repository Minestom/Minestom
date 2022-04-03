package net.minestom.server.network

import com.google.gson.JsonObject
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
import java.util.function.IntFunction
import net.minestom.server.thread.ThreadProvider.RefreshType
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
import net.kyori.adventure.text.Component
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
import net.minestom.server.utils.debug.DebugUtils
import net.minestom.server.item.ItemMeta
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import regressions.ItemMetaBuilderRegressions.BasicMetaBuilder
import java.lang.reflect.Constructor
import java.util.*
import java.util.List
import java.util.Map
import java.util.concurrent.*
import java.util.function.Consumer

/**
 * Ensures that packet can be written and read correctly.
 */
class PacketWriteReadTest {
    @Test
    fun serverTest() {
        SERVER_PACKETS.forEach(Consumer { writeable: ServerPacket -> testPacket(writeable) })
    }

    @Test
    fun clientTest() {
        CLIENT_PACKETS.forEach(Consumer { writeable: ClientPacket -> testPacket(writeable) })
    }

    companion object {
        private val SERVER_PACKETS: MutableList<ServerPacket> = ArrayList()
        private val CLIENT_PACKETS: MutableList<ClientPacket> = ArrayList()
        private val COMPONENT: Component = Component.text("Hey")
        private val VEC = Vec(5, 5, 5)
        @BeforeAll
        fun setupServer() {
            // Handshake
            SERVER_PACKETS.add(ResponsePacket(JsonObject().toString()))
            // Status
            SERVER_PACKETS.add(PongPacket(5))
            // Login
            //SERVER_PACKETS.add(new EncryptionRequestPacket("server", generateByteArray(16), generateByteArray(16)));
            SERVER_PACKETS.add(LoginDisconnectPacket(COMPONENT))
            //SERVER_PACKETS.add(new LoginPluginRequestPacket(5, "id", generateByteArray(16)));
            SERVER_PACKETS.add(LoginSuccessPacket(UUID.randomUUID(), "TheMode911"))
            SERVER_PACKETS.add(SetCompressionPacket(256))
            // Play
            SERVER_PACKETS.add(
                AcknowledgePlayerDiggingPacket(
                    VEC,
                    5,
                    ClientPlayerDiggingPacket.Status.STARTED_DIGGING,
                    true
                )
            )
            SERVER_PACKETS.add(ActionBarPacket(COMPONENT))
            SERVER_PACKETS.add(AttachEntityPacket(5, 10))
            SERVER_PACKETS.add(BlockActionPacket(VEC, 5.toByte(), 5.toByte(), 5))
            SERVER_PACKETS.add(BlockBreakAnimationPacket(5, VEC, 5.toByte()))
            SERVER_PACKETS.add(BlockChangePacket(VEC, 0))
            SERVER_PACKETS.add(BlockEntityDataPacket(VEC, 5, NBT.Compound(Map.of("key", NBT.String("value")))))
            SERVER_PACKETS.add(
                BossBarPacket(
                    UUID.randomUUID(),
                    AddAction(COMPONENT, 5f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS, 2.toByte())
                )
            )
            SERVER_PACKETS.add(BossBarPacket(UUID.randomUUID(), RemoveAction()))
            SERVER_PACKETS.add(BossBarPacket(UUID.randomUUID(), UpdateHealthAction(5f)))
            SERVER_PACKETS.add(BossBarPacket(UUID.randomUUID(), UpdateTitleAction(COMPONENT)))
            SERVER_PACKETS.add(
                BossBarPacket(
                    UUID.randomUUID(),
                    UpdateStyleAction(BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
                )
            )
            SERVER_PACKETS.add(BossBarPacket(UUID.randomUUID(), UpdateFlagsAction(5.toByte())))
            SERVER_PACKETS.add(CameraPacket(5))
            SERVER_PACKETS.add(ChangeGameStatePacket(ChangeGameStatePacket.Reason.RAIN_LEVEL_CHANGE, 2))
            SERVER_PACKETS.add(ChatMessagePacket(COMPONENT, ChatPosition.CHAT, UUID.randomUUID()))
            SERVER_PACKETS.add(ClearTitlesPacket(false))
            SERVER_PACKETS.add(CloseWindowPacket(2.toByte()))
            SERVER_PACKETS.add(CollectItemPacket(5, 5, 5))
            SERVER_PACKETS.add(CraftRecipeResponse(2.toByte(), "recipe"))
            SERVER_PACKETS.add(DeathCombatEventPacket(5, 5, COMPONENT))
            SERVER_PACKETS.add(
                DeclareRecipesPacket(
                    List.of<DeclaredRecipe>(
                        DeclaredShapelessCraftingRecipe(
                            "minecraft:sticks",
                            "sticks",
                            List.of(Ingredient(List.of(ItemStack.of(Material.OAK_PLANKS)))),
                            ItemStack.of(Material.STICK)
                        ),
                        DeclaredShapedCraftingRecipe(
                            "minecraft:torch",
                            1,
                            2,
                            "",
                            List.of(
                                Ingredient(List.of(ItemStack.of(Material.COAL))),
                                Ingredient(List.of(ItemStack.of(Material.STICK)))
                            ),
                            ItemStack.of(Material.TORCH)
                        )
                    )
                )
            )
            SERVER_PACKETS.add(DestroyEntitiesPacket(List.of(5, 5, 5)))
            SERVER_PACKETS.add(DisconnectPacket(COMPONENT))
            SERVER_PACKETS.add(DisplayScoreboardPacket(5.toByte(), "scoreboard"))
            SERVER_PACKETS.add(EffectPacket(5, VEC, 5, false))
            SERVER_PACKETS.add(EndCombatEventPacket(5, 5))
            SERVER_PACKETS.add(EnterCombatEventPacket())
            SERVER_PACKETS.add(EntityAnimationPacket(5, EntityAnimationPacket.Animation.TAKE_DAMAGE))
            SERVER_PACKETS.add(
                EntityEquipmentPacket(
                    6,
                    Map.of(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.DIAMOND_SWORD))
                )
            )
            SERVER_PACKETS.add(EntityHeadLookPacket(5, 90f))
            SERVER_PACKETS.add(EntityMetaDataPacket(5, List.of()))
            SERVER_PACKETS.add(
                EntityPositionAndRotationPacket(
                    5,
                    0.toShort(),
                    0.toShort(),
                    0.toShort(),
                    45f,
                    45f,
                    false
                )
            )
            SERVER_PACKETS.add(EntityPositionPacket(5, 0.toShort(), 0.toShort(), 0.toShort(), true))
            SERVER_PACKETS.add(EntityPropertiesPacket(5, List.of()))
            SERVER_PACKETS.add(EntityRotationPacket(5, 45f, 45f, false))
            SERVER_PACKETS.add(
                PlayerInfoPacket(
                    PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME,
                    UpdateDisplayName(UUID.randomUUID(), COMPONENT)
                )
            )
            SERVER_PACKETS.add(
                PlayerInfoPacket(
                    PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME,
                    UpdateDisplayName(UUID.randomUUID(), null as Component?)
                )
            )
            SERVER_PACKETS.add(
                PlayerInfoPacket(
                    PlayerInfoPacket.Action.UPDATE_GAMEMODE,
                    UpdateGameMode(UUID.randomUUID(), GameMode.CREATIVE)
                )
            )
            SERVER_PACKETS.add(
                PlayerInfoPacket(
                    PlayerInfoPacket.Action.UPDATE_LATENCY,
                    UpdateLatency(UUID.randomUUID(), 5)
                )
            )
            SERVER_PACKETS.add(
                PlayerInfoPacket(
                    PlayerInfoPacket.Action.ADD_PLAYER,
                    AddPlayer(
                        UUID.randomUUID(),
                        "TheMode911",
                        List.of(AddPlayer.Property("name", "value")),
                        GameMode.CREATIVE,
                        5,
                        COMPONENT
                    )
                )
            )
            SERVER_PACKETS.add(PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER, RemovePlayer(UUID.randomUUID())))

            //SERVER_PACKETS.add(new MultiBlockChangePacket(5,5,5,true, new long[]{0,5,543534,1321}));
        }

        @BeforeAll
        fun setupClient() {
            CLIENT_PACKETS.add(HandshakePacket(755, "localhost", 25565, 2))
        }

        private fun testPacket(writeable: Writeable) {
            try {
                val writer = BinaryWriter()
                writeable.write(writer)
                val readerConstructor: Constructor<out Writeable> = writeable.javaClass.getConstructor(
                    BinaryReader::class.java
                )
                val reader = BinaryReader(writer.toByteArray())
                val createdPacket = readerConstructor.newInstance(reader)
                Assertions.assertEquals(writeable, createdPacket)
            } catch (e: NoSuchMethodException) {
                Assertions.fail<Any>(writeable.toString(), e)
            } catch (e: InvocationTargetException) {
                Assertions.fail<Any>(writeable.toString(), e)
            } catch (e: InstantiationException) {
                Assertions.fail<Any>(writeable.toString(), e)
            } catch (e: IllegalAccessException) {
                Assertions.fail<Any>(writeable.toString(), e)
            }
        }

        private fun generateByteArray(size: Int): ByteArray {
            val array = ByteArray(size)
            ThreadLocalRandom.current().nextBytes(array)
            return array
        }
    }
}