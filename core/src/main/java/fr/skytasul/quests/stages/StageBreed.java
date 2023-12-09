package fr.skytasul.quests.stages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.types.AbstractEntityStage;

public class StageBreed extends AbstractEntityStage implements Listener {

	public StageBreed(StageController controller, EntityType entity, int amount) {
		super(controller, entity, amount);
	}

	@EventHandler
	public void onBreed(EntityBreedEvent e) {
		if (e.getBreeder() instanceof Player) {
			Player p = (Player) e.getBreeder();
			event(p, e.getEntityType());
		}
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_BREED.toString();
	}

	public static StageBreed deserialize(ConfigurationSection section, StageController controller) {
		String type = section.getString("entityType");
		return new StageBreed(controller, "any".equals(type) ? null : EntityType.valueOf(type), section.getInt("amount"));
	}

	public static class Creator extends AbstractEntityStage.AbstractCreator<StageBreed> {

		public Creator(@NotNull StageCreationContext<StageBreed> context) {
			super(context);
		}

		@Override
		protected boolean canUseEntity(EntityType type) {
			return Breedable.class.isAssignableFrom(type.getEntityClass());
		}

		@Override
		protected StageBreed finishStage(StageController controller) {
			return new StageBreed(controller, entity, amount);
		}

	}

}
