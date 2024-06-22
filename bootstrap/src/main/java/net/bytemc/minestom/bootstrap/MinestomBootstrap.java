package net.bytemc.minestom.bootstrap;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;

import java.util.List;
import java.util.Objects;

public class MinestomBootstrap {

    public static void main(String[] arguments) {
        MinecraftServer server = MinecraftServer.init();
        var args = List.of(arguments);

        if (args.contains("--velocity")) {
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i).equals("--velocity")) {
                    VelocityProxy.enable(args.get(i + 1));
                    MinecraftServer.LOGGER.info("Velocity will be enabled...");
                }
            }
        } else {
            if (args.contains("--disableMojangAuth")) {
                MinecraftServer.LOGGER.info("Disable MojangAuth...");
            } else {
                MojangAuth.init();
            }
        }

        var port = 25565;
        var host = "0.0.0.0";
        if (args.contains("--port")) {
            for (int i = 0; i < args.size(); i++) {
                if (Objects.equals(args.get(i), "--port")) {
                    port = Integer.parseInt(args.get(i + 1));
                }
                if (Objects.equals(args.get(i), "--host")) {
                    host = args.get(i + 1);
                }
            }
            MinecraftServer.LOGGER.info("Runtime port is {}", port);
        }
        server.start(host, port);
        MinecraftServer.LOGGER.info("Minestom server was started!");
    }

}
