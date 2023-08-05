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
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.gui.misc.DamageCausesGUI;

public class StageDeath extends AbstractStage {
	
	private List<DamageCause> causes;
	
	public StageDeath(StageController controller, List<DamageCause> causes) {
		super(controller);
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
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_DIE.toString();
	}
	
	@Override
	protected void serialize(ConfigurationSection section) {
		if (!causes.isEmpty()) section.set("causes", causes.stream().map(DamageCause::name).collect(Collectors.toList()));
	}
	
	public static StageDeath deserialize(ConfigurationSection section, StageController controller) {
		List<DamageCause> causes;
		if (section.contains("causes")) {
			causes = section.getStringList("causes").stream().map(DamageCause::valueOf).collect(Collectors.toList());
		}else {
			causes = Collections.emptyList();
		}
		return new StageDeath(controller, causes);
	}
	
	public static class Creator extends StageCreation<StageDeath> {
		
		private static final int CAUSES_SLOT = 7;
		
		private List<DamageCause> causes;
		
		public Creator(@NotNull StageCreationContext<StageDeath> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			
			line.setItem(CAUSES_SLOT, ItemUtils.item(XMaterial.SKELETON_SKULL, Lang.stageDeathCauses.toString()), event -> {
				new DamageCausesGUI(causes, newCauses -> {
					setCauses(newCauses);
					event.reopen();
				}).open(event.getPlayer());
			});
		}
		
		public void setCauses(List<DamageCause> causes) {
			this.causes = causes;
			getLine().refreshItem(CAUSES_SLOT,
					item -> ItemUtils.lore(item, QuestOption.formatNullableValue(causes.isEmpty() ? Lang.stageDeathCauseAny
							: Lang.stageDeathCausesSet.quickFormat("causes_amount", causes.size()))));
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
		protected StageDeath finishStage(StageController controller) {
			return new StageDeath(controller, causes);
		}
		
	}
	
}
