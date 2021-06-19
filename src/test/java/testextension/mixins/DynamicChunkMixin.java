package testextension.mixins;

import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DynamicChunk.class)
public class DynamicChunkMixin {

    @ModifyVariable(method = "setBlock", at = @At("HEAD"), index = 4, require = 1, argsOnly = true, remap = false)
    public int oopsAllTnt(short blockStateId) {
        if(blockStateId != 0)
            return Block.TNT.id();
        return 0;
    }
}
