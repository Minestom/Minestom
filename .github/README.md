![banner](banner_dark.png#gh-dark-mode-only)
![banner](banner_light.png#gh-light-mode-only)

# Minestom

[![license](https://img.shields.io/github/license/Minestom/Minestom?style=for-the-badge&color=b2204c)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)  
[![javadocs](https://img.shields.io/badge/documentation-javadocs-4d7a97?style=for-the-badge)](https://javadoc.minestom.net)
[![wiki](https://img.shields.io/badge/documentation-wiki-74aad6?style=for-the-badge)](https://minestom.net/docs/introduction)
[![discord-banner](https://img.shields.io/discord/706185253441634317?label=discord&style=for-the-badge&color=7289da)](https://discord.gg/pkFRvqB)

Minestom is an open-source library that enables developers to create their own Minecraft server software, without any code from Mojang.

Minestom does not include any vanilla features by default, however we have an extensive API which allows you to code any feature with ease.

This is a library for developers, and thus it is not meant to be used by the average server owner. Replacing a Bukkit, Forge, or Fabric server with this **will not work**, because we do not implement their APIs.

# Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [Why Minestom?](#why-minestom)
- [Advantages and Disadvantages](#advantages-and-disadvantages)
  - [Advantages](#advantages)
  - [Disadvantages](#disadvantages)
- [API](#api)
  - [Instances](#instances)
  - [Blocks](#blocks)
  - [Entities](#entities)
  - [Inventories](#inventories)
  - [Commands](#commands)
- [Credits](#credits)
- [Contributing](#contributing)
- [License](#license)

# Installation
Because Minestom is a library, it does not ship a JAR file for you to double click and run. You will need to add it as a dependency, add your code, and compile your server yourself.

Minestom is available on [Maven Central](https://mvnrepository.com/artifact/net.minestom/minestom). You can add the code below to your build script to install it.

[![](https://img.shields.io/maven-central/v/net.minestom/minestom)](https://mvnrepository.com/artifact/net.minestom/minestom)

<details open>
<summary>Gradle (Kotlin)</summary>
<br>

```kts
repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:<latest release>")

    // If you want to use the integration testing library.
    testImplementation("net.minestom:testing:<latest release>")
}
```

</details>

<details>
<summary>Gradle (Groovy)</summary>
<br>

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'net.minestom:minestom:<latest release>'

    // If you want to use the integration testing library.
    testImplementation 'net.minestom:testing:<latest release>'
}
```

</details>

<details>
<summary>Maven</summary>
<br>

```xml
<dependency>
    <groupId>net.minestom</groupId>
    <artifactId>minestom</artifactId>
    <version><latest release></version>
</dependency>
```
</details>

PR branches tagged with the "Publish Pull Request" tag are published to the maven central snapshot repository, which can
be used to test new features before they are released. The version for these snapshots is `<branch>-SNAPSHOT`, where 
`<branch>` is the name of the branch. The master branch is also published as `master-SNAPSHOT`.

```kotlin
repositories {
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
        content { // This filtering is optional, but recommended
            includeModule("net.minestom", "minestom")
            includeModule("net.minestom", "testing")
        }
    }
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:<branch>-SNAPSHOT")
    testImplementation("net.minestom:testing:<branch>-SNAPSHOT")
}
```

<details>
<summary>Pinning snapshot versions</summary>

By default, `<branch>-SNAPSHOT` versions will always resolve to the latest snapshot version, meaning the dependency
can update without you changing anything in your build file (and possibly be inconsistent between people if gradle
has cached an older version, by default for 24h).

To pin the snapshot version to a specific release you can reference the exact build. There are two places to find this:
* The maven-metadata.xml, combine the parts of `snapshot.timestamp` and `snapshot.buildNumber`. For example, the 1.21.6
  branch is currently published as `1_21_6-SNAPSHOT` and `1_21_6-20250707.141325-4`.
* In the "External Libraries" section of IntelliJ, if you expand the `-SNAPSHOT` jar it will show the pinnable 
  version which you can use.

</details>

# Usage
To get started writing your first server, check out our official [wiki](https://minestom.net/docs/introduction). For an API reference, consult the [Javadocs](https://javadoc.minestom.net).

# Why Minestom?
Minecraft has evolved a lot since its release, but many servers today overlook the built-in mechanics in favor of custom gameplay, leading to wasted performance.

Our target audience are developers who wish to make a server which benefits very little from vanilla features, such as a mini game or KitPVP server.

The goal is to offer more performance for those who need it. In other words, it makes sense to use Minestom when it will take less time implementing every missing feature you want, rather than removing every vanilla feature that will slow you down.

# Advantages and Disadvantages
Minestom isn't perfect, and while our choices make it better for some cases, it's not suitable for others.

## Advantages
* High performance — Designed for efficiency, offering high performance with minimal overhead.
* Lightweight — Comes with little to no functionality, allowing the server to be easily extended.
* Modern API — Written in Java 21, using best practices and standards.
* Open source — Anyone can contribute new features and improvements.
* Multi-threaded — Uses a thread pool to manage chunks independently from instances.
* Active community — We have lots of developers who contribute to our development and are happy to provide assistance.
* Avoid legacy NMS code — Obfuscation is a thing of the past, and the protocol is fully implemented and exposed to developers.

## Disadvantages
* Doesn't work with traditional plugins or mods.
* Doesn't support older versions. See [the wiki page](https://minestom.net/docs/compatibility/unsupported-versions) for more info.
* Unsuitable for those who want a server with vanilla mechanics.
* Takes a bigger time investment to develop something playable.
* Less users and a smaller plugin/library ecosystem than Spigot.

# API
Even if we do not include any vanilla mechanics by default, we simplify the way new features are added. Here are a few examples:

## Instances
This is one of our major concepts. While worlds are great for playing survival with friends, they do not scale and can become unmanageable. The best examples can be found in Skyblock or minigames, not being able to separate each part properly and being forced to save everything in files, not to mention the overhead caused by unnecessary data contained in them. Instances are a lightweight alternative, being able to have every chunk in memory, copying and sending it to another player in no time, while allowing custom serialization and more.

Being able to create instances directly on the go is a must-have, and we believe it can push many more projects forward.

Instances also come with performance benefits, unlike other server implementations which are single-threaded or only using one thread per world, we are using a set number of threads in a pool to manage all chunks independently from instances, meaning taking advantage of the CPU power.

## Blocks
Minestom does not recognize what a chest does by default, you will have to define that it should open an inventory when you right click it.

Every "special block" that isn't purely visual will need a specialized block handler. After applying this handler, you have a block that can be properly placed down and used.

However, all blocks exist visually, they just won't have any interaction by default.

## Entities
"Passive" or "hostile" mobs do not exist, and there's nothing stopping you from making a flying chicken rushing into any players coming too close. Doing so with NMS quickly becomes a mess due to Minecraft's obfuscation and inheritance.

## Inventories
In modern Minecraft, inventories are used as a graphical user interface with clickable items, and we support these interactions natively without needing to program your own solution.

## Commands
Since 1.13, Minecraft has integrated a Java library to manage commands named "[Brigadier](https://github.com/Mojang/brigadier)". We have developed an API designed to use the argument types to their full potential.

# Credits
* The [contributors](https://github.com/Minestom/Minestom/graphs/contributors) of the project.
* [The Minecraft Wiki](https://minecraft.wiki) for their research of the [Minecraft protocol](https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge) and various file formats.
* [JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html) for their amazing Java profiler.

# Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md)!

# License
This project is licensed under the [Apache License Version 2.0](../LICENSE).
