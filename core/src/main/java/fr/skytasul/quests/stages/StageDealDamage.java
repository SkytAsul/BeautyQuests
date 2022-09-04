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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.mobs.Mob;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.mobs.MobSelectionGUI;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

@SuppressWarnings ("rawtypes")
public class StageDealDamage extends AbstractStage {
	
	private final double damage;
	private final List<Mob> targetMobs;
	
	private final String targetMobsString;
	
	public StageDealDamage(QuestBranch branch, double damage, List<Mob> targetMobs) {
		super(branch);
		this.damage = damage;
		this.targetMobs = targetMobs;
		
		targetMobsString = getTargetMobsString(targetMobs);
	}
	
	private static String getTargetMobsString(List<Mob> targetMobs) {
		if (targetMobs == null || targetMobs.isEmpty()) return Lang.EntityTypeAny.toString();
		return targetMobs.stream().map(Mob::getName).collect(Collectors.joining(", "));
	}
	
	@Override
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		datas.put("amount", damage);
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) return;
		
		if (targetMobs != null && !targetMobs.isEmpty()
				&& targetMobs.stream().noneMatch(mob -> mob.appliesEntity(event.getEntity()))) return;
		
		Player player = (Player) event.getDamager();
		PlayerAccount account = PlayersManager.getPlayerAccount(player);
		
		if (!branch.hasStageLaunched(account, this)) return;
		if (!canUpdate(player)) return;
		
		double amount = getData(account, "amount");
		amount -= event.getFinalDamage();
		if (amount <= 0) {
			finishStage(player);
		}else {
			updateObjective(account, player, "amount", amount);
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return (targetMobs == null || targetMobs.isEmpty() ? Lang.SCOREBOARD_DEAL_DAMAGE_ANY : Lang.SCOREBOARD_DEAL_DAMAGE_MOBS).format(descriptionFormat(acc, source));
	}
	
	@Override
	protected Object[] descriptionFormat(PlayerAccount acc, Source source) {
		return new Object[] { (Supplier<String>) () -> Integer.toString(super.<Double>getData(acc, "amount").intValue()), targetMobsString };
	}
	
	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("damage", damage);
		if (targetMobs != null && !targetMobs.isEmpty())
			section.set("targetMobs", targetMobs.stream().map(Mob::serialize).collect(Collectors.toList()));
	}
	
	public static StageDealDamage deserialize(ConfigurationSection section, QuestBranch branch) {
		return new StageDealDamage(branch,
				section.getDouble("damage"),
				section.contains("targetMobs") ? section.getMapList("targetMobs").stream().map(map -> Mob.deserialize((Map) map)).collect(Collectors.toList()) : null);
	}
	
	public static class Creator extends StageCreation<StageDealDamage> {
		
		private static final int SLOT_DAMAGE = 6;
		private static final int SLOT_MOBS = 7;
		
		private double damage;
		private List<Mob> targetMobs;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(SLOT_DAMAGE, ItemUtils.item(XMaterial.REDSTONE, Lang.stageDealDamageValue.toString()), (p, item) -> {
				Lang.DAMAGE_AMOUNT.send(p);
				new TextEditor<>(p, () -> reopenGUI(p, false), newDamage -> {
					setDamage(newDamage);
					reopenGUI(p, false);
				}, NumberParser.DOUBLE_PARSER_STRICT_POSITIVE).enter();
			});
			
			line.setItem(SLOT_MOBS, ItemUtils.item(XMaterial.BLAZE_SPAWN_EGG, Lang.stageDealDamageMobs.toString(), QuestOption.formatNullableValue(Lang.EntityTypeAny.toString(), true)), (p, item) -> {
				new ListGUI<Mob>(Lang.stageDealDamageMobs.toString(), DyeColor.RED, targetMobs == null ? Collections.emptyList() : targetMobs) {
					
					@Override
					public void finish(List<Mob> objects) {
						setTargetMobs(objects.isEmpty() ? null : objects);
						reopenGUI(p, true);
					}
					
					@Override
					public ItemStack getObjectItemStack(Mob object) {
						return object.createItemStack(1);
					}
					
					@Override
					public void createObject(Function<Mob, ItemStack> callback) {
						new MobSelectionGUI(callback::apply).create(p);
					}
					
				}.create(p);
			});
		}
		
		public void setDamage(double damage) {
			this.damage = damage;
			line.editItem(SLOT_DAMAGE, ItemUtils.lore(line.getItem(SLOT_DAMAGE), QuestOption.formatNullableValue(Double.toString(damage))));
		}
		
		public void setTargetMobs(List<Mob> targetMobs) {
			this.targetMobs = targetMobs;
			boolean noMobs = targetMobs == null || targetMobs.isEmpty();
			line.editItem(SLOT_MOBS, ItemUtils.lore(line.getItem(SLOT_MOBS), QuestOption.formatNullableValue(getTargetMobsString(targetMobs), noMobs)));
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
			new TextEditor<>(p, removeAndReopen(p, false), newDamage -> {
				setDamage(newDamage);
				reopenGUI(p, false);
			}, NumberParser.DOUBLE_PARSER_STRICT_POSITIVE).enter();
		}
		
		@Override
		protected StageDealDamage finishStage(QuestBranch branch) {
			return new StageDealDamage(branch, damage, targetMobs);
		}
		
	}
	
}
