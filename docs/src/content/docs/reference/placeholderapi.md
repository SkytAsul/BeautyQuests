---
title: PlaceholderAPI
description: List of available placeholders for PlaceholderAPI
---

BeautyQuests automatically registers some placeholders when PlaceholderAPI is detected on your server. The following is an incomplete list, run `/papi info beautyquests`.

- `%beautyquests_total_amount%` to show the total amount of quests
- `%beautyquests_player_inprogress_amount%` to show the amount of quests the player is into
- `%beautyquests_player_finished_amount%` to show the amount of quests the player has finished
- `%beautyquests_player_finished_total_amount%` to show the amount of quests the player has finished, and sums up quests which has been completed multiple times
- `%beautyquests_started_ordered%` to show the advancement of each quest, one after another. You can configure the time to change in the config file.
- `%beautyquests_started_ordered_X%` to show the advancement of each quest but split into multiple lines customizable in [config.yml​](/guides/configuration)
- `%beautyquests_advancement_X%` to show the advancement of the quest with ID **X**
- `%beautyquests_advancement_X_raw%` to show the advancement of the quest with ID **X**, but only with the form on one number:
  - **-1** if the player has not started the quest
  - **-2** if the player is in an ending stage (i.e. in branching)
≥0 the stage ID, if the player is currently doing the quest​
- `%beautyquests_player_quest_finished_X%` to show amount of times the quest with ID **X** has been finished
- `%beautyquests_started_id_list%` which returns a list of quests currently in progress for player. The list is an enumeration of IDs, separated with `;`
- ... and more, use `/papi info beautyquests`