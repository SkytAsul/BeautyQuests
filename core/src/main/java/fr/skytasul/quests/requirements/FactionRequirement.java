package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.FactionsIndex;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.Factions;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;

public class FactionRequirement extends AbstractRequirement {

	public List<Faction> factions;
	
	public FactionRequirement() {
		this(new ArrayList<>());
	}
	
	public FactionRequirement(List<Faction> factions) {
		super("factionRequired");
		if (!DependenciesManager.fac) throw new MissingDependencyException("Factions");
		this.factions = factions;
	}
	
	public void addFaction(Object faction){
		factions.add((Faction) faction);
	}
	
	public boolean test(Player p) {
		if (factions.isEmpty()) return true;
		for (Faction fac : factions){
			if (FactionsIndex.get().getFaction(MPlayer.get(p)) == fac) return true;
		}
		return false;
	}

	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + factions.size() + " factions", "", Lang.Remove.toString() };
	}

	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new ListGUI<Faction>(Lang.INVENTORY_FACTIONS_REQUIRED.toString(), DyeColor.LIGHT_BLUE, factions) {
			
			@Override
			public ItemStack getObjectItemStack(Faction object) {
				return ItemUtils.item(XMaterial.IRON_SWORD, object.getName(), "", Lang.Remove.toString());
			}
			
			@Override
			public void createObject(Function<Faction, ItemStack> callback) {
				new PagedGUI<Faction>(Lang.INVENTORY_FACTIONS_LIST.toString(), DyeColor.PURPLE, Factions.getFactions()) {
					
					@Override
					public ItemStack getItemStack(Faction object) {
						return ItemUtils.item(XMaterial.IRON_SWORD, object.getName());
					}
					
					@Override
					public void click(Faction existing, ItemStack item, ClickType clickType) {
						callback.apply(existing);
					}
				}.create(p);
			}
			
			@Override
			public void finish(List<Faction> objects) {
				factions = objects;
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}
			
		}.create(p);
	}
	
	@Override
	public AbstractRequirement clone() {
		return new FactionRequirement(new ArrayList<>(factions));
	}
	
	protected void save(Map<String, Object> datas) {
		List<String> ls = new ArrayList<>();
		for (Faction fac : factions) {
			ls.add(fac.getId());
		}
		datas.put("factions", factions);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		for (String s : (List<String>) savedDatas.get("factions")) {
			if (!FactionColl.get().containsId(s)) {
				BeautyQuests.getInstance().getLogger().warning("Faction with ID " + s + " no longer exists.");
				continue;
			}
			factions.add(FactionColl.get().get(s));
		}
	}
	
}
