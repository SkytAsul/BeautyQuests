package fr.skytasul.quests.api.requirements;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

public abstract class AbstractRequirement extends QuestObject {

	protected static final String CUSTOM_REASON_KEY = "customReason";

	private String customReason;

	protected AbstractRequirement() {
		this(null, null);
	}

	protected AbstractRequirement(@Nullable String customDescription, @Nullable String customReason) {
		super(QuestsAPI.getAPI().getRequirements(), customDescription);
		this.customReason = customReason;
	}

	public @Nullable String getCustomReason() {
		return customReason;
	}

	public void setCustomReason(@Nullable String customReason) {
		this.customReason = customReason;
	}

	/**
	 * Called when the plugin has to check if a player can start a quest with this requirement
	 * @param p Player to test
	 * @return if the player fills conditions of this requirement
	 */
	public abstract boolean test(@NotNull Player p);

	/**
	 * Gets the reason the requirement has not matched for a player.
	 *
	 * @param player
	 * @return a string explaining why {@link #test(Player)} returned <code>false</code>
	 */
	public @Nullable String getReason(@NotNull Player player) {
		String reason = null;

		if (!isValid())
			reason = "§cerror: " + getInvalidReason();
		else if (customReason != null)
			reason = customReason;
		else
			reason = getDefaultReason(player);

		if (reason != null && !reason.isEmpty() && !"none".equals(reason))
			return MessageUtils.format(reason, getPlaceholdersRegistry());

		return null;
	}

	/**
	 * Gets the reason sent to the player in the chat if it does not meet the requirements and the user
	 * has not set a particular requirement reason.
	 *
	 * @param player player to get the message for
	 * @return the reason of the requirement (nullable)
	 */
	protected @Nullable String getDefaultReason(@NotNull Player player) {
		return null;
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.register("custom_reason", () -> customReason);
	}

	protected @Nullable ClickType getCustomReasonClick() {
		return ClickType.SHIFT_RIGHT;
	}

	protected @Nullable String getCustomReasonIndication(@NotNull Player p) {
		return Lang.CHOOSE_REQUIREMENT_CUSTOM_REASON.toString();
	}

	@Override
	protected String getCustomDescriptionIndication(@NotNull Player p) {
		return Lang.CHOOSE_REQUIREMENT_CUSTOM_DESCRIPTION.toString();
	}

	@Override
	protected final void clickInternal(@NotNull QuestObjectClickEvent event) {
		if (event.getClick() == getCustomReasonClick()) {
			QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderString(event.getPlayer(), event::reopenGUI, reason -> {
				setCustomReason(reason == null ? "none" : reason);
				event.reopenGUI();
			}).addReset(() -> {
				setCustomReason(null);
				event.reopenGUI();
			}, "reset")
			.allowEmpty().allowMultiline()
			.setInitialString(customReason)
			.setIndication(getCustomReasonIndication(event.getPlayer()))
			.build().start();
		} else {
			itemClick(event);
		}
	}

	protected abstract void itemClick(@NotNull QuestObjectClickEvent event);

	@Override
	protected void addLore(@NotNull LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(Lang.requirementReason.format(
						PlaceholderRegistry.of("reason", customReason == null ? Lang.NotSet.toString() : customReason)));
		loreBuilder.addClick(getCustomReasonClick(), Lang.setRequirementReason.toString());
	}

	@Override
	public abstract @NotNull AbstractRequirement clone();

	@Override
	public void save(@NotNull ConfigurationSection section) {
		super.save(section);
		if (customReason != null)
			section.set(CUSTOM_REASON_KEY, customReason);
	}

	@Override
	public void load(@NotNull ConfigurationSection section) {
		super.load(section);
		if (section.contains(CUSTOM_REASON_KEY))
			customReason = section.getString(CUSTOM_REASON_KEY);
	}

	public static @NotNull AbstractRequirement deserialize(Map<String, Object> map) {
		return SerializableObject.deserialize(map, QuestsAPI.getAPI().getRequirements());
	}

}
