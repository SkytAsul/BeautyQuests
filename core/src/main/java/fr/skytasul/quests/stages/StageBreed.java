package fr.skytasul.quests.stages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.types.AbstractEntityStage;
import fr.skytasul.quests.gui.creation.stages.Line;

public class StageBreed extends AbstractEntityStage {
	
	public StageBreed(StageController controller, EntityType entity, int amount) {
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
	protected String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return Lang.SCOREBOARD_BREED.format(getMobsLeft(acc));
	}

	public static StageBreed deserialize(ConfigurationSection section, StageController controller) {
		String type = section.getString("entityType");
		return new StageBreed(branch, "any".equals(type) ? null : EntityType.valueOf(type), section.getInt("amount"));
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
		protected StageBreed finishStage(StageController controller) {
			return new StageBreed(branch, entity, amount);
		}
		
	}
	
}
