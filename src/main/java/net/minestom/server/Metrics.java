package net.minestom.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.utils.debug.DebugUtils;
import org.bstats.MetricsBase;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bstats.json.JsonObjectBuilder;

public class Metrics {
    private static String bStatsUuid = System.getProperty("minestom.bstats.id");
    private final static int SERVICE_ID = 20684;
    private MetricsBase metrics;

    public void start() {
        MinecraftServer.LOGGER.info("Enable bstats.");
        String serverUUID = bStatsUuid;
        if (serverUUID == null) {
            Path bStatsFile = Path.of(".bstats");
            if (Files.notExists(bStatsFile)) {
                try {
                    Files.createFile(bStatsFile);
                } catch (IOException e) {
                    MinecraftServer.LOGGER.error("BStats file cannot created.");
                    return;
                }
                try {
                    Files.writeString(bStatsFile, UUID.randomUUID().toString());
                } catch (IOException e) {
                    MinecraftServer.LOGGER.error("BStats file cannot be written.");
                    return;
                }
            }
            try {
                serverUUID = Files.readString(bStatsFile);
            } catch (IOException e) {
                MinecraftServer.LOGGER.error("BStats file cannot be readed.");
                return;
            }
        }
        System.setProperty("bstats.relocatecheck", "false");
        metrics = new MetricsBase("server-implementation", serverUUID, SERVICE_ID,true,  this::getServerData, jsonObjectBuilder -> {}, null, () -> true, MinecraftServer.LOGGER::error,MinecraftServer.LOGGER::info,
                DebugUtils.INSIDE_TEST, DebugUtils.INSIDE_TEST,DebugUtils.INSIDE_TEST);

        metrics.addCustomChart(new SimplePie("minecraft_version", () -> {
            String minecraftVersion = MinecraftServer.VERSION_NAME;
            minecraftVersion = minecraftVersion.substring(minecraftVersion.indexOf("MC: ") + 4, minecraftVersion.length() - 1);
            return minecraftVersion;
        }));

        metrics.addCustomChart(new SingleLineChart("players", () -> MinecraftServer.getConnectionManager().getOnlinePlayers().size()));
        metrics.addCustomChart(new SimplePie("online_mode", () -> {
            if (MojangAuth.isEnabled()) {
                return "online";
            } else if (VelocityProxy.isEnabled() || BungeeCordProxy.isEnabled()) {
                return "proxied";
            } else {
                return "offline";
            }
        }));
        final String paperVersion = "git-Microtus-%s-%s".formatted(Git.branch(), Git.commit());
        metrics.addCustomChart(new SimplePie("minestom_version", () -> paperVersion));
        metrics.addCustomChart(new DrilldownPie("java_version", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            String javaVersion = System.getProperty("java.version");
            Map<String, Integer> entry = new HashMap<>();
            entry.put(javaVersion, 1);

            // http://openjdk.java.net/jeps/223
            // Java decided to change their versioning scheme and in doing so modified the java.version system
            // property to return $major[.$minor][.$secuity][-ea], as opposed to 1.$major.0_$identifier
            // we can handle pre-9 by checking if the "major" is equal to "1", otherwise, 9+
            String majorVersion = javaVersion.split("\\.")[0];
            String release;

            int indexOf = javaVersion.lastIndexOf('.');

            if (majorVersion.equals("1")) {
                release = "Java " + javaVersion.substring(0, indexOf);
            } else {
                // of course, it really wouldn't be all that simple if they didn't add a quirk, now would it
                // valid strings for the major may potentially include values such as -ea to deannotate a pre release
                Matcher versionMatcher = Pattern.compile("\\d+").matcher(majorVersion);
                if (versionMatcher.find()) {
                    majorVersion = versionMatcher.group(0);
                }
                release = "Java " + majorVersion;
            }
            map.put(release, entry);

            return map;
        }));

        metrics.addCustomChart(new DrilldownPie("extensions", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();

            // count legacy plugins
            int extensions = MinecraftServer.getExtensionManager().getExtensions().size();

            // insert real value as lower dimension
            Map<String, Integer> entry = new HashMap<>();
            entry.put(String.valueOf(extensions), 1);

            // create buckets as higher dimension
            if (extensions == 0) {
                map.put("0 \uD83D\uDE0E", entry); // :sunglasses:
            } else if (extensions <= 5) {
                map.put("1-5", entry);
            } else if (extensions <= 10) {
                map.put("6-10", entry);
            } else if (extensions <= 25) {
                map.put("11-25", entry);
            } else if (extensions <= 50) {
                map.put("26-50", entry);
            } else {
                map.put("50+ \uD83D\uDE2D", entry); // :cry:
            }

            return map;
        }));
    }

    public void shutdown() {
        if (this.metrics != null) {
            this.metrics.shutdown();
        }
    }

    private void getServerData(JsonObjectBuilder builder) {
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

}
