package net.minestom.server.utils;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
public final class PlatformUtils {
    private PlatformUtils() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformUtils.class);

    /**
     * The current operating system normalized to 'linux', 'macos', 'windows' or 'other'.
     */
    public static final String OS;
    static {
        final String raw = System.getProperty("os.name").toLowerCase();
        if (raw.contains("win")) {
            OS = "windows";
        } else if (raw.contains("mac")) {
            OS = "macos";
        } else if (raw.contains("linux") || raw.contains("unix")) {
            OS = "linux";
        } else {
            LOGGER.warn("Unknown OS: " + raw + ", os specific features and dependencies will not be enabled.");
            OS = "other";
        }
    }

    /**
     * The current architecture normalized to 'x86', 'x64', 'arm64' or 'other'.
     */
    public static final String ARCH;
    static {
        final String raw = System.getProperty("os.arch").toLowerCase();
        if (raw.contains("x86")) {
            ARCH = "x86";
        } else if (raw.contains("amd64")) {
            ARCH = "x64";
        } else if (raw.contains("aarch64")) {
            ARCH = "arm64";
        } else {
            LOGGER.warn("Unknown architecture: " + raw + ", arch specific features and dependencies will not be enabled.");
            ARCH = "other";
        }
    }
}
