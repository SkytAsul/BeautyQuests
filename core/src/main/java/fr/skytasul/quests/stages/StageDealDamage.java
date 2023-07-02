package fr.skytasul.quests.stages;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.gui.mobs.MobSelectionGUI;
import fr.skytasul.quests.mobs.Mob;

@SuppressWarnings ("rawtypes")
public class StageDealDamage extends AbstractStage {
	
	private final double damage;
	private final List<Mob> targetMobs;
	
	private final String targetMobsString;
	
	public StageDealDamage(StageController controller, double damage, List<Mob> targetMobs) {
		super(controller);
		this.damage = damage;
		this.targetMobs = targetMobs;
		
		targetMobsString = getTargetMobsString(targetMobs);
	}
	
	private static String getTargetMobsString(List<Mob> targetMobs) {
		if (targetMobs == null || targetMobs.isEmpty()) return Lang.EntityTypeAny.toString();
		return targetMobs.stream().map(Mob::getName).collect(Collectors.joining(", "));
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
	
	@Override
	public String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return (targetMobs == null || targetMobs.isEmpty() ? Lang.SCOREBOARD_DEAL_DAMAGE_ANY : Lang.SCOREBOARD_DEAL_DAMAGE_MOBS).format(descriptionFormat(acc, source));
	}
	
	@Override
	public Object[] descriptionFormat(PlayerAccount acc, DescriptionSource source) {
		return new Object[] { (Supplier<String>) () -> Integer.toString(super.<Double>getData(acc, "amount").intValue()), targetMobsString };
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
						return ItemUtils.item(object.getMobItem(), object.getName(), Lang.RemoveMid.toString());
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
