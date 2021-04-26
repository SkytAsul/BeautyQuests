package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.worldguard.BQWorldGuard;

public class RegionRequirement extends AbstractRequirement {
	
	private String worldName;
	private String regionName;
	private ProtectedRegion region;
	
	public RegionRequirement() {
		this(null, null);
	}
	
	public RegionRequirement(String worldName, String regionName) {
		super("regionRequired");
		if (!DependenciesManager.wg.isEnabled()) throw new MissingDependencyException("WorldGuard");
		
		this.worldName = worldName;
		setRegionName(regionName);
	}
	
	private void setRegionName(String regionName) {
		this.regionName = regionName;
		if (worldName != null) this.region = BQWorldGuard.getRegion(regionName, Bukkit.getWorld(worldName));
		if (region == null) BeautyQuests.logger.warning("Region " + regionName + " no longer exist in world " + worldName);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { Lang.optionValue.format(regionName), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.CHOOSE_REGION_REQUIRED.send(p);
		new TextEditor<String>(p, () -> {
			if (regionName == null) gui.remove(this);
			gui.reopen();
		}, obj -> {
			this.region = BQWorldGuard.getRegion(obj, p.getWorld());
			if (region != null) {
				this.worldName = p.getWorld().getName();
				this.regionName = region.getId();
				ItemUtils.lore(clicked, getLore());
			}else {
				Utils.sendMessage(p, Lang.REGION_DOESNT_EXIST.toString());
				gui.remove(this);
			}
			gui.reopen();
		}).useStrippedMessage().enter();
	}
	
	@Override
	public boolean test(Player p) {
		return region != null && BQWorldGuard.isInRegion(region, p.getLocation());
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
	protected void save(Map<String, Object> datas) {
		datas.put("world", worldName);
		datas.put("region", regionName);
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		worldName = (String) savedDatas.get("world");
		setRegionName((String) savedDatas.get("region"));
	}
	
}
