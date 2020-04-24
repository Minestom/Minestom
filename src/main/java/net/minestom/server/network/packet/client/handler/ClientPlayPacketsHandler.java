package net.minestom.server.network.packet.client.handler;

import net.minestom.server.network.packet.client.play.*;

public class ClientPlayPacketsHandler extends ClientPacketsHandler {

    public ClientPlayPacketsHandler() {
        register(0x00, ClientTeleportConfirmPacket.class);
        register(0x03, ClientChatMessagePacket.class);
        register(0x04, ClientStatusPacket.class);
        register(0x05, ClientSettingsPacket.class);
        register(0x06, ClientTabCompletePacket.class);
        register(0x07, ClientWindowConfirmationPacket.class);
        register(0x08, ClientClickWindowButtonPacket.class); // Marked as 0x07 on wiki.vg
        register(0x09, ClientClickWindowPacket.class);
        register(0x0A, ClientCloseWindow.class);
        register(0x0B, ClientPluginMessagePacket.class);
        register(0x0E, ClientInteractEntityPacket.class);
        register(0x0F, ClientKeepAlivePacket.class);

        // 0x10 packet not used server-side
        register(0x11, ClientPlayerPositionPacket.class);
        register(0x12, ClientPlayerPositionAndLookPacket.class);
        register(0x13, ClientPlayerLookPacket.class);
        register(0x14, ClientPlayerPacket.class);
        register(0x15, ClientVehicleMovePacket.class);
        register(0x16, ClientSteerBoatPacket.class);
        register(0x17, ClientPickItemPacket.class);
        register(0x18, ClientCraftRecipeRequest.class);
        register(0x19, ClientPlayerAbilitiesPacket.class);
        register(0x1A, ClientPlayerDiggingPacket.class);
        register(0x1B, ClientEntityActionPacket.class);
        register(0x1C, ClientSteerVehiclePacket.class);
        register(0x1D, ClientRecipeBookData.class);
        register(0x1E, ClientNameItemPacket.class);
        register(0x1F, ClientResourcePackStatusPacket.class);

        register(0x20, ClientAdvancementTabPacket.class);
        register(0x21, ClientSelectTradePacket.class);
        register(0x23, ClientHeldItemChangePacket.class);
        register(0x24, ClientUpdateCommandBlockPacket.class);
        register(0x25, ClientUpdateCommandBlockMinecartPacket.class);
        register(0x26, ClientCreativeInventoryActionPacket.class);
        register(0x29, ClientUpdateSignPacket.class);
        register(0x2A, ClientAnimationPacket.class);
        register(0x2C, ClientPlayerBlockPlacementPacket.class);
        register(0x2D, ClientUseItemPacket.class);
    }
}
