package net.minestom.server.network.packet.client.handler;

import net.minestom.server.network.packet.client.play.*;

public class ClientPlayPacketsHandler extends ClientPacketsHandler {

    public ClientPlayPacketsHandler() {
        register(0x00, ClientTeleportConfirmPacket::new);
        register(0x01, ClientQueryBlockNbtPacket::new);
        register(0x03, ClientChatMessagePacket::new);
        register(0x04, ClientStatusPacket::new);
        register(0x05, ClientSettingsPacket::new);
        register(0x06, ClientTabCompletePacket::new);
        register(0x07, ClientWindowConfirmationPacket::new);
        register(0x08, ClientClickWindowButtonPacket::new);
        register(0x09, ClientClickWindowPacket::new);
        register(0x0A, ClientCloseWindow::new);
        register(0x0B, ClientPluginMessagePacket::new);
        register(0x0C, ClientEditBookPacket::new);
        register(0x0D, ClientQueryEntityNbtPacket::new);
        register(0x0E, ClientInteractEntityPacket::new);
        register(0x0F, ClientGenerateStructurePacket::new);
        register(0x10, ClientKeepAlivePacket::new);

        // 0x11 packet not used server-side
        register(0x12, ClientPlayerPositionPacket::new);
        register(0x13, ClientPlayerPositionAndRotationPacket::new);
        register(0x14, ClientPlayerRotationPacket::new);
        register(0x15, ClientPlayerPacket::new);
        register(0x16, ClientVehicleMovePacket::new);
        register(0x17, ClientSteerBoatPacket::new);
        register(0x18, ClientPickItemPacket::new);
        register(0x19, ClientCraftRecipeRequest::new);
        register(0x1A, ClientPlayerAbilitiesPacket::new);
        register(0x1B, ClientPlayerDiggingPacket::new);
        register(0x1C, ClientEntityActionPacket::new);
        register(0x1D, ClientSteerVehiclePacket::new);
        register(0x1E, ClientRecipeBookData::new);
        register(0x1F, ClientRecipeBookData::new);


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
        //Update Structure Block??
        register(0x2B, ClientUpdateSignPacket::new);
        register(0x2C, ClientAnimationPacket::new);
        register(0x2D, ClientSpectatePacket::new);
        register(0x2E, ClientPlayerBlockPlacementPacket::new);
        register(0x2F, ClientUseItemPacket::new);
    }
}
