package net.minestom.server.thread;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public class MinestomThread extends Thread {
    public MinestomThread(String name) {
        super(name);
    }
}
