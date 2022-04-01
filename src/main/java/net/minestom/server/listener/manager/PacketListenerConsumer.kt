package net.minestom.server.listener.manager

import net.minestom.server.MinecraftServer.Companion.exceptionManager
import net.minestom.server.ServerProcess
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.listener.manager.PacketListenerConsumer
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket
import net.minestom.server.listener.KeepAliveListener
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket
import net.minestom.server.listener.ChatMessageListener
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket
import net.minestom.server.network.packet.client.play.ClientPongPacket
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket
import net.minestom.server.listener.PlayerHeldListener
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket
import net.minestom.server.listener.BlockPlacementListener
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket
import net.minestom.server.listener.PlayerVehicleListener
import net.minestom.server.network.packet.client.play.ClientVehicleMovePacket
import net.minestom.server.network.packet.client.play.ClientSteerBoatPacket
import net.minestom.server.network.packet.client.play.ClientPlayerPacket
import net.minestom.server.listener.PlayerPositionListener
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket
import net.minestom.server.network.packet.client.play.ClientTeleportConfirmPacket
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket
import net.minestom.server.listener.PlayerDiggingListener
import net.minestom.server.network.packet.client.play.ClientAnimationPacket
import net.minestom.server.listener.AnimationListener
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket
import net.minestom.server.listener.UseEntityListener
import net.minestom.server.network.packet.client.play.ClientUseItemPacket
import net.minestom.server.listener.UseItemListener
import net.minestom.server.network.packet.client.play.ClientStatusPacket
import net.minestom.server.listener.StatusListener
import net.minestom.server.network.packet.client.play.ClientSettingsPacket
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket
import net.minestom.server.listener.CreativeInventoryActionListener
import net.minestom.server.network.packet.client.play.ClientCraftRecipeRequest
import net.minestom.server.listener.RecipeListener
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket
import net.minestom.server.listener.TabCompleteListener
import net.minestom.server.network.packet.client.play.ClientPluginMessagePacket
import net.minestom.server.listener.PluginMessageListener
import net.minestom.server.network.packet.client.play.ClientPlayerAbilitiesPacket
import net.minestom.server.listener.AbilitiesListener
import net.minestom.server.network.packet.client.play.ClientResourcePackStatusPacket
import net.minestom.server.network.packet.client.play.ClientAdvancementTabPacket
import net.minestom.server.listener.AdvancementTabListener
import net.minestom.server.network.packet.client.play.ClientSpectatePacket
import net.minestom.server.listener.SpectateListener
import net.minestom.server.listener.manager.PacketListenerManager
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

/**
 * Small convenient interface to use method references with [PacketListenerManager.setListener].
 *
 * @param <T> the packet type
</T> */
fun interface PacketListenerConsumer<T : ClientPacket?> {
    fun accept(packet: T, player: Player?)
}