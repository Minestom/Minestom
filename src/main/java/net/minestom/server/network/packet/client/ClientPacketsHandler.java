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
            register(0x03, ClientCommandChatPacket::new);
            register(0x04, ClientChatMessagePacket::new);
            register(0x05, ClientChatPreviewPacket::new);
            register(0x06, ClientStatusPacket::new);
            register(0x07, ClientSettingsPacket::new);
            register(0x08, ClientTabCompletePacket::new);
            register(0x09, ClientClickWindowButtonPacket::new);
            register(0x0A, ClientClickWindowPacket::new);
            register(0x0B, ClientCloseWindowPacket::new);
            register(0x0C, ClientPluginMessagePacket::new);
            register(0x0D, ClientEditBookPacket::new);
            register(0x0E, ClientQueryEntityNbtPacket::new);
            register(0x0F, ClientInteractEntityPacket::new);
            register(0x10, ClientGenerateStructurePacket::new);
            register(0x11, ClientKeepAlivePacket::new);
            // 0x12 packet not used server-side
            register(0x13, ClientPlayerPositionPacket::new);
            register(0x14, ClientPlayerPositionAndRotationPacket::new);
            register(0x15, ClientPlayerRotationPacket::new);
            register(0x16, ClientPlayerPacket::new);
            register(0x17, ClientVehicleMovePacket::new);
            register(0x18, ClientSteerBoatPacket::new);
            register(0x19, ClientPickItemPacket::new);
            register(0x1A, ClientCraftRecipeRequest::new);
            register(0x1B, ClientPlayerAbilitiesPacket::new);
            register(0x1C, ClientPlayerDiggingPacket::new);
            register(0x1D, ClientEntityActionPacket::new);
            register(0x1E, ClientSteerVehiclePacket::new);
            register(0x1F, ClientPongPacket::new);
            register(0x20, ClientSetRecipeBookStatePacket::new);
            register(0x21, ClientSetDisplayedRecipePacket::new);
            register(0x22, ClientNameItemPacket::new);
            register(0x23, ClientResourcePackStatusPacket::new);
            register(0x24, ClientAdvancementTabPacket::new);
            register(0x25, ClientSelectTradePacket::new);
            register(0x26, ClientSetBeaconEffectPacket::new);
            register(0x27, ClientHeldItemChangePacket::new);
            register(0x28, ClientUpdateCommandBlockPacket::new);
            register(0x29, ClientUpdateCommandBlockMinecartPacket::new);
            register(0x2A, ClientCreativeInventoryActionPacket::new);
            // 0x2B Update Jigsaw Block
            register(0x2C, ClientUpdateStructureBlockPacket::new);
            register(0x2D, ClientUpdateSignPacket::new);
            register(0x2E, ClientAnimationPacket::new);
            register(0x2F, ClientSpectatePacket::new);
            register(0x30, ClientPlayerBlockPlacementPacket::new);
            register(0x31, ClientUseItemPacket::new);
        }
    }
}
