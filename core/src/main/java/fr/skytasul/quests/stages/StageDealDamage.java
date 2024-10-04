package fr.skytasul.quests.stages;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.progress.HasProgress;
import fr.skytasul.quests.api.utils.progress.ProgressPlaceholders;
import fr.skytasul.quests.gui.mobs.MobSelectionGUI;
import fr.skytasul.quests.mobs.Mob;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings ("rawtypes")
public class StageDealDamage extends AbstractStage implements HasProgress, Listener {

	private final double damage;
	private final List<Mob> targetMobs;

	public StageDealDamage(StageController controller, double damage, List<Mob> targetMobs) {
		super(controller);
		this.damage = damage;
		this.targetMobs = targetMobs;
	}

	@Override
	public void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("amount", damage);
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent event) {
		Player player;
		if (event.getDamager() instanceof Projectile) {
			ProjectileSource projectileShooter = ((Projectile) event.getDamager()).getShooter();
			if (!(projectileShooter instanceof Player)) return;
			player = (Player) projectileShooter;
		}else if (event.getDamager() instanceof Player) {
			player = (Player) event.getDamager();
		}else return;

		if (targetMobs != null && !targetMobs.isEmpty()
				&& targetMobs.stream().noneMatch(mob -> mob.appliesEntity(event.getEntity()))) return;

		PlayerAccount account = PlayersManager.getPlayerAccount(player);

		if (!hasStarted(player) || !canUpdate(player))
			return;

		double amount = getData(account, "amount");
		amount -= event.getFinalDamage();
		if (amount <= 0) {
			finishStage(player);
		}else {
			updateObjective(player, "amount", amount);
		}
	}

	public double getPlayerAmountDouble(@NotNull PlayerAccount account) {
		return getData(account, "amount", Double.class);
	}

	@Override
	public long getPlayerAmount(@NotNull PlayerAccount account) {
		return (long) Math.ceil(getPlayerAmountDouble(account));
	}

	@Override
	public long getTotalAmount() {
		return (long) damage;
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		ProgressPlaceholders.registerProgress(placeholders, "damage", this);
		placeholders.registerIndexedContextual("damage_remaining", StageDescriptionPlaceholdersContext.class,
				context -> Long.toString(getPlayerAmount(context.getPlayerAccount())));
		placeholders.registerIndexed("target_mobs", getTargetMobsString(targetMobs));
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return targetMobs == null || targetMobs.isEmpty() ? Lang.SCOREBOARD_DEAL_DAMAGE_ANY.toString()
				: Lang.SCOREBOARD_DEAL_DAMAGE_MOBS.toString();
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("damage", damage);
		if (targetMobs != null && !targetMobs.isEmpty())
			section.set("targetMobs", targetMobs.stream().map(Mob::serialize).collect(Collectors.toList()));
	}

	public static StageDealDamage deserialize(ConfigurationSection section, StageController controller) {
		return new StageDealDamage(controller,
				section.getDouble("damage"),
				section.contains("targetMobs") ? section.getMapList("targetMobs").stream().map(map -> Mob.deserialize((Map) map)).collect(Collectors.toList()) : null);
	}

	private static String getTargetMobsString(List<Mob> targetMobs) {
		if (targetMobs == null || targetMobs.isEmpty())
			return Lang.EntityTypeAny.toString();
		return targetMobs.stream().map(Mob::getName).collect(Collectors.joining(", "));
	}

	public static class Creator extends StageCreation<StageDealDamage> {

		private static final int SLOT_DAMAGE = 6;
		private static final int SLOT_MOBS = 7;

		private double damage;
		private List<Mob> targetMobs;

		public Creator(@NotNull StageCreationContext<StageDealDamage> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.setItem(SLOT_DAMAGE, ItemUtils.item(XMaterial.REDSTONE, Lang.stageDealDamageValue.toString()), event -> {
				Lang.DAMAGE_AMOUNT.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, newDamage -> {
					setDamage(newDamage);
					event.reopen();
				}, NumberParser.DOUBLE_PARSER_STRICT_POSITIVE).start();
			});

			line.setItem(SLOT_MOBS, ItemUtils.item(XMaterial.BLAZE_SPAWN_EGG, Lang.stageDealDamageMobs.toString(), QuestOption.formatNullableValue(Lang.EntityTypeAny.toString(), true)), event -> {
				new ListGUI<Mob>(Lang.stageDealDamageMobs.toString(), DyeColor.RED, targetMobs == null ? Collections.emptyList() : targetMobs) {

					@Override
					public void finish(List<Mob> objects) {
						setTargetMobs(objects.isEmpty() ? null : objects);
						event.reopen();
					}

					@Override
					public ItemStack getObjectItemStack(Mob object) {
						return ItemUtils.item(object.getMobItem(), object.getName(),
								createLoreBuilder(object).toLoreArray());
					}

					@Override
					public void createObject(Function<Mob, ItemStack> callback) {
						new MobSelectionGUI(callback::apply).open(player);
					}

				}.open(event.getPlayer());
			});
		}

		public void setDamage(double damage) {
			this.damage = damage;
			getLine().refreshItem(SLOT_DAMAGE,
					item -> ItemUtils.lore(item, QuestOption.formatNullableValue(Double.toString(damage))));
		}

		public void setTargetMobs(List<Mob> targetMobs) {
			this.targetMobs = targetMobs;
			boolean noMobs = targetMobs == null || targetMobs.isEmpty();
			getLine().refreshItem(SLOT_MOBS,
					item -> ItemUtils.lore(item, QuestOption.formatNullableValue(getTargetMobsString(targetMobs), noMobs)));
		}

		@Override
		public void edit(StageDealDamage stage) {
			super.edit(stage);
			setDamage(stage.damage);
			setTargetMobs(stage.targetMobs);
		}

		@Override
		public void start(Player p) {
			super.start(p);
			Lang.DAMAGE_AMOUNT.send(p);
			new TextEditor<>(p, context::removeAndReopenGui, newDamage -> {
				setDamage(newDamage);
				context.reopenGui();
			}, NumberParser.DOUBLE_PARSER_STRICT_POSITIVE).start();
		}

		@Override
		protected StageDealDamage finishStage(StageController controller) {
			return new StageDealDamage(controller, damage, targetMobs);
		}

	}

}
