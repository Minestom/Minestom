package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientPlaceRecipePacket;

public class RecipeListener {

    public static void listener(ClientPlaceRecipePacket packet, Player player) {
        // TODO(1.21.2)
//        player.sendPacket(new PlaceGhostRecipePacket(packet.windowId(), packet.recipe()));
    }
}
