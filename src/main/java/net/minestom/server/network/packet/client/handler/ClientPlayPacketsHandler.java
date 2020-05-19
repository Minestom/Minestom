package net.minestom.server.network.packet.client.handler;

import net.minestom.server.network.packet.client.play.*;

public class ClientPlayPacketsHandler extends ClientPacketsHandler {

    public ClientPlayPacketsHandler() {
        register(0x00, ClientTeleportConfirmPacket::new);
        register(0x03, ClientChatMessagePacket::new);
        register(0x04, ClientStatusPacket::new);
        register(0x05, ClientSettingsPacket::new);
        register(0x06, ClientTabCompletePacket::new);
        register(0x07, ClientWindowConfirmationPacket::new);
        register(0x08, ClientClickWindowButtonPacket::new); // Marked as 0x07 on wiki.vg
        register(0x09, ClientClickWindowPacket::new);
        register(0x0A, ClientCloseWindow::new);
        register(0x0B, ClientPluginMessagePacket::new);
        register(0x0E, ClientInteractEntityPacket::new);
        register(0x0F, ClientKeepAlivePacket::new);

        // 0x10 packet not used server-side
        register(0x11, ClientPlayerPositionPacket::new);
        register(0x12, ClientPlayerPositionAndLookPacket::new);
        register(0x13, ClientPlayerLookPacket::new);
        register(0x14, ClientPlayerPacket::new);
        register(0x15, ClientVehicleMovePacket::new);
        register(0x16, ClientSteerBoatPacket::new);
        register(0x17, ClientPickItemPacket::new);
        register(0x18, ClientCraftRecipeRequest::new);
        register(0x19, ClientPlayerAbilitiesPacket::new);
        register(0x1A, ClientPlayerDiggingPacket::new);
        register(0x1B, ClientEntityActionPacket::new);
        register(0x1C, ClientSteerVehiclePacket::new);
        register(0x1D, ClientRecipeBookData::new);
        register(0x1E, ClientNameItemPacket::new);
        register(0x1F, ClientResourcePackStatusPacket::new);

        register(0x20, ClientAdvancementTabPacket::new);
        register(0x21, ClientSelectTradePacket::new);
        register(0x23, ClientHeldItemChangePacket::new);
        register(0x24, ClientUpdateCommandBlockPacket::new);
        register(0x25, ClientUpdateCommandBlockMinecartPacket::new);
        register(0x26, ClientCreativeInventoryActionPacket::new);
        register(0x29, ClientUpdateSignPacket::new);
        register(0x2A, ClientAnimationPacket::new);
        register(0x2C, ClientPlayerBlockPlacementPacket::new);
        register(0x2D, ClientUseItemPacket::new);
    }
}
