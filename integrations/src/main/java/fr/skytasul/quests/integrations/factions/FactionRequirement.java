package fr.skytasul.quests.integrations.factions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.massivecraft.factions.FactionsIndex;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.requirements.AbstractRequirement;

public class FactionRequirement extends AbstractRequirement {

	public List<Faction> factions;
	
	public FactionRequirement() {
		this(null, null, new ArrayList<>());
	}
	
	public FactionRequirement(String customDescription, String customReason, List<Faction> factions) {
		super(customDescription, customReason);
		this.factions = factions;
	}
	
	public void addFaction(Object faction){
		factions.add((Faction) faction);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return Lang.RDFaction.quickFormat("factions", String.join(" " + Lang.Or.toString() + " ",
				(Iterable<String>) () -> factions.stream().map(Faction::getName).iterator()));
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
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(factions.size() + " factions");
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
				new PagedGUI<Faction>(Lang.INVENTORY_FACTIONS_LIST.toString(), DyeColor.PURPLE, FactionColl.get().getAll()) {
					
					@Override
					public ItemStack getItemStack(Faction object) {
						return ItemUtils.item(XMaterial.IRON_SWORD, object.getName());
					}
					
					@Override
					public void click(Faction existing, ItemStack item, ClickType clickType) {
						callback.apply(existing);
					}
				}.open(player);
			}
			
			@Override
			public void finish(List<Faction> objects) {
				factions = objects;
				event.reopenGUI();
			}
			
		}.open(event.getPlayer());
	}
	
	@Override
	public AbstractRequirement clone() {
		return new FactionRequirement(getCustomDescription(), getCustomReason(), new ArrayList<>(factions));
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("factions", factions.stream().map(Faction::getId).collect(Collectors.toList()));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		for (String s : section.getStringList("factions")) {
			if (!FactionColl.get().containsId(s)) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Faction with ID " + s + " no longer exists.");
				continue;
			}
			factions.add(FactionColl.get().get(s));
		}
	}
	
}
