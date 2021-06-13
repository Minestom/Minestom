package improveextensions.mixins;

import improveextensions.MixinIntoMinestomCore;
import net.minestom.server.world.WorldContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldContainer.class)
public class WorldContainerMixin {

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void constructorHead(CallbackInfo ci) {
        System.out.println("Mixin into WorldContainerMixin");
        MixinIntoMinestomCore.success = true;
    }

}
