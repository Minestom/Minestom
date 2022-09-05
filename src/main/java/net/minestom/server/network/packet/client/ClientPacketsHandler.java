package net.minestom.server.network.packet.client;

import net.minestom.server.network.packet.client.login.EncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.LoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.LoginStartPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.client.status.PingPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

/**
 * Contains registered packets and a way to instantiate them.
 * <p>
 * Packets are registered using {@link #register(int, Function)} and created using {@link #create(int, BinaryReader)}.
 */
public sealed class ClientPacketsHandler permits ClientPacketsHandler.Status, ClientPacketsHandler.Login, ClientPacketsHandler.Play {
    private final ObjectArray<Function<BinaryReader, ClientPacket>> suppliers = ObjectArray.singleThread(0x10);

    private ClientPacketsHandler() {
    }

    public void register(int id, @NotNull Function<@NotNull BinaryReader, @NotNull ClientPacket> packetSupplier) {
        this.suppliers.set(id, packetSupplier);
    }

    public @UnknownNullability ClientPacket create(int packetId, @NotNull BinaryReader reader) {
        final Function<BinaryReader, ClientPacket> supplier = suppliers.get(packetId);
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
        public Play() {
            register(0x00, ClientTeleportConfirmPacket::new);
            register(0x01, ClientQueryBlockNbtPacket::new);
            // 0x02 difficulty packet
            register(0x03, ClientChatAckPacket::new);
            register(0x04, ClientCommandChatPacket::new);
            register(0x05, ClientChatMessagePacket::new);
            register(0x06, ClientChatPreviewPacket::new);
            register(0x07, ClientStatusPacket::new);
            register(0x08, ClientSettingsPacket::new);
            register(0x09, ClientTabCompletePacket::new);
            register(0x0A, ClientClickWindowButtonPacket::new);
            register(0x0B, ClientClickWindowPacket::new);
            register(0x0C, ClientCloseWindowPacket::new);
            register(0x0D, ClientPluginMessagePacket::new);
            register(0x0E, ClientEditBookPacket::new);
            register(0x0F, ClientQueryEntityNbtPacket::new);
            register(0x10, ClientInteractEntityPacket::new);
            register(0x11, ClientGenerateStructurePacket::new);
            register(0x12, ClientKeepAlivePacket::new);
            // 0x12 packet not used server-side
            register(0x14, ClientPlayerPositionPacket::new);
            register(0x15, ClientPlayerPositionAndRotationPacket::new);
            register(0x16, ClientPlayerRotationPacket::new);
            register(0x17, ClientPlayerPacket::new);
            register(0x18, ClientVehicleMovePacket::new);
            register(0x19, ClientSteerBoatPacket::new);
            register(0x1A, ClientPickItemPacket::new);
            register(0x1B, ClientCraftRecipeRequest::new);
            register(0x1C, ClientPlayerAbilitiesPacket::new);
            register(0x1D, ClientPlayerDiggingPacket::new);
            register(0x1E, ClientEntityActionPacket::new);
            register(0x1F, ClientSteerVehiclePacket::new);
            register(0x20, ClientPongPacket::new);
            register(0x21, ClientSetRecipeBookStatePacket::new);
            register(0x22, ClientSetDisplayedRecipePacket::new);
            register(0x23, ClientNameItemPacket::new);
            register(0x24, ClientResourcePackStatusPacket::new);
            register(0x25, ClientAdvancementTabPacket::new);
            register(0x26, ClientSelectTradePacket::new);
            register(0x27, ClientSetBeaconEffectPacket::new);
            register(0x28, ClientHeldItemChangePacket::new);
            register(0x29, ClientUpdateCommandBlockPacket::new);
            register(0x2A, ClientUpdateCommandBlockMinecartPacket::new);
            register(0x2B, ClientCreativeInventoryActionPacket::new);
            // 0x2B Update Jigsaw Block
            register(0x2D, ClientUpdateStructureBlockPacket::new);
            register(0x2E, ClientUpdateSignPacket::new);
            register(0x2F, ClientAnimationPacket::new);
            register(0x30, ClientSpectatePacket::new);
            register(0x31, ClientPlayerBlockPlacementPacket::new);
            register(0x32, ClientUseItemPacket::new);
        }
    }
}
