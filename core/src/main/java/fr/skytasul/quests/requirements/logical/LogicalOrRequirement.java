package fr.skytasul.quests.requirements.logical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class LogicalOrRequirement extends AbstractRequirement {
	
	private List<AbstractRequirement> requirements;
	
	public LogicalOrRequirement() {
		this(new ArrayList<>());
	}
	
	public LogicalOrRequirement(List<AbstractRequirement> requirements) {
		super("logicalOr");
		this.requirements = requirements;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { Lang.requirements.format(requirements.size()), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new QuestObjectGUI<>(Lang.INVENTORY_REQUIREMENTS.toString(), QuestObjectLocation.OTHER, QuestsAPI.requirements.values(), requirements -> {
			LogicalOrRequirement.this.requirements = requirements;
			event.updateItemLore(getLore());
			event.reopenGUI();
		}, requirements).create(event.getPlayer());
	}
	
	@Override
	public boolean test(Player p) {
		return requirements.stream().anyMatch(x -> x.test(p));
	}
	
	@Override
	public AbstractRequirement clone() {
		return new LogicalOrRequirement(new ArrayList<>(requirements));
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("requirements", Utils.serializeList(requirements, AbstractRequirement::serialize));
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		requirements = QuestObject.deserializeList((List<Map<?, ?>>) savedDatas.get("requirements"), AbstractRequirement::deserialize);
	}
	
}
