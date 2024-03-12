package fr.skytasul.quests.stages;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.types.AbstractEntityStage;
import fr.skytasul.quests.api.utils.XMaterial;

public class StageBreed extends AbstractEntityStage implements Listener {

	private static final List<Material> EGG_MATERIALS =
			Arrays.asList(XMaterial.TURTLE_EGG, XMaterial.FROGSPAWN)
			.stream()
			.filter(XMaterial::isSupported)
			.map(XMaterial::parseMaterial)
			.collect(Collectors.toList());

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

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if (!EGG_MATERIALS.contains(e.getBlock().getType()))
			return;
		if (!(e.getEntity() instanceof Animals))
			return;
		Animals entity = (Animals) e.getEntity();
		Player breeder = Bukkit.getPlayer(entity.getBreedCause());
		if (breeder != null)
			event(breeder, e.getEntityType());
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
