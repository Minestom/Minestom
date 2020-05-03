# Minestom
![banner](banner.png)

[![license](https://img.shields.io/github/license/Minestom/Minestom.svg)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)  
[![discord-banner](https://discordapp.com/api/guilds/706185253441634317/widget.png?style=banner2)](https://discord.gg/pkFRvqB)

Minestom is an alternative to the popular Minecraft server API called Bukkit.


The main difference is that our implementation of the Notchian server does not contain any features by default!
However, we have a complete API which allows you to make anything possible with current spigot plugins.

# Table of contents
- [Install](#install)
- [Usage](#usage)
- [Why Minestom?](#why-minestom)
- [Advantages & Disadvantages](#advantages-and-disadvantages)
- [API](#api)
- [Contributing](#contributing)
- [License](#license)

# Install
Minestom is similar to Bukkit in the fact that it is not a standlone program, it must be expanded upon.
It is the base for interfacing between the server and client.
Our own expanded version for Vanilla can be found [here](https://github.com/Minestom/VanillaReimplementation).

# Usage
An example of how to use the Minestom library is available [here](https://github.com/Minestom/Minestom/tree/master/src/main/java/fr/themode/demo).
Alternatively you can check the official wiki [here](https://github.com/Minestom/Minestom/wiki).

# Why Minestom?
Minecraft evolved a lot since its release, most of the servers today do not take advantage of vanilla features and even have to struggle because of them. Our target audience is those who want to make a completely different server compared to default Minecraft gamemode such as survival or creative building.
The goal is to offer more performance for those who need it, Minecraft being single-threaded is the most important problem for them.

In other words, it makes sense to use Minestom when it takes less time to implement everything you want than removing everything you don't need.

# Advantages and Disadvantages
Minestom isn't perfect, our choices make it much better for some cases, worse for some others.

## Advantages
* Remove the overhead of vanilla features
* Multi-threaded (Chunks & Entities)
* 100% Thread-Safe
* Instance system which is much more scalable than worlds
* Open-source
* Modern API
* No more disgusting NMS

## Disadvantages
* Does not work with Bukkit/Spigot plugins
* Does not work with older clients
* Bad for those who want a vanilla experience
* Longer to develop something playable
* Multi-threaded environments are prone to complications

# API
Even if we do not include anything by default in the game, we simplify the way you add them, here is a preview.

## Instances
It is our major concept, worlds are great for survival with friends, but when it scales up it can become unmanageable. The best examples can be found in Skyblock or minigames, not being able to separate each part properly and being forced to save everything in files, not to say the overhead caused by unnecessary data contained in them. Instances are a lightweight solution to it, being able to have every chunk in memory only, copying and sending it to another player in no time, making your serializer and much more...

Being able to create instances directly on the go is a must-have, according to us it can push many more projects forward.

Instances also come with performance benefits, unlike some others which will be fully single-threaded or maybe using one thread per world we are using a set number of threads (pool) to manage all chunks independently from instances, meaning using more of CPU power.

## Blocks
Minestom by default does not know what is a chest, you will have to tell him that it opens an inventory. 
Every "special blocks" (which aren't only visual) have to be registered, then they can be placed anywhere simply.

However, all blocks are visually there, they just won't have interaction by default.

## Entities
The terms "passive" or "aggressive" monsters do not exist, nobody forbid you from making a flying chicken rushing into any players coming too close, doing so with NMS is a real mess because of obfuscation and the large inheritance.

## Inventories
It is a field where Minecraft evolved a lot, inventories are now used a lot as client<->server interface with clickable items and callback, we support it natively without the need of programming your solution.

## Commands
Commands are the simplest way of communication between clients and server. Since 1.13 Minecraft has incorporated a new library denominated "Brigadier", we then integrated an API meant to use the full potential of args types.

# Contributing
See [the contributing file](CONTRIBUTING.md)!  
All planned features are listed on [Trello](https://trello.com/b/4ysvj5hT/minestom)

# License
This project is licensed under the [Apache License Version 2.0](../LICENSE).


