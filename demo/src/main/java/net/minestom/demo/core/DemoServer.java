package net.minestom.demo.core;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.extras.lan.OpenToLANConfig;
import net.minestom.server.utils.time.TimeUnit;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Fluent builder used by every {@code server/*Server.java} launcher. */
public final class DemoServer {

    private final Auth auth;
    private final List<Feature> features = new ArrayList<>();
    private boolean openToLan = true;

    private DemoServer(Auth auth) {
        this.auth = auth;
    }

    public static DemoServer create() {
        return create(new Auth.Offline());
    }

    public static DemoServer create(Auth auth) {
        return new DemoServer(auth);
    }

    public DemoServer feature(Feature feature) {
        features.add(feature);
        return this;
    }

    public DemoServer features(Feature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    public DemoServer openToLan(boolean openToLan) {
        this.openToLan = openToLan;
        return this;
    }

    public void start(String host, int port) {
        bootstrap().start(host, port);
        afterStart();
    }

    public void start(SocketAddress address) {
        bootstrap().start(address);
        afterStart();
    }

    /** Bind on {@code 0.0.0.0:25565}. */
    public void start() {
        start("0.0.0.0", 25565);
    }

    private MinecraftServer bootstrap() {
        System.setProperty("minestom.new-socket-write-lock", "true");
        MinecraftServer.setCompressionThreshold(0);
        MinecraftServer server = MinecraftServer.init(auth);
        ServerProcess process = MinecraftServer.process();
        for (Feature feature : features) feature.register(process);
        return server;
    }

    private void afterStart() {
        if (!openToLan) return;
        OpenToLAN.open(new OpenToLANConfig().eventCallDelay(Duration.of(1, TimeUnit.DAY)));
    }
}
