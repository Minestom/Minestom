![banner](banner_dark.png#gh-dark-mode-only)
![banner](banner_light.png#gh-light-mode-only)

# Minestom

[![license](https://img.shields.io/github/license/Minestom/Minestom?style=for-the-badge&color=b2204c)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)  
[![javadocs](https://img.shields.io/badge/documentation-javadocs-4d7a97?style=for-the-badge)](https://javadoc.minestom.net)
[![wiki](https://img.shields.io/badge/documentation-wiki-74aad6?style=for-the-badge)](https://wiki.minestom.net/)
[![discord-banner](https://img.shields.io/discord/706185253441634317?label=discord&style=for-the-badge&color=7289da)](https://discord.gg/pkFRvqB)

Minestom is an open-source library that enables developers to create their own Minecraft server software, without any code from Mojang.

Minestom does not include any vanilla features by default, however we have an extensive API which allows you to code any feature with ease.

This is a library for developers, thus it is not meant to be used by the average server owner. Replacing a Bukkit, Forge, or Fabric server with this **will not work**, because we do not implementing their APIs.

# Table of Contents
- [Minestom](#minestom)
- [Table of Contents](#table-of-contents)
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

Minestom is available on [Maven Central](https://mvnrepository.com/artifact/net.minestom/minestom-snapshots). You can add the following code to your build script to install it.

<details>
<summary>Gradle (Kotlin)</summary>
<br>

```kts
repositories {
    mavenCentral()
}

implementation("net.minestom:minestom-snapshots:XXXXXXXXXX")
```

</details>

<details>
<summary>Gradle (Groovy)</summary>
<br>

```groovy
repositories {
    mavenCentral()
}

implementation 'net.minestom:minestom-snapshots:XXXXXXXXXX'
```

</details>

<details>
<summary>Maven</summary>
<br>

```xml
<dependency>
    <groupId>net.minestom</groupId>
    <artifactId>minestom-snapshots</artifactId>
    <version>XXXXXXXXXX</version>
</dependency>
```
</details>

# Usage
To get started writing your first server, check out our official [wiki](https://minestom.net/docs/introduction). For an API reference, consult the [Javadocs](https://minestom.github.io/Minestom/).

# Why Minestom?
Minecraft has evolved a lot since its release, and most of the servers today do not take advantage of vanilla features, which leads to performance struggles.

Our target audience are developers who wish to make a server that benefits very little from vanilla features, such as a minigame or KitPVP server.

The goal is to offer more performance for those who need it. In other words, it makes sense to use Minestom when it will take less time implementing every missing feature you want, rather than removing every vanilla feature that will slow you down.

# Advantages and Disadvantages
Minestom isn't perfect, and while our choices make it better for some cases, it's not suitable for others.

## Advantages
* High performance — Designed for effiency, offering high performance with minimal overhead.
* Lightweight — Comes with little to no functionality, allowing the server to be easily extended.
* Modern API — Written in Java 21, using best practices and standards.
* Open source — Anyone can contribute new features and improvements.
* Multi-threaded — Uses a thread pool to manage chunks independently from instances.
* Active community — We have lots of developers who contribute to our development and are happy to provide assistance.
* Avoid legacy NMS code — Obfuscation is a thing of the past, and the protocol is fully implemented and exposed to developers.

## Disadvantages
* Doesn't work with traditional plugins or mods.
* Doesn't support older versions. (Using a proxy like Velocity with [ViaBackwards](https://modrinth.com/plugin/viabackwards) is possible)
* Unsuitable for those who want a server with vanilla mechanics.
* Takes a bigger time investment to develop something playable.
* Less users and smaller plugin/library ecosystem than Spigot.

# API
Even if we do not include any vanilla mechanics by default, we simplify the way new features are added. Here are a few examples:

## Instances
One of our major concepts, worlds are great for survival with friends, but when it scales up it can become unmanageable. The best examples can be found in Skyblock or minigames, not being able to separate each part properly and being forced to save everything in files, not to say the overhead caused by unnecessary data contained in them. Instances are a lightweight solution to it, being able to have every chunk in memory only, copying and sending it to another player in no time, with custom serialization and much more...

Being able to create instances directly on the go is a must-have, we believe it can push many more projects forward.

Instances also come with performance benefits, unlike some others which will be fully single-threaded or maybe using one thread per world we are using a set number of threads (pool) to manage all chunks independently from instances, meaning using more CPU power.

## Blocks
Minestom does not know what a chest is by default, you will have to define that it should open an inventory.

Every "special block" that isn't purely visual will need a specialized block handler. After applying this handler, you have a block that can be placed anywhere simply.

However, all blocks are visually there, they just won't have interaction by default.

## Entities
"Passive" or "hostile" mobs do not exist, there's nothing stopping you from making a flying chicken rushing into any players coming too close, and doing so with NMS quickly becomes a mess because of obfuscation and inheritance.

## Inventories
In modern Minecraft, inventories are used as a GUI interface with clickable items, and we support these interactions natively without needing to program your own solution.

## Commands
Since 1.13, Minecraft has added a new library named "[Brigadier](https://github.com/Mojang/brigadier)", and we have developed an API designed to reach the full potential of the new argument types.

# Credits
* The [contributors](https://github.com/Minestom/Minestom/graphs/contributors) of the project.
* [The Minecraft Coalition](https://wiki.vg/) and [`#mcdevs`](https://github.com/mcdevs) for their research of the Minecraft protocol and file formats.
* [The Minecraft Wiki](https://minecraft.wiki) for all of their useful info.
* [JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html) for their amazing Java profiler.

# Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md)!

# License
This project is licensed under the [Apache License Version 2.0](../LICENSE).