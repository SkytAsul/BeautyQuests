package fr.skytasul.quests.api.stages;

import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
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
import fr.skytasul.quests.utils.XMaterial;

public abstract class AbstractEntityStage extends AbstractStage {
	
	protected EntityType entity;
	protected int amount;

	public AbstractEntityStage(QuestBranch branch, EntityType entity, int amount) {
		super(branch);
		this.entity = entity;
		this.amount = amount;
	}
	
	protected void event(Player p, EntityType type) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this) && canUpdate(p)) {
			if (entity == null || type.equals(entity)) {
				Integer playerAmount = getPlayerAmount(acc);
				if (playerAmount == null) {
					BeautyQuests.logger.warning(p.getName() + " does not have object datas for stage " + debugName() + ". This is a bug!");
				}else if (playerAmount.intValue() <= 1) {
					finishStage(p);
				}else {
					updateObjective(acc, p, "amount", playerAmount.intValue() - 1);
				}
			}
		}
	}
	
	protected Integer getPlayerAmount(PlayerAccount acc) {
		return getData(acc, "amount");
	}
	
	@Override
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("amount", amount);
	}
	
	@Override
	protected void serialize(Map<String, Object> map) {
		map.put("entityType", entity == null ? "any" : entity.name());
		map.put("amount", amount);
	}
	
	protected String getMobsLeft(PlayerAccount acc) {
		Integer playerAmount = getPlayerAmount(acc);
		if (playerAmount == null) return "§cerror: no datas";
		
		return Utils.getStringFromNameAndAmount(entity == null ? Lang.EntityTypeAny.toString() : MinecraftNames.getEntityName(entity), QuestsConfiguration.getItemAmountColor(), playerAmount, amount, false);
	}
	
	@Override
	protected Supplier<Object>[] descriptionFormat(PlayerAccount acc, Source source) {
		return new Supplier[] { () -> getMobsLeft(acc) };
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
			line.editItem(6, ItemUtils.lore(line.getItem(6), entity == null ? Lang.EntityTypeAny.toString() : entity.name()));
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
		
		protected abstract boolean canUseEntity(EntityType type);
		
	}
	
}