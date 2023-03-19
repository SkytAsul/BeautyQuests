#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo -e "\e[91mRequired syntax: ./libs.sh <maven binary path> <jars directory>"
    exit 1
fi

mavenPath=$1
jarsPath=$2

echo -e "\e[93mInstalling BeautyQuests dependencies jars from \"$jarsPath\" to the local repository..."

echo -e "Maven path: $mavenPath\e[39m"

"$mavenPath" install:install-file -Dfile=$jarsPath/dynmap.jar -DgroupId=org.dynmap -DartifactId=dynmap -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/Factions.jar -DgroupId=com.massivecraft -DartifactId=factions -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/MassiveCore.jar -DgroupId=com.massivecraft -DartifactId=massivecore -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/GPS.jar -DgroupId=com.live.bemmamin -DartifactId=gps -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/Jobs.jar -DgroupId=com.gamingmesh -DartifactId=jobs -Dversion=5.1.0.1 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/McCombatLevel.jar -DgroupId=com.gmail.mrphpfan -DartifactId=mccombatlevel -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/mcMMO.jar -DgroupId=com.gmail.nossr50 -DartifactId=mcmmo -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/SkillAPI.jar -DgroupId=com.suxy -DartifactId=skillapi -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/Boss.jar -DgroupId=org.mineacademy -DartifactId=boss -Dversion=4.2.1 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/CMI.jar -DgroupId=com.zrips -DartifactId=cmi -Dversion=9.0.2.1 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/CMILib.jar -DgroupId=com.zrips -DartifactId=cmilib -Dversion=1.2.3.3 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/UltimateTimber.jar -DgroupId=com.songoda -DartifactId=UltimateTimber -Dversion=2.3.5 -Dpackaging=jar -DgeneratePom=true
"$mavenPath" install:install-file -Dfile=$jarsPath/AdvancedSpawners-API.jar -DgroupId=gcspawners -DartifactId=gcspawners -Dversion=3.3.0 -Dpackaging=jar -DgeneratePom=true
#"$mavenPath" install:install-file -Dfile=$jarsPath/MythicMobs.jar -DgroupId=io.lumine.xikage -DartifactId=MythicMobs -Dversion=4.12.0 -Dpackaging=jar -DgeneratePom=true
#"$mavenPath" install:install-file -Dfile=$jarsPath/TokenEnchantAPI.jar -DgroupId=com.vk2gpz.tokenenchant -DartifactId=TokenEnchantAPI -Dversion=18.15.2 -Dpackaging=jar -DgeneratePom=true

echo -e "\e[92mOperation complete."