package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record UnlockRecipesPacket(int mode,
                                  boolean craftingRecipeBookOpen, boolean craftingRecipeBookFilterActive,
                                  boolean smeltingRecipeBookOpen, boolean smeltingRecipeBookFilterActive,
                                  boolean blastFurnaceRecipeBookOpen, boolean blastFurnaceRecipeBookFilterActive,
                                  boolean smokerRecipeBookOpen, boolean smokerRecipeBookFilterActive,
                                  @NotNull List<String> recipeIds,
                                  @UnknownNullability List<String> initRecipeIds) implements ServerPacket.Play {
    public UnlockRecipesPacket {
        recipeIds = List.copyOf(recipeIds);
        if (initRecipeIds != null) {
            initRecipeIds = List.copyOf(initRecipeIds);
        }
    }

    public static final NetworkBuffer.Type<UnlockRecipesPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, UnlockRecipesPacket value) {
            buffer.write(VAR_INT, value.mode);
            buffer.write(BOOLEAN, value.craftingRecipeBookOpen);
            buffer.write(BOOLEAN, value.craftingRecipeBookFilterActive);
            buffer.write(BOOLEAN, value.smeltingRecipeBookOpen);
            buffer.write(BOOLEAN, value.smeltingRecipeBookFilterActive);
            buffer.write(BOOLEAN, value.blastFurnaceRecipeBookOpen);
            buffer.write(BOOLEAN, value.blastFurnaceRecipeBookFilterActive);
            buffer.write(BOOLEAN, value.smokerRecipeBookOpen);
            buffer.write(BOOLEAN, value.smokerRecipeBookFilterActive);

            buffer.writeCollection(STRING, value.recipeIds);
            if (value.mode == 0) {
                buffer.writeCollection(STRING, value.initRecipeIds);
            }
        }

        @Override
        public UnlockRecipesPacket read(@NotNull NetworkBuffer buffer) {
            var mode = buffer.read(VAR_INT);
            var craftingRecipeBookOpen = buffer.read(BOOLEAN);
            var craftingRecipeBookFilterActive = buffer.read(BOOLEAN);
            var smeltingRecipeBookOpen = buffer.read(BOOLEAN);
            var smeltingRecipeBookFilterActive = buffer.read(BOOLEAN);
            var blastFurnaceRecipeBookOpen = buffer.read(BOOLEAN);
            var blastFurnaceRecipeBookFilterActive = buffer.read(BOOLEAN);
            var smokerRecipeBookOpen = buffer.read(BOOLEAN);
            var smokerRecipeBookFilterActive = buffer.read(BOOLEAN);
            var recipeIds = buffer.readCollection(STRING, DeclareRecipesPacket.MAX_RECIPES);
            var initRecipeIds = mode == 0 ? buffer.readCollection(STRING, DeclareRecipesPacket.MAX_RECIPES) : null;
            return new UnlockRecipesPacket(mode,
                    craftingRecipeBookOpen, craftingRecipeBookFilterActive,
                    smeltingRecipeBookOpen, smeltingRecipeBookFilterActive,
                    blastFurnaceRecipeBookOpen, blastFurnaceRecipeBookFilterActive,
                    smokerRecipeBookOpen, smokerRecipeBookFilterActive,
                    recipeIds, initRecipeIds);
        }
    };
}
