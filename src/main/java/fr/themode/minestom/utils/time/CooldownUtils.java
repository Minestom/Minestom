package fr.themode.minestom.utils.time;

public class CooldownUtils {

    public static boolean hasCooldown(long currentTime, long lastUpdate, TimeUnit timeUnit, int cooldown) {
        long cooldownMs = timeUnit.toMilliseconds(cooldown);
        return currentTime - lastUpdate < cooldownMs;
    }

    public static boolean hasCooldown(long lastUpdate, TimeUnit timeUnit, int cooldown) {
        return hasCooldown(System.currentTimeMillis(), lastUpdate, timeUnit, cooldown);
    }

}
