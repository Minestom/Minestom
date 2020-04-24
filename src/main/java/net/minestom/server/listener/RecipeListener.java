package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientCraftRecipeRequest;
import net.minestom.server.network.packet.server.play.CraftRecipeResponse;

public class RecipeListener {

    public static void listener(ClientCraftRecipeRequest packet, Player player) {
        CraftRecipeResponse recipeResponse = new CraftRecipeResponse();
        recipeResponse.windowId = packet.windowId;
        recipeResponse.recipe = packet.recipe;
        player.getPlayerConnection().sendPacket(recipeResponse);
    }
}
