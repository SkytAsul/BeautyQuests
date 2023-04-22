package fr.skytasul.quests.api.objects;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public abstract class QuestObject extends SerializableObject implements Cloneable {
	
	protected static final String CUSTOM_DESCRIPTION_KEY = "customDescription";

	private Quest quest;
	private String customDescription;
	
	protected QuestObject(@NotNull QuestObjectsRegistry registry, @Nullable String customDescription) {
		super(registry);
		this.customDescription = customDescription;
	}
	
	@Override
	public QuestObjectCreator getCreator() {
		return (QuestObjectCreator) super.getCreator();
	}
	
	public void attach(@NotNull Quest quest) {
		this.quest = quest;
	}
	
	public void detach() {
		this.quest = null;
	}
	
	public @Nullable Quest getAttachedQuest() {
		return quest;
	}
	
	public @Nullable String getCustomDescription() {
		return customDescription;
	}

	public void setCustomDescription(@Nullable String customDescription) {
		this.customDescription = customDescription;
	}

	public @NotNull String debugName() {
		return getClass().getSimpleName() + (quest == null ? ", unknown quest" : (", quest " + quest.getID()));
	}
	
	public boolean isValid() {
		return true;
	}

	@Override
	public abstract @NotNull QuestObject clone();
	
	@Deprecated
	protected void save(Map<String, Object> datas) {}
	
	@Deprecated
	protected void load(Map<String, Object> savedDatas) {}
	
	@Override
	public void save(@NotNull ConfigurationSection section) {
		Map<String, Object> datas = new HashMap<>();
		save(datas);
		Utils.setConfigurationSectionContent(section, datas);

		if (customDescription != null)
			section.set(CUSTOM_DESCRIPTION_KEY, customDescription);
	}
	
	@Override
	public void load(@NotNull ConfigurationSection section) {
		load(section.getValues(false));

		if (section.contains(CUSTOM_DESCRIPTION_KEY))
			customDescription = section.getString(CUSTOM_DESCRIPTION_KEY);
	}
	
	public final @Nullable String getDescription(Player player) {
		return customDescription == null ? getDefaultDescription(player) : customDescription;
	}

	/**
	 * Gets the description shown in the GUIs for this quest object.
	 * 
	 * @param player player to get the description for
	 * @return the description of this object (nullable)
	 */
	protected @Nullable String getDefaultDescription(@NotNull Player p) {
		return null;
	}
	
	@Deprecated
	public String[] getLore() { // backward compatibility from 0.20.1 - TODO REMOVE
		return null;
	}

	public @NotNull String @Nullable [] getItemLore() {
		String[] legacyLore = getLore();
		if (legacyLore != null)
			return legacyLore;

		QuestObjectLoreBuilder lore = new QuestObjectLoreBuilder();
		addLore(lore);
		return lore.toLoreArray();
	}
	
	protected void addLore(@NotNull QuestObjectLoreBuilder loreBuilder) {
		loreBuilder.addClick(getRemoveClick(), "§c" + Lang.Remove.toString());
		loreBuilder.addClick(getCustomDescriptionClick(), Lang.object_description_set.toString());

		String description;
		try {
			description = getDescription(null);
		} catch (Exception ex) {
			description = "§cerror";
			BeautyQuests.logger.warning("Could not get quest object description during edition", ex);
		}
		loreBuilder.addDescription(
				Lang.object_description.format(description + (customDescription == null ? " " + Lang.defaultValue : "")));
	}

	public @NotNull ItemStack getItemStack() {
		return ItemUtils.lore(getCreator().getItem().clone(), getItemLore());
	}
	
	public @Nullable ClickType getRemoveClick() {
		return ClickType.SHIFT_LEFT;
	}

	protected @Nullable ClickType getCustomDescriptionClick() {
		return ClickType.RIGHT;
	}

	protected abstract void sendCustomDescriptionHelpMessage(@NotNull Player p);

	public final void click(@NotNull QuestObjectClickEvent event) {
		if (event.getClick() == getRemoveClick())
			return;

		if (event.getClick() == getCustomDescriptionClick()) {
			sendCustomDescriptionHelpMessage(event.getPlayer());
			new TextEditor<String>(event.getPlayer(), event::reopenGUI, msg -> {
				setCustomDescription(msg);
				event.reopenGUI();
			}).passNullIntoEndConsumer().enter();
		} else {
			clickInternal(event);
		}
	}
	
	protected abstract void clickInternal(@NotNull QuestObjectClickEvent event);

}