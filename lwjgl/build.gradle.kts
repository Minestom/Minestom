import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

description = "LWJGL-related code for map rendering"

// get lwjgl native version
private val os = DefaultNativePlatform.getCurrentOperatingSystem()
val lwjglNativesVersion = when {
    os.isWindows -> {
        val osArch = System.getProperty("os.arch")
        if (osArch.startsWith("arm") || osArch.startsWith("aarch64")) {
            if (osArch.contains("64") || osArch.startsWith("armv8")) {
                "natives-linux-arm64"
            } else {
                "natives-linux-arm32"
            }
        } else {
            "natives-linux"
        }
    }
    os.isMacOsX -> "natives-macos"
    os.isLinux -> if (System.getProperty("os.arch").contains("64")) "natives-windows" else "natives-windows-x86"
    else -> "unknown"
}

dependencies {
    compileOnly(projects.minestomCore)

    // lwjgl - java game library for graphics rendering
    val lwjglVersion = "3.2.3"
    api("org.lwjgl", "lwjgl", lwjglVersion)
    api("org.lwjgl", "lwjgl-egl", lwjglVersion)
    api("org.lwjgl", "lwjgl-opengl", lwjglVersion)
    api("org.lwjgl", "lwjgl-opengles", lwjglVersion)
    api("org.lwjgl", "lwjgl-glfw", lwjglVersion)

    // lwjgl natives - native implementations of lwjgl
    runtimeOnly("org.lwjgl", "lwjgl", "", "", lwjglNativesVersion)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", "", "", lwjglNativesVersion)
    runtimeOnly("org.lwjgl", "lwjgl-opengles", "", "", lwjglNativesVersion)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", "", "", lwjglNativesVersion)

    // joml - math lib for opengl rendering
    api("org.joml", "joml", "1.9.25")
}