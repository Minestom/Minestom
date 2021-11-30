package net.minestom.server.network.packet.client;

import net.minestom.server.network.packet.client.login.EncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.LoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.LoginStartPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.client.status.PingPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.utils.ObjectArray;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

/**
 * Contains registered packets and a way to instantiate them.
 * <p>
 * Packets are registered using {@link #register(int, Function)} and created using {@link #create(int, BinaryReader)}.
 */
public sealed class ClientPacketsHandler permits ClientPacketsHandler.Status, ClientPacketsHandler.Login, ClientPacketsHandler.Play {
    private final ObjectArray<Function<BinaryReader, ClientPacket>> suppliers = new ObjectArray<>(0x10);

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
            register(0x03, ClientChatMessagePacket::new);
            register(0x04, ClientStatusPacket::new);
            register(0x05, ClientSettingsPacket::new);
            register(0x06, ClientTabCompletePacket::new);
            register(0x07, ClientClickWindowButtonPacket::new);
            register(0x08, ClientClickWindowPacket::new);
            register(0x09, ClientCloseWindowPacket::new);
            register(0x0A, ClientPluginMessagePacket::new);
            register(0x0B, ClientEditBookPacket::new);
            register(0x0C, ClientQueryEntityNbtPacket::new);
            register(0x0D, ClientInteractEntityPacket::new);
            register(0x0E, ClientGenerateStructurePacket::new);
            register(0x0F, ClientKeepAlivePacket::new);

            // 0x10 packet not used server-side
            register(0x11, ClientPlayerPositionPacket::new);
            register(0x12, ClientPlayerPositionAndRotationPacket::new);
            register(0x13, ClientPlayerRotationPacket::new);
            register(0x14, ClientPlayerPacket::new);
            register(0x15, ClientVehicleMovePacket::new);
            register(0x16, ClientSteerBoatPacket::new);
            register(0x17, ClientPickItemPacket::new);
            register(0x18, ClientCraftRecipeRequest::new);
            register(0x19, ClientPlayerAbilitiesPacket::new);
            register(0x1A, ClientPlayerDiggingPacket::new);
            register(0x1B, ClientEntityActionPacket::new);
            register(0x1C, ClientSteerVehiclePacket::new);
            register(0x1D, ClientPongPacket::new);
            register(0x1E, ClientSetRecipeBookStatePacket::new);
            register(0x1F, ClientSetDisplayedRecipePacket::new);

            register(0x20, ClientNameItemPacket::new);
            register(0x21, ClientResourcePackStatusPacket::new);
            register(0x22, ClientAdvancementTabPacket::new);
            register(0x23, ClientSelectTradePacket::new);
            register(0x24, ClientSetBeaconEffectPacket::new);
            register(0x25, ClientHeldItemChangePacket::new);
            register(0x26, ClientUpdateCommandBlockPacket::new);
            register(0x27, ClientUpdateCommandBlockMinecartPacket::new);
            register(0x28, ClientCreativeInventoryActionPacket::new);
            //Update Jigsaw Block??
            register(0x2A, ClientUpdateStructureBlockPacket::new);
            register(0x2B, ClientUpdateSignPacket::new);
            register(0x2C, ClientAnimationPacket::new);
            register(0x2D, ClientSpectatePacket::new);
            register(0x2E, ClientPlayerBlockPlacementPacket::new);
            register(0x2F, ClientUseItemPacket::new);
        }
    }
}
