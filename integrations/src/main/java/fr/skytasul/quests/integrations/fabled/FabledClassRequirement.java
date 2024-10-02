package fr.skytasul.quests.integrations.fabled;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.classes.FabledClass;
import studio.magemonkey.fabled.api.player.PlayerClass;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FabledClassRequirement extends AbstractRequirement {

	public List<FabledClass> classes;

	public FabledClassRequirement() {
		this(null, null, new ArrayList<>());
	}

	public FabledClassRequirement(String customDescription, String customReason, List<FabledClass> classes) {
		super(customDescription, customReason);
		this.classes = classes;
	}

	public List<String> getClassesName(){
		List<String> ls = new ArrayList<>();
		for (FabledClass cl : classes)
			ls.add(cl.getName());
		return ls;
	}

	public void addClass(Object classe){
		classes.add((FabledClass) classe);
	}

	@Override
	public boolean test(Player p) {
		if (classes.isEmpty()) return true;
		for (FabledClass classe : classes) {
			PlayerClass mainClass = Fabled.getData(p).getMainClass();
			if (mainClass != null && mainClass.getData() == classe) return true;
		}
		return false;
	}

	@Override
	public String getDefaultDescription(Player p) {
		return Lang.RDClass.quickFormat("classes", String.join(" " + Lang.Or.toString() + " ",
				(Iterable<String>) () -> classes.stream().map(FabledClass::getName).iterator()));
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(classes.size() + " classes");
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ListGUI<FabledClass>(Lang.INVENTORY_CLASSES_REQUIRED.toString(), DyeColor.GREEN, classes) {

			@Override
			public ItemStack getObjectItemStack(FabledClass object) {
				return ItemUtils.loreAdd(object.getIcon(), createLoreBuilder(object).toLoreArray());
			}

			@Override
			public void createObject(Function<FabledClass, ItemStack> callback) {
				new PagedGUI<FabledClass>(Lang.INVENTORY_CLASSES_LIST.toString(), DyeColor.PURPLE,
						Fabled.getClasses().values()) {

					@Override
					public ItemStack getItemStack(FabledClass object) {
						return object.getIcon();
					}

					@Override
					public void click(FabledClass existing, ItemStack item, ClickType clickType) {
						callback.apply(existing);
					}
				}.open(player);
			}

			@Override
			public void finish(List<FabledClass> objects) {
				classes = objects;
				event.reopenGUI();
			}

		}.open(event.getPlayer());
	}

	@Override
	public AbstractRequirement clone() {
		return new FabledClassRequirement(getCustomDescription(), getCustomReason(), new ArrayList<>(classes));
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		if (!classes.isEmpty())
			section.set("classes", classes.stream().map(FabledClass::getName).collect(Collectors.toList()));
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		if (!section.contains("classes")) return;
		for (String s : section.getStringList("classes")) {
			FabledClass classe = Fabled.getClasses().get(s.toLowerCase());
			if (classe == null) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Class with name " + s + " no longer exists.");
				continue;
			}
			classes.add(classe);
		}
	}

}
