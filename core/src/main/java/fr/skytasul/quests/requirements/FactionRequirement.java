package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.FactionsIndex;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Factions;

public class FactionRequirement extends AbstractRequirement {

	public List<Faction> factions;
	
	public FactionRequirement() {
		this(new ArrayList<>());
	}
	
	public FactionRequirement(List<Faction> factions) {
		this.factions = factions;
	}
	
	public void addFaction(Object faction){
		factions.add((Faction) faction);
	}
	
	@Override
	public String getDescription(Player p) {
		return Lang.RDFaction.format(String.join(" " + Lang.Or.toString() + " ", (Iterable<String>) () -> factions.stream().map(Faction::getName).iterator()));
	}
	
	@Override
	public boolean test(Player p) {
		if (factions.isEmpty()) return true;
		for (Faction fac : factions){
			if (FactionsIndex.get().getFaction(MPlayer.get(p)) == fac) return true;
		}
		return false;
	}

	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + factions.size() + " factions", "", Lang.RemoveMid.toString() };
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ListGUI<Faction>(Lang.INVENTORY_FACTIONS_REQUIRED.toString(), DyeColor.LIGHT_BLUE, factions) {
			
			@Override
			public ItemStack getObjectItemStack(Faction object) {
				return ItemUtils.item(XMaterial.IRON_SWORD, object.getName(), "", Lang.RemoveMid.toString());
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
				event.reopenGUI();
			}
			
		}.create(event.getPlayer());
	}
	
	@Override
	public AbstractRequirement clone() {
		return new FactionRequirement(new ArrayList<>(factions));
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("factions", factions.stream().map(Faction::getId).collect(Collectors.toList()));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		for (String s : section.getStringList("factions")) {
			if (!FactionColl.get().containsId(s)) {
				BeautyQuests.getInstance().getLogger().warning("Faction with ID " + s + " no longer exists.");
				continue;
			}
			factions.add(FactionColl.get().get(s));
		}
	}
	
}
