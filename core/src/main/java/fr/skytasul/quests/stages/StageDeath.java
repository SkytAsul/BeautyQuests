package fr.skytasul.quests.stages;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.misc.DamageCausesGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class StageDeath extends AbstractStage {
	
	private List<DamageCause> causes;
	
	public StageDeath(QuestBranch branch, List<DamageCause> causes) {
		super(branch);
		this.causes = causes;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		if (!hasStarted(p)) return;
		
		if (!causes.isEmpty()) {
			EntityDamageEvent lastDamage = p.getLastDamageCause();
			if (lastDamage == null) return;
			if (!causes.contains(lastDamage.getCause())) return;
		}
		
		if (canUpdate(p, true)) finishStage(p);
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_DIE.toString();
	}
	
	@Override
	protected void serialize(ConfigurationSection section) {
		if (!causes.isEmpty()) section.set("causes", causes.stream().map(DamageCause::name).collect(Collectors.toList()));
	}
	
	public static StageDeath deserialize(ConfigurationSection section, QuestBranch branch) {
		List<DamageCause> causes;
		if (section.contains("causes")) {
			causes = section.getStringList("causes").stream().map(DamageCause::valueOf).collect(Collectors.toList());
		}else {
			causes = Collections.emptyList();
		}
		return new StageDeath(branch, causes);
	}
	
	public static class Creator extends StageCreation<StageDeath> {
		
		private static final int CAUSES_SLOT = 7;
		
		private List<DamageCause> causes;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(CAUSES_SLOT, ItemUtils.item(XMaterial.SKELETON_SKULL, Lang.stageDeathCauses.toString()), (p, item) -> {
				new DamageCausesGUI(causes, newCauses -> {
					setCauses(newCauses);
					reopenGUI(p, true);
				}).create(p);
			});
		}
		
		public void setCauses(List<DamageCause> causes) {
			this.causes = causes;
			line.editItem(CAUSES_SLOT, ItemUtils.lore(line.getItem(CAUSES_SLOT), Lang.optionValue.format(causes.isEmpty() ? Lang.stageDeathCauseAny : Lang.stageDeathCausesSet.format(causes.size()))));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			setCauses(Collections.emptyList());
		}
		
		@Override
		public void edit(StageDeath stage) {
			super.edit(stage);
			setCauses(stage.causes);
		}
		
		@Override
		protected StageDeath finishStage(QuestBranch branch) {
			return new StageDeath(branch, causes);
		}
		
	}
	
}
