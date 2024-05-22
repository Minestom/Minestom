# Microtus

[![license](https://img.shields.io/github/license/OneLiteFeatherNET/Microtus?style=for-the-badge&color=b2204c)](../LICENSE)
[![wiki](https://img.shields.io/badge/documentation-wiki-74aad6?style=for-the-badge)](https://wiki.minestom.net/)

[Microtus](https://en.wikipedia.org/wiki/Microtus) is a fork of the original minestom but patch based.

Our projects get names of animals but in latin.

Discord for discussion: [OneLiteFeather.net](https://discord.onelitefeather.net)

## Our goals
- Include patches from open pull requests on minestom repository
- Fixes issues from original minestom repository
- Make useful decisions
    - Include new features
    - Try to stay updated with original minecraft cycle
- Release cycle of 2 weeks

## Project contribution:

For details how you can contribute to the project please read our [Contributing](CONTRIBUTING.md).

## Usage of Jitpack:

Some of the project's dependencies are only available in the Jitpack repository.
To ensure a seamless experience, kindly include this repository in your project configuration to prevent any potential issues.

```kt
maven("https://jitpack.io")
```


## Usage of microtus
Since 23.07.2023 we are now official on the maven central for releases and snapshots.  
To use Microtus in your projects you need:
<details>
  <summary>Snapshot</summary>

Please replace the `<version>` with the right one. You can find the versions here: [Central](https://central.sonatype.com/search?q=microtus)  
For example: `1.1.0-SNAPSHOT+9284d26`

### Repositories Section
```kt
repositories {
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots")
}
```

### Dependency Section
```kt
dependencies {
    implementation("net.onelitefeather.microtus:Minestom:1.1.3-SNAPSHOT")
    testImplementation("net.onelitefeather.microtus.testing:testing:1.1.3-SNAPSHOT")
}
```
</details>

<details>
  <summary>Release</summary>

Please replace the `<version>` with the right one. You can find the versions here: [Central](https://central.sonatype.com/search?q=microtus)  
For example: `1.1.0`
### Dependency Section
```kt
dependencies {
    implementation("net.onelitefeather.microtus:Minestom:1.1.3")
    testImplementation("net.onelitefeather.microtus.testing:testing:1.1.3")
}
```
</details>

## Extension usage
### settings.gradle.kts
Read more about here: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry
```kt
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/OneLiteFeatherNET/Microtus") {
            credentials {
              username = "Your username"
              password = "your github token"
            }
        }
    }
}
```

### build.gradle.kts
```kt
plugins {
    id("net.onelitefeather.microtus.extension") version "0.0.1"
}

dependencies {
  extensionLibrary("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2") // Use the external dependencies function from minestom
}
extension {
  authors = listOf("TheMeinerLP")
  entrypoint = "net.onelitefeather.microtus.extension.ProjectEntry"
  // dependencies = listOf("LuckPerms") // To generate dependencies
  // version = "1.0.0" // Normally its use the project version
  // name = "Example" // Normally its use the project name
  // External dependencies are handled via `extensionLibrary("String")` from gradle
}
```

---

[![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com/)

[YourKit](https://www.yourkit.com/), makers of the outstanding java profiler, support open source projects of all kinds with their full featured [Java](https://www.yourkit.com/java/profiler) and [.NET](https://www.yourkit.com/.net/profiler) application profilers. We thank them for granting Microtus an OSS license so that we can make our software the best it can be.
