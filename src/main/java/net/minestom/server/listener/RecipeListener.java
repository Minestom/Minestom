package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientCraftRecipeRequest;
import net.minestom.server.network.packet.server.play.CraftRecipeResponse;

public class RecipeListener {

    public static void listener(ClientCraftRecipeRequest packet, Player player) {
        player.getPlayerConnection().sendPacket(new CraftRecipeResponse(packet.windowId(), packet.recipe()));
    }
}
