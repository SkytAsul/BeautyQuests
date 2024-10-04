package fr.skytasul.quests.integrations.worldguard;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegionRequirement extends AbstractRequirement {

	private String worldName;
	private String regionName;
	private ProtectedRegion region;

	private boolean mustBeHighest;

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
			if (region == null) QuestsPlugin.getPlugin().getLoggerExpanded().warning("Region " + regionName + " no longer exist in world " + worldName);
		}
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.register("region_name", () -> regionName);
		placeholders.register("region_world", () -> worldName);
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(regionName));
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Player p = event.getPlayer();
		Lang.CHOOSE_REGION_REQUIRED.send(p);
		new TextEditor<String>(p, event::cancel, obj -> {
			this.region = BQWorldGuard.getInstance().getRegion(obj, p.getWorld());
			if (region != null) {
				this.worldName = p.getWorld().getName();
				this.regionName = region.getId();
				event.reopenGUI();
			}else {
				Lang.REGION_DOESNT_EXIST.send(p);
				event.cancel();
			}
		}).useStrippedMessage().start();
	}

	@Override
	public boolean test(Player p) {
		if (region == null)
			return false;
		if (!p.getWorld().getName().equals(worldName))
			return false;
		return BQWorldGuard.getInstance().isInRegion(region, p.getLocation(), mustBeHighest);
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
		if (mustBeHighest)
			section.set("mustBeHighest", true);
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		worldName = section.getString("world");
		setRegionName(section.getString("region"));
		mustBeHighest = section.getBoolean("mustBeHighest", false);
	}

}
