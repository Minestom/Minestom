package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public value record RecipeBookSettingsPacket(boolean craftingRecipeBookOpen,boolean craftingRecipeBookFilterActive,boolean smeltingRecipeBookOpen,boolean smeltingRecipeBookFilterActive,boolean blastFurnaceRecipeBookOpen,boolean blastFurnaceRecipeBookFilterActive,boolean smokerRecipeBookOpen,boolean smokerRecipeBookFilterActive) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<RecipeBookSettingsPacket> SERIALIZER = NetworkBufferTemplate.template(
            BOOLEAN, RecipeBookSettingsPacket::craftingRecipeBookOpen,
            BOOLEAN, RecipeBookSettingsPacket::craftingRecipeBookFilterActive,
            BOOLEAN, RecipeBookSettingsPacket::smeltingRecipeBookOpen,
            BOOLEAN, RecipeBookSettingsPacket::smeltingRecipeBookFilterActive,
            BOOLEAN, RecipeBookSettingsPacket::blastFurnaceRecipeBookOpen,
            BOOLEAN, RecipeBookSettingsPacket::blastFurnaceRecipeBookFilterActive,
            BOOLEAN, RecipeBookSettingsPacket::smokerRecipeBookOpen,
            BOOLEAN, RecipeBookSettingsPacket::smokerRecipeBookFilterActive,
            RecipeBookSettingsPacket::new);

}
