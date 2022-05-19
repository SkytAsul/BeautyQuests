package fr.skytasul.quests.requirements;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.worldguard.BQWorldGuard;

public class RegionRequirement extends AbstractRequirement {
	
	private String worldName;
	private String regionName;
	private ProtectedRegion region;
	
	public RegionRequirement() {
		this(null, null);
	}
	
	public RegionRequirement(String worldName, String regionName) {
		this.worldName = worldName;
		setRegionName(regionName);
	}
	
	private void setRegionName(String regionName) {
		this.regionName = regionName;
		if (worldName != null) {
			region = BQWorldGuard.getInstance().getRegion(regionName, Bukkit.getWorld(worldName));
			if (region == null) BeautyQuests.logger.warning("Region " + regionName + " no longer exist in world " + worldName);
		}
	}
	
	@Override
	public String[] getLore() {
		return new String[] { Lang.optionValue.format(regionName), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Player p = event.getPlayer();
		Lang.CHOOSE_REGION_REQUIRED.send(p);
		new TextEditor<String>(p, () -> {
			if (regionName == null) event.getGUI().remove(this);
			event.reopenGUI();
		}, obj -> {
			this.region = BQWorldGuard.getInstance().getRegion(obj, p.getWorld());
			if (region != null) {
				this.worldName = p.getWorld().getName();
				this.regionName = region.getId();
				event.updateItemLore(getLore());
			}else {
				Utils.sendMessage(p, Lang.REGION_DOESNT_EXIST.toString());
				event.getGUI().remove(this);
			}
			event.reopenGUI();
		}).useStrippedMessage().enter();
	}
	
	@Override
	public boolean test(Player p) {
		if (region == null) return false;
		if (regionName.equals("__global__")) return p.getWorld().getName().equals(worldName);
		return BQWorldGuard.getInstance().isInRegion(region, p.getLocation());
	}
	
	@Override
	public void sendReason(Player p) {
		if (region == null) Lang.ERROR_OCCURED.send(p, "required region does not exist");
	}
	
	@Override
	public AbstractRequirement clone() {
		return new RegionRequirement(worldName, regionName);
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("world", worldName);
		section.set("region", regionName);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		worldName = section.getString("world");
		setRegionName(section.getString("region"));
	}
	
}
