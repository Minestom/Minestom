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
