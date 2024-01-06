package net.minestom.server.network.packet.client;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.*;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.client.status.PingPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

/**
 * Contains registered packets and a way to instantiate them.
 * <p>
 * Packets are registered using {@link #register(int, Function)} and created using {@link #create(int, NetworkBuffer)}.
 */
public sealed class ClientPacketsHandler permits ClientPacketsHandler.Status, ClientPacketsHandler.Login, ClientPacketsHandler.Configuration, ClientPacketsHandler.Play {
    private final ObjectArray<Function<NetworkBuffer, ClientPacket>> suppliers = ObjectArray.singleThread(0x10);

    private ClientPacketsHandler() {
    }

    public void register(int id, @NotNull Function<@NotNull NetworkBuffer, @NotNull ClientPacket> packetSupplier) {
        this.suppliers.set(id, packetSupplier);
    }

    public @UnknownNullability ClientPacket create(int packetId, @NotNull NetworkBuffer reader) {
        final Function<NetworkBuffer, ClientPacket> supplier = suppliers.get(packetId);
        if (supplier == null)
            throw new IllegalStateException("Packet id 0x" + Integer.toHexString(packetId) + " isn't registered!");
        return supplier.apply(reader);
    }

    public static final class Status extends ClientPacketsHandler {
        private static int nextId = 0;
        private static int nextId() {
            return nextId++;
        }

        public Status() {
            register(nextId(), StatusRequestPacket::new);
            register(nextId(), PingPacket::new);
        }
    }

    public static final class Login extends ClientPacketsHandler {
        private static int nextId = 0;
        private static int nextId() {
            return nextId++;
        }

        public Login() {
            register(nextId(), ClientLoginStartPacket::new);
            register(nextId(), ClientEncryptionResponsePacket::new);
            register(nextId(), ClientLoginPluginResponsePacket::new);
            register(nextId(), ClientLoginAcknowledgedPacket::new);
        }
    }

    public static final class Configuration extends ClientPacketsHandler {
        private static int nextId = 0;
        private static int nextId() {
            return nextId++;
        }

        public Configuration() {
            register(nextId(), ClientSettingsPacket::new);
            register(nextId(), ClientPluginMessagePacket::new);
            register(nextId(), ClientFinishConfigurationPacket::new);
            register(nextId(), ClientKeepAlivePacket::new);
            register(nextId(), ClientPongPacket::new);
            register(nextId(), ClientResourcePackStatusPacket::new);
        }

    }

    public static final class Play extends ClientPacketsHandler {
        private static int nextId = 0;

        private static int nextId() {
            return nextId++;
        }

        public Play() {
            register(nextId(), ClientTeleportConfirmPacket::new);
            register(nextId(), ClientQueryBlockNbtPacket::new);
            nextId(); // difficulty packet
            register(nextId(), ClientChatAckPacket::new);
            register(nextId(), ClientCommandChatPacket::new);
            register(nextId(), ClientChatMessagePacket::new);
            register(nextId(), ClientChatSessionUpdatePacket::new);
            register(nextId(), ClientChunkBatchReceivedPacket::new);
            register(nextId(), ClientStatusPacket::new);
            register(nextId(), ClientSettingsPacket::new);
            register(nextId(), ClientTabCompletePacket::new);
            register(nextId(), ClientConfigurationAckPacket::new);
            register(nextId(), ClientClickWindowButtonPacket::new);
            register(nextId(), ClientClickWindowPacket::new);
            register(nextId(), ClientCloseWindowPacket::new);
            register(nextId(), ClientWindowSlotStatePacket::new);
            register(nextId(), ClientPluginMessagePacket::new);
            register(nextId(), ClientEditBookPacket::new);
            register(nextId(), ClientQueryEntityNbtPacket::new);
            register(nextId(), ClientInteractEntityPacket::new);
            register(nextId(), ClientGenerateStructurePacket::new);
            register(nextId(), ClientKeepAlivePacket::new);
            nextId(); // lock difficulty
            register(nextId(), ClientPlayerPositionPacket::new);
            register(nextId(), ClientPlayerPositionAndRotationPacket::new);
            register(nextId(), ClientPlayerRotationPacket::new);
            register(nextId(), ClientPlayerPacket::new);
            register(nextId(), ClientVehicleMovePacket::new);
            register(nextId(), ClientSteerBoatPacket::new);
            register(nextId(), ClientPickItemPacket::new);
            nextId(); // Ping request
            register(nextId(), ClientCraftRecipeRequest::new);
            register(nextId(), ClientPlayerAbilitiesPacket::new);
            register(nextId(), ClientPlayerDiggingPacket::new);
            register(nextId(), ClientEntityActionPacket::new);
            register(nextId(), ClientSteerVehiclePacket::new);
            register(nextId(), ClientPongPacket::new);
            register(nextId(), ClientSetRecipeBookStatePacket::new);
            register(nextId(), ClientSetDisplayedRecipePacket::new);
            register(nextId(), ClientNameItemPacket::new);
            register(nextId(), ClientResourcePackStatusPacket::new);
            register(nextId(), ClientAdvancementTabPacket::new);
            register(nextId(), ClientSelectTradePacket::new);
            register(nextId(), ClientSetBeaconEffectPacket::new);
            register(nextId(), ClientHeldItemChangePacket::new);
            register(nextId(), ClientUpdateCommandBlockPacket::new);
            register(nextId(), ClientUpdateCommandBlockMinecartPacket::new);
            register(nextId(), ClientCreativeInventoryActionPacket::new);
            nextId(); // Update Jigsaw Block
            register(nextId(), ClientUpdateStructureBlockPacket::new);
            register(nextId(), ClientUpdateSignPacket::new);
            register(nextId(), ClientAnimationPacket::new);
            register(nextId(), ClientSpectatePacket::new);
            register(nextId(), ClientPlayerBlockPlacementPacket::new);
            register(nextId(), ClientUseItemPacket::new);
        }
    }
}
