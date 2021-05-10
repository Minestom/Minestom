package net.minestom.server.extras.selfmodification.mixins;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class MixinServiceMinestomBootstrap implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "MinestomBootstrap";
    }

    @Override
    public String getServiceClassName() {
        return "net.minestom.server.extras.selfmodification.mixins.MixinServiceMinestom";
    }

    @Override
    public void bootstrap() {

    }
}
