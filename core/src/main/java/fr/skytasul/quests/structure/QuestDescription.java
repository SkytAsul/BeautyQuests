package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.options.OptionEndRewards;
import fr.skytasul.quests.options.OptionRequirements;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class QuestDescription {
	
	private boolean requirements;
	private String requirementsValid;
	private String requirementsInvalid;
	
	private boolean rewards;
	private String rewardsFormat;
	
	public QuestDescription(ConfigurationSection config) {
		requirements = config.getBoolean("requirements.display");
		requirementsValid = config.getString("requirements.valid");
		requirementsInvalid = config.getString("requirements.invalid");
		
		rewards = config.getBoolean("rewards.display");
		rewardsFormat = config.getString("rewards.format");
	}
	
	public List<String> formatDescription(Quest quest, Player p) {
		List<String> list = new ArrayList<>();
		
		String desc = quest.getDescription();
		if (desc != null) list.add("§7" + desc);
		
		if (p == null) return list;
		
		if (this.rewards) {
			List<String> rewards = quest.getOptionValueOrDef(OptionEndRewards.class).stream().map(x -> x.getDescription(p)).filter(Objects::nonNull).map(x -> Utils.format(rewardsFormat, x)).collect(Collectors.toList());
			if (!rewards.isEmpty()) {
				if (!list.isEmpty()) list.add("");
				list.add(Lang.RWDTitle.toString());
				list.addAll(rewards);
			}
		}
		
		if (this.requirements) {
			List<String> requirements = quest.getOptionValueOrDef(OptionRequirements.class).stream().map(x -> {
				String description = x.getDescription(p);
				if (description != null) description = Utils.format(x.test(p) ? requirementsValid : requirementsInvalid, description);
				return description;
			}).filter(Objects::nonNull).collect(Collectors.toList());
			if (!requirements.isEmpty()) {
				if (!list.isEmpty()) list.add("");
				list.add(Lang.RDTitle.toString());
				list.addAll(requirements);
			}
		}
		
		return list;
	}
	
}
