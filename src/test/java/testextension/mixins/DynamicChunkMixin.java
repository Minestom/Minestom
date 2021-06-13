package testextension.mixins;

import net.minestom.server.world.DynamicChunk;
import net.minestom.server.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DynamicChunk.class)
public class DynamicChunkMixin {

    @ModifyVariable(method = "UNSAFE_setBlock", at = @At("HEAD"), index = 4, require = 1, argsOnly = true, remap = false)
    public int oopsAllTnt(short blockStateId) {
        if(blockStateId != 0)
            return Block.TNT.getId();
        return 0;
    }
}
