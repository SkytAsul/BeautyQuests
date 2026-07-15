---
title: Internal placeholders
description: List of the internal placeholders
---

The plugin has an internal placeholders system: it allows to dynamically include text in other texts, for instance the player name, the remaining amount of blocks to mine, etc.

The placeholders can be used in stage description messages, in quest descriptions... practically in all in-game messages.

Most of the placeholders are **contextual**: it means they can only be used in a certain context (e.g. a stage, an NPC message...) and that the value they will be replaced with will depend on the context. For instance, the placeholder that shows the remaining actions for a stage is only available in the context of a stage description (shown in scoreboard, in the quest menu, etc.)

## Placeholders format
All placeholders are usable with the `{key}` format, where `key` is the placeholder itself. To say it literally, you have to write the placeholder enclosed in curly braces.

***

## Placeholders list
:::note
This list is not exhaustive.
:::

### Default
Those placeholders are present in all contexts.
* `prefix`: the prefix of the plugin (configurable in the locale file) if the `enablePrefix` config option is enabled, `§6` otherwise
* `nl`: new line (line break)

### Questers
Those placeholders are present in almost all context, except for server-wide texts.
* `quester_name`: the friendly name of the quester (e.g. the player name)
* `quester_display_name`: the display name of the quester. Most of the time identical to `quester_name`.
* `quester_identifier`: the unique identifier of the quester
* `quester_detailed_name`: a detailed name describing the quester
If the quester is an actual player, the following placeholders will also work:
* `player`: the name of the player (identical to `quester_name`)
* `PLAYER`: same as `player` (legacy)
* `player_display_name`: the display name of the player (identical to `quester_display_name`)

### Quests
Those placeholders are present in all contexts that are linked to a quest.
* `quest`: name and id of the quest, in the format `name (#id)`
* `quest_name`
* `quest_id`
* `quest_description`: description of the quest, or nothing if not defined

### NPCs
* `npc_name`
* `npc_id`

### Dialog messages
* _all placeholders from [NPCs](#npcs)_
* `player_name`: name of the player in the dialog
* `npc_name_message`: name of the NPC _in the context of this dialog_ (the name can be changed in the dialog editor)
* `message_count`: total amount of messages in the dialog
* `message_id`: number of the message (ranging from 1 to `message_count` included)

### Stages
All contexts linked to a stage have the following placeholders available:
* _all placeholders from [Quests](#quests)_
* `stage_type`: the type of stage (kill mobs, break blocks, etc.)
* `stage_rewards`: amount of rewards of the stage
* `stage_requirements`: amount of requirements of the stage

On top of that, each stage has a specific set of placeholders available so the advancement and settings of each stage are available.

Furthermore, since a lot of stages share a similar structure (e.g. do some action on a list of mobs), there are a few shared list of placeholders:

#### _Shared between Entity stages_
* `mobs`: preformatted information about the entities, following the `stage description.item formats` section in `config.yml`
* `mobs_remaining`: amount of mobs remaining for the action
* `mobs_done`: amount of mobs for which the action have already been done
* `mobs_total`: total amount of mobs for the action
* `mobs_percentage`: percentage of mobs done / total mobs
* `mobs_name`: name of the mobs for the action

#### _Shared between stages with multiple `<object_type>`_
* `<object_type>`: preformatted information about the objects, following the `stage description.item formats` section in config.yml
* `<object_type>_remaining|done|total|percentage|name`: informations about the objects, see "Entity stage"
* `<object_type>_<id>_remaining|done|total|percentage|name`: informations about the `id`-th object, see "Entity stage"

#### Breed animals
* _all placeholders from [Entity stages](#shared-between-entity-stages)_

#### Tame animals
* _all placeholders from [Entity stages](#shared-between-entity-stages)_

#### Talk to NPC
* _all placeholders from [NPCs](#npcs)_
* `dialog_npc_name`: name of the NPC. Either the real NPC's name, or the custom one if set in the dialog editor.

#### Bring items to NPC
* _all placeholders from [Talk to NPC](#talk-to-npc)_
* `items`: formatted list of items, following the `stage description` section in `config.yml`

#### Fill buckets
* `bucket_type`: name of the bucket to fill
* `buckets`, `buckets_remaining|done|total|percentage|name`: informations about the buckets, see "Entity stage"

#### Write in chat
* `text`: text to write in the chat

#### Craft items
* `items`, `items_remaining|done|total|percentage|name`: informations about the items to craft, see "Entity stage"

#### Deal damage
* `damage_remaining|done|total|percentage`: informations about the damage, see "Entity stage"
* `target_mobs`: list of mobs applicable for this stage

#### Kill mobs
* _all placeholders from [Stages with multiple `mobs`](#shared-between-stages-with-multiple-object_type)_

#### Break blocks
* _all placeholders from [Stages with multiple `blocks`](#shared-between-stages-with-multiple-object_type)_

#### Place blocks
* _all placeholders from [Stages with multiple `blocks`](#shared-between-stages-with-multiple-object_type)_

#### Melt items
* _all placeholders from [Stages with multiple `items`](#shared-between-stages-with-multiple-object_type)_

#### Fish items
* _all placeholders from [Stages with multiple `items`](#shared-between-stages-with-multiple-object_type)_

#### Consume/eat/drink items
* _all placeholders from [Stages with multiple `items`](#shared-between-stages-with-multiple-object_type)_

#### Enchant items
* _all placeholders from [Stages with multiple `items`](#shared-between-stages-with-multiple-object_type)_

#### Play some time
* `time_remaining_human`: a human-readable string of the time left (e.g. "2 hours and 3 minutes")
* `time_remaining|done|total|percentage`: information about the time in milliseconds, see "Entity stage"

#### Interact with block
* `block`: pretty name of the block to interact with
* `block_type`: internal name of the block
* `block_material`: name of a real material associated with this block (some blocks can have multiple materials, e.g. for a tag)

#### Interact at location
* `x`, `y`, `z`: integer coordinates of the location to interact with
* `world_name`: the name of the world to interact in, if it is defined
* `world_pattern`: the regular expression describing in which world the interaction is allowed, if it is defined
* `world`:  either the name or the regular expression for the world, depending on which is defined

<!--- Todo continue --->
