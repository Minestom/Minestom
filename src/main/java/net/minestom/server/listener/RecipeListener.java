package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientPlaceRecipePacket;
import net.minestom.server.network.packet.server.play.PlaceGhostRecipePacket;

public class RecipeListener {

    public static void listener(ClientPlaceRecipePacket packet, Player player) {
        player.sendPacket(new PlaceGhostRecipePacket(packet.windowId(), packet.recipe()));
    }
}
