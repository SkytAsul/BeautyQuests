package fr.skytasul.quests.api.objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public abstract class QuestObject extends SerializableObject implements Cloneable, HasPlaceholders {

	protected static final String CUSTOM_DESCRIPTION_KEY = "customDescription";

	private Quest quest;
	private String customDescription;

	private @Nullable PlaceholderRegistry placeholders;

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
		return getClass().getSimpleName() + (quest == null ? ", unknown quest" : (", quest " + quest.getId()));
	}

	public boolean isValid() {
		return true;
	}

	@Override
	public final @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		if (placeholders == null) {
			placeholders = new PlaceholderRegistry();
			createdPlaceholdersRegistry(placeholders);
		}
		return placeholders;
	}

	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		placeholders.register("custom_description", () -> customDescription);
		placeholders.register("object_type", creator.getID());
	}

	@Override
	public abstract @NotNull QuestObject clone();

	@Override
	public void save(@NotNull ConfigurationSection section) {
		if (customDescription != null)
			section.set(CUSTOM_DESCRIPTION_KEY, customDescription);
	}

	@Override
	public void load(@NotNull ConfigurationSection section) {
		if (section.contains(CUSTOM_DESCRIPTION_KEY))
			customDescription = section.getString(CUSTOM_DESCRIPTION_KEY);
	}

	public final @Nullable String getDescription(Player player) {
		String string = customDescription == null ? getDefaultDescription(player) : customDescription;
		if (string != null)
			string = MessageUtils.format(string, getPlaceholdersRegistry());
		return string;
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

	public @NotNull String @Nullable [] getItemLore() {
		LoreBuilder lore = new LoreBuilder();
		addLore(lore);
		return lore.toLoreArray();
	}

	protected void addLore(@NotNull LoreBuilder loreBuilder) {
		loreBuilder.addClick(getRemoveClick(), "§c" + Lang.Remove.toString());
		loreBuilder.addClick(getCustomDescriptionClick(), Lang.object_description_set.toString());

		String description;
		try {
			description = getDescription(null);
		} catch (Exception ex) {
			description = "§cerror";
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Could not get quest object description during edition", ex);
		}
		if (description != null)
			loreBuilder.addDescription(Lang.object_description.format(PlaceholderRegistry.of("description",
					description + (customDescription == null ? " " + Lang.defaultValue : ""))));
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
			}).passNullIntoEndConsumer().start();
		} else {
			clickInternal(event);
		}
	}

	protected abstract void clickInternal(@NotNull QuestObjectClickEvent event);

}