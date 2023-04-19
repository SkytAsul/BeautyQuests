package fr.skytasul.quests.api.requirements;

import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public abstract class AbstractRequirement extends QuestObject {
	
	protected static final String CUSTOM_REASON_KEY = "customReason";

	private String customReason;

	protected AbstractRequirement() {
		this(null, null);
	}

	protected AbstractRequirement(String customDescription, String customReason) {
		super(QuestsAPI.getRequirements(), customDescription);
		this.customReason = customReason;
	}
	
	public String getCustomReason() {
		return customReason;
	}

	public void setCustomReason(String customReason) {
		this.customReason = customReason;
	}

	/**
	 * Called when the plugin has to check if a player can start a quest with this requirement
	 * @param p Player to test
	 * @return if the player fills conditions of this requirement
	 */
	public abstract boolean test(Player p);
	
	/**
	 * Called if the condition if not filled and if the plugin allows to send a message to the player
	 * @param p Player to send the reason
	 */
	public final boolean sendReason(Player player) {
		String reason;

		if (!isValid())
			reason = "§cerror: " + getInvalidReason();
		else if (customReason != null)
			reason = customReason;
		else
			reason = getDefaultReason(player);

		if (reason != null && !reason.isEmpty() && !"none".equals(reason)) {
			Utils.sendMessage(player, reason);
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
	protected String getDefaultReason(Player player) {
		return null;
	}
	
	protected String getInvalidReason() {
		return "invalid requirement";
	}

	protected ClickType getCustomReasonClick() {
		return ClickType.SHIFT_RIGHT;
	}

	protected void sendCustomReasonHelpMessage(Player p) {
		Lang.CHOOSE_REQUIREMENT_CUSTOM_REASON.send(p);
	}

	@Override
	protected void sendCustomDescriptionHelpMessage(Player p) {
		Lang.CHOOSE_REQUIREMENT_CUSTOM_DESCRIPTION.send(p);
	}

	@Override
	protected final void clickInternal(QuestObjectClickEvent event) {
		if (event.getClick() == getCustomReasonClick()) {
			sendCustomReasonHelpMessage(event.getPlayer());
			new TextEditor<String>(event.getPlayer(), event::reopenGUI, msg -> {
				setCustomReason(msg);
				event.reopenGUI();
			}).enter();
		} else {
			itemClick(event);
		}
	}

	protected abstract void itemClick(QuestObjectClickEvent event);

	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder
				.addDescription(Lang.requirementReason.format(customReason == null ? Lang.NotSet.toString() : customReason));
		loreBuilder.addClick(getCustomReasonClick(), Lang.setRequirementReason.toString());
	}

	@Override
	public abstract AbstractRequirement clone();
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		if (customReason != null)
			section.set(CUSTOM_REASON_KEY, customReason);
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		if (section.contains(CUSTOM_REASON_KEY))
			customReason = section.getString(CUSTOM_REASON_KEY);
	}

	public static AbstractRequirement deserialize(Map<String, Object> map) {
		return QuestObject.deserialize(map, QuestsAPI.getRequirements());
	}
	
}
