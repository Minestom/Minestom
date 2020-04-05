package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientCraftRecipeRequest;
import fr.themode.minestom.net.packet.server.play.CraftRecipeResponse;

public class RecipeListener {

    public static void listener(ClientCraftRecipeRequest packet, Player player) {
        CraftRecipeResponse recipeResponse = new CraftRecipeResponse();
        recipeResponse.windowId = packet.windowId;
        recipeResponse.recipe = packet.recipe;
        player.getPlayerConnection().sendPacket(recipeResponse);
    }
}
