package net.minestom.server.extras.selfmodification.mixins;

import net.minestom.server.extras.selfmodification.MinestomOverwriteClassLoader;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.IConsumer;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

public class MixinServiceMinestom extends MixinServiceAbstract {

    private final MinestomOverwriteClassLoader classLoader;
    private final MinestomClassProvider classProvider;
    private final MinestomBytecodeProvider bytecodeProvider;
    private final MixinAuditTrailMinestom auditTrail;

    public MixinServiceMinestom() {
        this.classLoader = MinestomOverwriteClassLoader.getInstance();
        classProvider = new MinestomClassProvider(classLoader);
        bytecodeProvider = new MinestomBytecodeProvider(classLoader);
        auditTrail = new MixinAuditTrailMinestom();
    }

    @Override
    public String getName() {
        return "Minestom";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IClassProvider getClassProvider() {
        return classProvider;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return bytecodeProvider;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList("net.minestom.server.extras.selfmodification.mixins.MixinPlatformAgentMinestom");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleVirtual("Minestom");
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return classLoader.getResourceAsStream(name);
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return auditTrail;
    }

    @Override
    public void wire(MixinEnvironment.Phase phase, IConsumer<MixinEnvironment.Phase> phaseConsumer) {
        super.wire(phase, phaseConsumer);
        // TODO: hook into Minestom initialization process
        phaseConsumer.accept(MixinEnvironment.Phase.PREINIT);
        phaseConsumer.accept(MixinEnvironment.Phase.INIT);
    }
}
