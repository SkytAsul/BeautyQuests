package fr.skytasul.quests.api.stages.types;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Spliterator;
import java.util.Spliterators;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.MinecraftNames;
import fr.skytasul.quests.api.utils.itemdescription.HasItemsDescriptionConfiguration.HasSingleObject;
import fr.skytasul.quests.api.utils.itemdescription.ItemsDescriptionPlaceholders;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

@LocatableType (types = LocatedType.ENTITY)
public abstract class AbstractEntityStage extends AbstractStage implements Locatable.MultipleLocatable, HasSingleObject {
	
	protected final @NotNull EntityType entity;
	protected final int amount;

	protected AbstractEntityStage(@NotNull StageController controller, @NotNull EntityType entity, int amount) {
		super(controller);
		this.entity = entity;
		this.amount = amount;
	}
	
	protected void event(@NotNull Player p, @NotNull EntityType type) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (hasStarted(p) && canUpdate(p)) {
			if (entity == null || type.equals(entity)) {
				OptionalInt playerAmount = getPlayerAmountOptional(acc);
				if (!playerAmount.isPresent()) {
					QuestsPlugin.getPlugin().getLoggerExpanded().warning(p.getName() + " does not have object datas for stage " + toString() + ". This is a bug!");
				} else if (playerAmount.getAsInt() <= 1) {
					finishStage(p);
				}else {
					updateObjective(p, "amount", playerAmount.getAsInt() - 1);
				}
			}
		}
	}
	
	protected @NotNull OptionalInt getPlayerAmountOptional(@NotNull PlayerAccount acc) {
		Integer amount = getData(acc, "amount");
		return amount == null ? OptionalInt.empty() : OptionalInt.of(amount.intValue());
	}
	
	@Override
	public int getPlayerAmount(@NotNull PlayerAccount account) {
		return getPlayerAmountOptional(account).orElse(0);
	}

	@Override
	public int getObjectAmount() {
		return amount;
	}

	@Override
	public @NotNull String getObjectName() {
		return entity == null ? Lang.EntityTypeAny.toString() : MinecraftNames.getEntityName(entity);
	}

	@Override
	public void initPlayerDatas(@NotNull PlayerAccount acc, @NotNull Map<@NotNull String, @Nullable Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("amount", amount);
	}
	
	@Override
	protected void serialize(@NotNull ConfigurationSection section) {
		section.set("entityType", entity == null ? "any" : entity.name());
		section.set("amount", amount);
	}
	
	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		ItemsDescriptionPlaceholders.register(placeholders, "mobs", this);
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
		
		protected AbstractCreator(@NotNull StageCreationContext<T> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			
			line.setItem(6, ItemUtils.item(XMaterial.CHICKEN_SPAWN_EGG, Lang.changeEntityType.toString()), event -> {
				new EntityTypeGUI(x -> {
					setEntity(x);
					event.reopen();
				}, x -> x == null ? canBeAnyEntity() : canUseEntity(x)).open(event.getPlayer());
			});
			
			line.setItem(7, ItemUtils.item(XMaterial.REDSTONE, Lang.Amount.format(1)), event -> {
				new TextEditor<>(event.getPlayer(), event::reopen, x -> {
					setAmount(x);
					event.reopen();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			});
		}
		
		public void setEntity(EntityType entity) {
			this.entity = entity;
			getLine().refreshItemLore(6,
					Lang.optionValue.format(entity == null ? Lang.EntityTypeAny.toString() : entity.name()));
		}
		
		public void setAmount(int amount) {
			this.amount = amount;
			getLine().refreshItemName(7, Lang.Amount.format(amount));
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