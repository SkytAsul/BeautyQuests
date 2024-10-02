package fr.skytasul.quests.rewards;

import fr.skytasul.quests.api.editors.WaitClick;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.npc.NpcCreateGUI;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.types.BQLocation;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;

public class TeleportationReward extends AbstractReward {

	public Location teleportation;

	public TeleportationReward() {}

	public TeleportationReward(String customDescription, Location teleportation) {
		super(customDescription);
		this.teleportation = teleportation;
	}

	@Override
	public List<String> give(Player player) {
		QuestUtils.runOrSync(() -> player.teleport(teleportation));
		return null;
	}

	@Override
	public AbstractReward clone() {
		return new TeleportationReward(getCustomDescription(), teleportation.clone());
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder
				.addDescriptionAsValue(Lang.Location.format(teleportation == null ? null : BQLocation.of(teleportation)));
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.MOVE_TELEPORT_POINT.send(event.getPlayer());
		new WaitClick(event.getPlayer(), event::cancel, NpcCreateGUI.validMove.clone(), () -> {
			teleportation = event.getPlayer().getLocation();
			event.reopenGUI();
		}).start();
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("tp", teleportation.serialize());
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		teleportation = Location.deserialize(section.getConfigurationSection("tp").getValues(false));
	}

}
