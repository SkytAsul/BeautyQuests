package fr.skytasul.quests.rewards;

import java.util.List;

import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.energie.utils.EntityUtils;
import fr.skytasul.quests.api.QuestsPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.editors.WaitClick;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.npc.NpcCreateGUI;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.types.BQLocation;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportationReward extends AbstractReward {

	public Location teleportation;

	public TeleportationReward() {}

	public TeleportationReward(String customDescription, Location teleportation) {
		super(customDescription);
		this.teleportation = teleportation;
	}

	@Override
	public List<String> give(Player player) {
		QuestsPlugin.getPlugin().getScheduler().runTask(SchedulerType.SYNC, player, __ -> {
			EntityUtils.teleportAsync(player, teleportation, PlayerTeleportEvent.TeleportCause.PLUGIN);
		}, null);
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
				.addDescriptionAsValue(Lang.Location.format(teleportation == null ? null : new BQLocation(teleportation)));
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
