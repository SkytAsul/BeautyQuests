package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.entity.Breedable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;

import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;

public class StageBreed extends AbstractEntityStage {
	
	public StageBreed(QuestBranch branch, EntityType entity, int amount) {
		super(branch, entity, amount);
	}
	
	@EventHandler
	public void onBreed(EntityBreedEvent e) {
		if (e.getBreeder() instanceof Player) {
			Player p = (Player) e.getBreeder();
			event(p, e.getEntityType());
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_BREED.format(getMobsLeft(acc));
	}

	public static StageBreed deserialize(Map<String, Object> map, QuestBranch branch) {
		String type = (String) map.get("entityType");
		return new StageBreed(branch, "any".equals(type) ? null : EntityType.valueOf(type), (int) map.get("amount"));
	}
	
	public static class Creator extends AbstractEntityStage.AbstractCreator<StageBreed> {
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
		}
		
		@Override
		protected boolean canUseEntity(EntityType type) {
			return Breedable.class.isAssignableFrom(type.getEntityClass());
		}
		
		@Override
		protected StageBreed finishStage(QuestBranch branch) {
			return new StageBreed(branch, entity, amount);
		}
		
	}
	
}
