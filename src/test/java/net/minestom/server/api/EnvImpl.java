package net.minestom.server.api;

import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.NotNull;

final class EnvImpl implements Env {
    private final ServerProcess process;

    public EnvImpl(ServerProcess process) {
        this.process = process;
    }

    @Override
    public @NotNull ServerProcess process() {
        return process;
    }

    @Override
    public @NotNull TestConnection createConnection() {
        return new TestConnectionImpl(this);
    }
}
