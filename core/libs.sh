#!/bin/sh

if [ "$#" -ne 2 ]; then
    echo -e "\e[91mRequired syntax: ./libs.sh <maven binary path> <jars directory>"
    exit 1
fi

mavenPath=$1
jarsPath=$2
echo -e "\e[93mInstalling BeautyQuests dependencies jars from \"$jarsPath\" to the local repository...\e[39m"

$mavenPath install:install-file -Dfile=$jarsPath/dynmap.jar -DgroupId=org.dynmap -DartifactId=dynmap -Dversion=1.0 -Dpackaging=jar
$mavenPath install:install-file -Dfile=$jarsPath/Factions.jar -DgroupId=com.massivecraft -DartifactId=factions -Dversion=1.0 -Dpackaging=jar
$mavenPath install:install-file -Dfile=$jarsPath/MassiveCore.jar -DgroupId=com.massivecraft -DartifactId=massivecore -Dversion=1.0 -Dpackaging=jar
$mavenPath install:install-file -Dfile=$jarsPath/GPS.jar -DgroupId=com.live.bemmamin -DartifactId=gps -Dversion=1.0 -Dpackaging=jar
$mavenPath install:install-file -Dfile=$jarsPath/Jobs.jar -DgroupId=com.gamingmesh -DartifactId=jobs -Dversion=1.0 -Dpackaging=jar
$mavenPath install:install-file -Dfile=$jarsPath/McCombatLevel.jar -DgroupId=com.gmail.mrphpfan -DartifactId=mccombatlevel -Dversion=1.0 -Dpackaging=jar
$mavenPath install:install-file -Dfile=$jarsPath/mcMMO.jar -DgroupId=com.gmail.nossr50 -DartifactId=mcmmo -Dversion=1.0 -Dpackaging=jar
$mavenPath install:install-file -Dfile=$jarsPath/SkillAPI.jar -DgroupId=com.suxy -DartifactId=skillapi -Dversion=1.0 -Dpackaging=jar
$mavenPath install:install-file -Dfile=$jarsPath/BossAPI.jar -DgroupId=com.suxy -DartifactId=boss -Dversion=1.0 -Dpackaging=jar

echo -e "\e[92mOperation complete."