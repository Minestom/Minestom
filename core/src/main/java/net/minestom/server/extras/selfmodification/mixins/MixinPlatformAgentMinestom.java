package net.minestom.server.extras.selfmodification.mixins;

import org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent;
import org.spongepowered.asm.launch.platform.MixinPlatformAgentAbstract;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.util.Constants;

import java.util.Collection;

public class MixinPlatformAgentMinestom extends MixinPlatformAgentAbstract implements IMixinPlatformServiceAgent {
    @Override
    public void init() { }

    @Override
    public String getSideName() {
        return Constants.SIDE_SERVER;
    }

    @Override
    public AcceptResult accept(MixinPlatformManager manager, IContainerHandle handle) {
        return AcceptResult.ACCEPTED;
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return null;
    }
}
