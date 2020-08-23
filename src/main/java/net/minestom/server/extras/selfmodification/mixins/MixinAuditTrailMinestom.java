package net.minestom.server.extras.selfmodification.mixins;

import lombok.extern.slf4j.Slf4j;
import org.spongepowered.asm.service.IMixinAuditTrail;

/**
 * Takes care of logging mixin operations
 */
@Slf4j
public class MixinAuditTrailMinestom implements IMixinAuditTrail {
    @Override
    public void onApply(String className, String mixinName) {
        log.trace("Applied mixin "+mixinName+" to class "+className);
    }

    @Override
    public void onPostProcess(String className) {
        log.trace("Post processing "+className);
    }

    @Override
    public void onGenerate(String className, String generatorName) {
        log.trace("Generating class "+className+" via generator "+generatorName);
    }
}
