![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/SkytAsul/BeautyQuests?color=green)
[![Jenkins](https://ci.codemc.org/job/SkytAsul/job/BeautyQuests/badge/icon)](https://ci.codemc.org/job/SkytAsul/job/BeautyQuests)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/beautyquests/localized.svg)](https://crowdin.com/project/beautyquests)
[![Spiget Downloads](https://img.shields.io/spiget/downloads/39255)](https://www.spigotmc.org/resources/beautyquests.39255/)
![bStats Servers](https://img.shields.io/bstats/servers/7460)
![bStats Players](https://img.shields.io/bstats/players/7460)
[![Discord](https://ptb.discordapp.com/api/guilds/482632781395132416/widget.png)](https://discord.gg/H8fXrkD)
[![Gitmoji](https://img.shields.io/badge/gitmoji-%20😜%20😍-FFDD67.svg?style=flat-square)](https://gitmoji.dev)

# BeautyQuests
What is **BeautyQuests**?
* BeautyQuests is a Minecraft quest plugin based on a simple-to-use graphical interface.
* It's an absolutely free plugin with a full technical documentation.
* It's also a collaborative work, where everyone can improve and add new features. If you have ideas, submit them and I or somebody else will try to implement them.
* It's compatible with a large number of other plugins (like MythicMobs, Jobs, Vault, ...).

***

## Overview
BeautyQuests is a plugin that allows you to create and manage quests very easy with a GUI created with the in-game inventories.
This plugin is elaborate to look pretty and to simplify the quests for the players.

***

## Downloads
Plugin releases can be downloaded from **[SpigotMC](https://www.spigotmc.org/resources/beautyquests.39255)**.

Development builds are available on **[Jenkins](https://ci.codemc.org/job/SkytAsul/job/BeautyQuests/)**.

***

## Wiki
You can find the **wiki [here](https://github.com/SkytAsul/BeautyQuests/wiki)**.

***

## Crowdin
If you want to help us translate BeautyQuests into your native language, join the [official BeautyQuests Crowdin project](https://crowdin.com/project/beautyquests) and let players in-game see your translations (read the project description for more informations).

***

## Discord
If you need help, found a bug, or want to share an idea, you can join the official [Plugin Support Discord server by SkytAsul](https://discord.gg/H8fXrkD).

***

## Maven
In *pom.xml*, add this to the `repositories` section:
```xml
<repository>
  <id>codemc</id>
  <url>https://repo.codemc.org/repository/maven-public</url>
</repository>
```
And add this to the `dependencies` section: (replace VERSION by whatever version you want, i.e. `0.19.7`, `0.20-SNAPSHOT`...)
```xml
<dependency>
  <groupId>fr.skytasul</groupId>
  <artifactId>beautyquests-core</artifactId>
  <version>VERSION</version>
  <scope>provided</scope>
</dependency>
```
