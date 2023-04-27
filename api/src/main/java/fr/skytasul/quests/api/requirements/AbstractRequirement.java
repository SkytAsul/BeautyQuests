package fr.skytasul.quests.api.requirements;

import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.utils.MessageUtils;

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
	 * Called if the condition if not filled and if the plugin allows to send a message to the player
	 * @param player Player to send the reason
	 */
	public final boolean sendReason(@NotNull Player player) {
		String reason;

		if (!isValid())
			reason = "§cerror: " + getInvalidReason();
		else if (customReason != null)
			reason = customReason;
		else
			reason = getDefaultReason(player);

		if (reason != null && !reason.isEmpty() && !"none".equals(reason)) {
			MessageUtils.sendPrefixedMessage(player, reason);
			return true;
		}

		return false;
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
	
	protected @NotNull String getInvalidReason() {
		return "invalid requirement";
	}

	protected @Nullable ClickType getCustomReasonClick() {
		return ClickType.SHIFT_RIGHT;
	}

	protected void sendCustomReasonHelpMessage(@NotNull Player p) {
		Lang.CHOOSE_REQUIREMENT_CUSTOM_REASON.send(p);
	}

	@Override
	protected void sendCustomDescriptionHelpMessage(@NotNull Player p) {
		Lang.CHOOSE_REQUIREMENT_CUSTOM_DESCRIPTION.send(p);
	}

	@Override
	protected final void clickInternal(@NotNull QuestObjectClickEvent event) {
		if (event.getClick() == getCustomReasonClick()) {
			sendCustomReasonHelpMessage(event.getPlayer());
			new TextEditor<String>(event.getPlayer(), event::reopenGUI, msg -> {
				setCustomReason(msg);
				event.reopenGUI();
			}).passNullIntoEndConsumer().start();
		} else {
			itemClick(event);
		}
	}

	protected abstract void itemClick(@NotNull QuestObjectClickEvent event);

	@Override
	protected void addLore(@NotNull QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder
				.addDescription(Lang.requirementReason.format(customReason == null ? Lang.NotSet.toString() : customReason));
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
