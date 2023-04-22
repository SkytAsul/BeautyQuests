package fr.skytasul.quests.api.stages.types;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.Utils;

@LocatableType (types = LocatedType.ENTITY)
public abstract class AbstractEntityStage extends AbstractStage implements Locatable.MultipleLocatable {
	
	protected final @NotNull EntityType entity;
	protected final int amount;

	protected AbstractEntityStage(@NotNull QuestBranch branch, @NotNull EntityType entity, int amount) {
		super(branch);
		this.entity = entity;
		this.amount = amount;
	}
	
	protected void event(@NotNull Player p, @NotNull EntityType type) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this) && canUpdate(p)) {
			if (entity == null || type.equals(entity)) {
				Integer playerAmount = getPlayerAmount(acc);
				if (playerAmount == null) {
					BeautyQuests.logger.warning(p.getName() + " does not have object datas for stage " + toString() + ". This is a bug!");
				}else if (playerAmount.intValue() <= 1) {
					finishStage(p);
				}else {
					updateObjective(acc, p, "amount", playerAmount.intValue() - 1);
				}
			}
		}
	}
	
	protected @Nullable Integer getPlayerAmount(@NotNull PlayerAccount acc) {
		return getData(acc, "amount");
	}
	
	@Override
	protected void initPlayerDatas(@NotNull PlayerAccount acc, @NotNull Map<@NotNull String, @Nullable Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("amount", amount);
	}
	
	@Override
	protected void serialize(@NotNull ConfigurationSection section) {
		section.set("entityType", entity == null ? "any" : entity.name());
		section.set("amount", amount);
	}
	
	protected @NotNull String getMobsLeft(@NotNull PlayerAccount acc) {
		Integer playerAmount = getPlayerAmount(acc);
		if (playerAmount == null) return "§cerror: no datas";
		
		return Utils.getStringFromNameAndAmount(entity == null ? Lang.EntityTypeAny.toString() : MinecraftNames.getEntityName(entity), QuestsConfiguration.getItemAmountColor(), playerAmount, amount, false);
	}
	
	@Override
	protected @NotNull Supplier<Object> @NotNull [] descriptionFormat(@NotNull PlayerAccount acc, @NotNull Source source) {
		return new Supplier[] { () -> getMobsLeft(acc) };
	}
	
	@Override
	public boolean canBeFetchedAsynchronously() {
		return false;
	}
	
	@Override
	public @NotNull Spliterator<@NotNull Located> getNearbyLocated(@NotNull NearbyFetcher fetcher) {
		if (!fetcher.isTargeting(LocatedType.ENTITY)) return Spliterators.emptySpliterator();
		return fetcher.getCenter().getWorld()
				.getEntitiesByClass(entity.getEntityClass())
				.stream()
				.map(x -> {
					double ds = x.getLocation().distanceSquared(fetcher.getCenter());
					if (ds > fetcher.getMaxDistanceSquared()) return null;
					return new AbstractMap.SimpleEntry<>(x, ds);
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(Entry::getValue))
				.<Located>map(entry -> Located.LocatedEntity.create(entry.getKey()))
				.spliterator();
	}
	
	public abstract static class AbstractCreator<T extends AbstractEntityStage> extends StageCreation<T> {
		
		protected EntityType entity = null;
		protected int amount = 1;
		
		protected AbstractCreator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(6, ItemUtils.item(XMaterial.CHICKEN_SPAWN_EGG, Lang.changeEntityType.toString()), (p, item) -> {
				new EntityTypeGUI(x -> {
					setEntity(x);
					reopenGUI(p, true);
				}, x -> x == null ? canBeAnyEntity() : canUseEntity(x)).create(p);
			});
			
			line.setItem(7, ItemUtils.item(XMaterial.REDSTONE, Lang.Amount.format(1)), (p, item) -> {
				new TextEditor<>(p, () -> {
					reopenGUI(p, false);
				}, x -> {
					setAmount(x);
					reopenGUI(p, false);
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			});
		}
		
		public void setEntity(EntityType entity) {
			this.entity = entity;
			line.editItem(6, ItemUtils.lore(line.getItem(6), Lang.optionValue.format(entity == null ? Lang.EntityTypeAny.toString() : entity.name())));
		}
		
		public void setAmount(int amount) {
			this.amount = amount;
			line.editItem(7, ItemUtils.name(line.getItem(7), Lang.Amount.format(amount)));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			setEntity(null);
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setEntity(stage.entity);
			setAmount(stage.amount);
		}
		
		protected boolean canBeAnyEntity() {
			return true;
		}
		
		protected abstract boolean canUseEntity(@NotNull EntityType type);
		
	}
	
}