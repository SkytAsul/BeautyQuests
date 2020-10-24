package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.misc.ItemGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

/**
 * @author SkytAsul, ezeiger92, TheBusyBiscuit
 */
public class StageCraft extends AbstractStage {

	private ItemStack result;
	
	public StageCraft(QuestBranch branch, ItemStack result){
		super(branch);
		this.result = result;
	}
	
	public ItemStack getItem(){
		return result;
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onCraft(CraftItemEvent e){
		Player p = (Player) e.getView().getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		ItemStack item = e.getRecipe().getResult();
		
		if (branch.hasStageLaunched(acc, this) && canUpdate(p)) {
			if (e.getRecipe().getResult().isSimilar(result)){
				
				int recipeAmount = item.getAmount();

				switch (e.getClick()) {
					case NUMBER_KEY:
						// If hotbar slot selected is full, crafting fails (vanilla behavior, even when items match)
						if (e.getWhoClicked().getInventory().getItem(e.getHotbarButton()) != null) recipeAmount = 0;
						break;

					case DROP:
					case CONTROL_DROP:
						// If we are holding items, craft-via-drop fails (vanilla behavior)
						ItemStack cursor = e.getCursor();
						if (cursor != null && cursor.getType() != Material.AIR) recipeAmount = 0;
						break;

					case SHIFT_RIGHT:
					case SHIFT_LEFT:
						if (recipeAmount == 0) break;

						int maxCraftable = getMaxCraftAmount(e.getInventory());
						int capacity = fits(item, e.getView().getBottomInventory());

						// If we can't fit everything, increase "space" to include the items dropped by crafting
						// (Think: Uncrafting 8 iron blocks into 1 slot)
						if (capacity < maxCraftable)
							maxCraftable = ((capacity + recipeAmount - 1) / recipeAmount) * recipeAmount;

						recipeAmount = maxCraftable;
						break;
					default:
				}

				// No use continuing if we haven't actually crafted a thing
				if (recipeAmount == 0) return;

				int amount = getPlayerAmount(acc) - recipeAmount;
				if (amount <= 0) {
					finishStage(p);
				}else {
					updateObjective(acc, p, "amount", amount);
				}
			}
		}
	}
	
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("amount", result.getAmount());
	}

	private int getPlayerAmount(PlayerAccount acc) {
		return getData(acc, "amount");
	}

	protected String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_CRAFT.format(Utils.getStringFromNameAndAmount(ItemUtils.getName(result, true), QuestsConfiguration.getItemAmountColor(), getPlayerAmount(acc), result.getAmount(), false));
	}

	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new Object[] { Utils.getStringFromNameAndAmount(ItemUtils.getName(result, true), QuestsConfiguration.getItemAmountColor(), getPlayerAmount(acc), result.getAmount(), false) };
	}
	
	protected void serialize(Map<String, Object> map){
		map.put("result", result.serialize());
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		StageCraft stage = new StageCraft(branch, ItemStack.deserialize((Map<String, Object>) map.get("result")));

		if (map.containsKey("players")) {
			PlayersManagerYAML migration = PlayersManagerYAML.getMigrationYAML();
			((Map<String, Object>) map.get("players")).forEach((acc, amount) -> stage.setData(migration.getByIndex(acc), "amount", (int) amount));
		}

		return stage;
	}
	
	public static int getMaxCraftAmount(CraftingInventory inv) {
		if (inv.getResult() == null) return 0;

		int resultCount = inv.getResult().getAmount();
		int materialCount = Integer.MAX_VALUE;

		for (ItemStack is : inv.getMatrix())
			if (is != null && is.getAmount() < materialCount)
				materialCount = is.getAmount();

		return resultCount * materialCount;
	}

	public static int fits(ItemStack stack, Inventory inv) {
		int result = 0;

		for (ItemStack is : inv.getContents())
			if (is == null)
				result += stack.getMaxStackSize();
			else if (is.isSimilar(stack))
				result += Math.max(stack.getMaxStackSize() - is.getAmount(), 0);

		return result;
	}

	public static class Creator extends StageCreation<StageCraft> {
		
		private ItemStack item;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(XMaterial.CHEST, Lang.editItem.toString()), (p, item) -> {
				new ItemGUI((is) -> {
					setItem(is);
					reopenGUI(p, true);
				}, () -> reopenGUI(p, true)).create(p);
			});
		}

		public void setItem(ItemStack item) {
			this.item = item;
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(Utils.getStringFromItemStack(item, "§8", true))));
		}

		@Override
		public void start(Player p) {
			super.start(p);
			new ItemGUI(is -> {
				setItem(is);
				reopenGUI(p, true);
			}, removeAndReopen(p, true)).create(p);
		}

		@Override
		public void edit(StageCraft stage) {
			super.edit(stage);
			setItem(stage.getItem());
		}
		
		@Override
		public StageCraft finishStage(QuestBranch branch) {
			return new StageCraft(branch, item);
		}
	}

}
