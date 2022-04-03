package net.minestom.server.listener.manager

import net.minestom.server.MinecraftServer.Companion.exceptionManager
import net.minestom.server.ServerProcess
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.listener.manager.PacketListenerConsumer
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket
import net.minestom.server.network.packet.client.play.ClientPongPacket
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket
import net.minestom.server.network.packet.client.play.ClientVehicleMovePacket
import net.minestom.server.network.packet.client.play.ClientSteerBoatPacket
import net.minestom.server.network.packet.client.play.ClientPlayerPacket
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket
import net.minestom.server.network.packet.client.play.ClientTeleportConfirmPacket
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket
import net.minestom.server.network.packet.client.play.ClientAnimationPacket
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket
import net.minestom.server.network.packet.client.play.ClientUseItemPacket
import net.minestom.server.network.packet.client.play.ClientStatusPacket
import net.minestom.server.network.packet.client.play.ClientSettingsPacket
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket
import net.minestom.server.network.packet.client.play.ClientCraftRecipeRequest
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket
import net.minestom.server.network.packet.client.play.ClientPluginMessagePacket
import net.minestom.server.network.packet.client.play.ClientPlayerAbilitiesPacket
import net.minestom.server.network.packet.client.play.ClientResourcePackStatusPacket
import net.minestom.server.network.packet.client.play.ClientAdvancementTabPacket
import net.minestom.server.network.packet.client.play.ClientSpectatePacket
import net.minestom.server.listener.manager.PacketListenerManager
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.listener.*
import org.slf4j.LoggerFactory
import java.lang.Exception

class PacketListenerManager(private val serverProcess: ServerProcess) {
    private val listeners: MutableMap<Class<out ClientPacket>, PacketListenerConsumer<*>> = ConcurrentHashMap()

    init {
        setListener(
            ClientKeepAlivePacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> KeepAliveListener.listener(packet, player) })
        setListener(
            ClientChatMessagePacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> ChatMessageListener.listener(packet, player) })
        setListener(
            ClientClickWindowPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                WindowListener.clickWindowListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientCloseWindowPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                WindowListener.closeWindowListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientPongPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> WindowListener.pong(packet, player) })
        setListener(
            ClientEntityActionPacket::class.java,
            PacketListenerConsumer { obj: T?, packet: Player? -> obj.listener(packet) })
        setListener(
            ClientHeldItemChangePacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> PlayerHeldListener.heldListener(packet, player) })
        setListener(
            ClientPlayerBlockPlacementPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> BlockPlacementListener.listener(packet, player) })
        setListener(
            ClientSteerVehiclePacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerVehicleListener.steerVehicleListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientVehicleMovePacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerVehicleListener.vehicleMoveListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientSteerBoatPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerVehicleListener.boatSteerListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientPlayerPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerPositionListener.playerPacketListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientPlayerRotationPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerPositionListener.playerLookListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientPlayerPositionPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerPositionListener.playerPositionListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientPlayerPositionAndRotationPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerPositionListener.playerPositionAndLookListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientTeleportConfirmPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerPositionListener.teleportConfirmListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientPlayerDiggingPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                PlayerDiggingListener.playerDiggingListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientAnimationPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                AnimationListener.animationListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientInteractEntityPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                UseEntityListener.useEntityListener(
                    packet,
                    player
                )
            })
        setListener(
            ClientUseItemPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> UseItemListener.useItemListener(packet, player) })
        setListener(
            ClientStatusPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> StatusListener.listener(packet, player) })
        setListener(
            ClientSettingsPacket::class.java,
            PacketListenerConsumer { obj: T?, packet: Player? -> obj.listener(packet) })
        setListener(
            ClientCreativeInventoryActionPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? ->
                CreativeInventoryActionListener.listener(
                    packet,
                    player
                )
            })
        setListener(
            ClientCraftRecipeRequest::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> RecipeListener.listener(packet, player) })
        setListener(
            ClientTabCompletePacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> TabCompleteListener.listener(packet, player) })
        setListener(
            ClientPluginMessagePacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> PluginMessageListener.listener(packet, player) })
        setListener(
            ClientPlayerAbilitiesPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> AbilitiesListener.listener(packet, player) })
        setListener(
            ClientResourcePackStatusPacket::class.java,
            PacketListenerConsumer { obj: T?, packet: Player? -> obj.listener(packet) })
        setListener(
            ClientAdvancementTabPacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> AdvancementTabListener.listener(packet, player) })
        setListener(
            ClientSpectatePacket::class.java,
            PacketListenerConsumer { packet: T?, player: Player? -> SpectateListener.listener(packet, player) })
    }

    /**
     * Processes a packet by getting its [PacketListenerConsumer] and calling all the packet listeners.
     *
     * @param packet the received packet
     * @param player the player who sent the packet
     * @param <T>    the packet type
    </T> */
    fun <T : ClientPacket?> processClientPacket(packet: T, player: Player) {
        val clazz: Class<*> = packet.javaClass
        val packetListenerConsumer: PacketListenerConsumer<T>? = listeners.get(clazz)

        // Listener can be null if none has been set before, call PacketConsumer anyway
        if (packetListenerConsumer == null) {
            LOGGER.warn("Packet $clazz does not have any default listener! (The issue comes from Minestom)")
        }

        // Event
        val playerPacketEvent = PlayerPacketEvent(player, packet)
        EventDispatcher.call(playerPacketEvent)
        if (playerPacketEvent.isCancelled) {
            return
        }

        // Finally execute the listener
        if (packetListenerConsumer != null) {
            try {
                packetListenerConsumer.accept(packet, player)
            } catch (e: Exception) {
                // Packet is likely invalid
                exceptionManager.handleException(e)
            }
        }
    }

    /**
     * Sets the listener of a packet.
     *
     *
     * WARNING: this will overwrite the default minestom listener, this is not reversible.
     *
     * @param packetClass the class of the packet
     * @param consumer    the new packet's listener
     * @param <T>         the type of the packet
    </T> */
    fun <T : ClientPacket?> setListener(packetClass: Class<T>, consumer: PacketListenerConsumer<T>) {
        listeners[packetClass] = consumer
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(PacketListenerManager::class.java)
    }
}