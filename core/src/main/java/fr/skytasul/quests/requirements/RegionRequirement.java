package fr.skytasul.quests.requirements;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.options.QuestOption;
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
		this(null, null, null, null);
	}
	
	public RegionRequirement(String customDescription, String customReason, String worldName, String regionName) {
		super(customDescription, customReason);
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
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(regionName));
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
				event.reopenGUI();
			}else {
				Utils.sendMessage(p, Lang.REGION_DOESNT_EXIST.toString());
				event.remove();
			}
		}).useStrippedMessage().enter();
	}
	
	@Override
	public boolean test(Player p) {
		if (region == null) return false;
		if (regionName.equals("__global__")) return p.getWorld().getName().equals(worldName);
		return BQWorldGuard.getInstance().isInRegion(region, p.getLocation());
	}
	
	@Override
	protected String getInvalidReason() {
		return "required region " + regionName + " in " + worldName + " does not exist";
	}

	@Override
	public boolean isValid() {
		return region != null;
	}

	@Override
	public AbstractRequirement clone() {
		return new RegionRequirement(getCustomDescription(), getCustomReason(), worldName, regionName);
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("world", worldName);
		section.set("region", regionName);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		worldName = section.getString("world");
		setRegionName(section.getString("region"));
	}
	
}
