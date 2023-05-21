package net.minestom.server.network.packet.client;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.login.EncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.LoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.LoginStartPacket;
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
public sealed class ClientPacketsHandler permits ClientPacketsHandler.Status, ClientPacketsHandler.Login, ClientPacketsHandler.Play {
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
        public Status() {
            register(0x00, StatusRequestPacket::new);
            register(0x01, PingPacket::new);
        }
    }

    public static final class Login extends ClientPacketsHandler {
        public Login() {
            register(0x00, LoginStartPacket::new);
            register(0x01, EncryptionResponsePacket::new);
            register(0x02, LoginPluginResponsePacket::new);
        }
    }

    public static final class Play extends ClientPacketsHandler {
        private static int nextId = 0;

        private static int nextPlayId() {
            return nextId++;
        }

        public Play() {
            register(nextPlayId(), ClientTeleportConfirmPacket::new);
            register(nextPlayId(), ClientQueryBlockNbtPacket::new);
            nextPlayId(); // difficulty packet
            register(nextPlayId(), ClientChatAckPacket::new);
            register(nextPlayId(), ClientCommandChatPacket::new);
            register(nextPlayId(), ClientChatMessagePacket::new);
            register(nextPlayId(), ClientChatSessionUpdatePacket::new);
            register(nextPlayId(), ClientStatusPacket::new);
            register(nextPlayId(), ClientSettingsPacket::new);
            register(nextPlayId(), ClientTabCompletePacket::new);
            register(nextPlayId(), ClientClickWindowButtonPacket::new);
            register(nextPlayId(), ClientClickWindowPacket::new);
            register(nextPlayId(), ClientCloseWindowPacket::new);
            register(nextPlayId(), ClientPluginMessagePacket::new);
            register(nextPlayId(), ClientEditBookPacket::new);
            register(nextPlayId(), ClientQueryEntityNbtPacket::new);
            register(nextPlayId(), ClientInteractEntityPacket::new);
            register(nextPlayId(), ClientGenerateStructurePacket::new);
            register(nextPlayId(), ClientKeepAlivePacket::new);
            nextPlayId(); // lock difficulty
            register(nextPlayId(), ClientPlayerPositionPacket::new);
            register(nextPlayId(), ClientPlayerPositionAndRotationPacket::new);
            register(nextPlayId(), ClientPlayerRotationPacket::new);
            register(nextPlayId(), ClientPlayerPacket::new);
            register(nextPlayId(), ClientVehicleMovePacket::new);
            register(nextPlayId(), ClientSteerBoatPacket::new);
            register(nextPlayId(), ClientPickItemPacket::new);
            register(nextPlayId(), ClientCraftRecipeRequest::new);
            register(nextPlayId(), ClientPlayerAbilitiesPacket::new);
            register(nextPlayId(), ClientPlayerDiggingPacket::new);
            register(nextPlayId(), ClientEntityActionPacket::new);
            register(nextPlayId(), ClientSteerVehiclePacket::new);
            register(nextPlayId(), ClientPongPacket::new);
            register(nextPlayId(), ClientSetRecipeBookStatePacket::new);
            register(nextPlayId(), ClientSetDisplayedRecipePacket::new);
            register(nextPlayId(), ClientNameItemPacket::new);
            register(nextPlayId(), ClientResourcePackStatusPacket::new);
            register(nextPlayId(), ClientAdvancementTabPacket::new);
            register(nextPlayId(), ClientSelectTradePacket::new);
            register(nextPlayId(), ClientSetBeaconEffectPacket::new);
            register(nextPlayId(), ClientHeldItemChangePacket::new);
            register(nextPlayId(), ClientUpdateCommandBlockPacket::new);
            register(nextPlayId(), ClientUpdateCommandBlockMinecartPacket::new);
            register(nextPlayId(), ClientCreativeInventoryActionPacket::new);
            nextPlayId(); // Update Jigsaw Block
            register(nextPlayId(), ClientUpdateStructureBlockPacket::new);
            register(nextPlayId(), ClientUpdateSignPacket::new);
            register(nextPlayId(), ClientAnimationPacket::new);
            register(nextPlayId(), ClientSpectatePacket::new);
            register(nextPlayId(), ClientPlayerBlockPlacementPacket::new);
            register(nextPlayId(), ClientUseItemPacket::new);
        }
    }
}
