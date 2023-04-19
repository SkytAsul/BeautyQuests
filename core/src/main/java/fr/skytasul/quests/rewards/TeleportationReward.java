package fr.skytasul.quests.rewards;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.gui.npc.NPCGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class TeleportationReward extends AbstractReward {

	public Location teleportation;

	public TeleportationReward() {}
	
	public TeleportationReward(String customDescription, Location teleportation) {
		super(customDescription);
		this.teleportation = teleportation;
	}

	@Override
	public List<String> give(Player p) {
		Utils.runOrSync(() -> p.teleport(teleportation));
		return null;
	}

	@Override
	public AbstractReward clone() {
		return new TeleportationReward(getCustomDescription(), teleportation.clone());
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescriptionAsValue(Utils.locationToString(teleportation));
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.MOVE_TELEPORT_POINT.send(event.getPlayer());
		new WaitClick(event.getPlayer(), event::cancel, NPCGUI.validMove.clone(), () -> {
			teleportation = event.getPlayer().getLocation();
			event.reopenGUI();
		}).enter();
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("tp", teleportation.serialize());
	}
	
	@Override
	public void load(ConfigurationSection section) {
		teleportation = Location.deserialize(section.getConfigurationSection("tp").getValues(false));
	}
	
}
